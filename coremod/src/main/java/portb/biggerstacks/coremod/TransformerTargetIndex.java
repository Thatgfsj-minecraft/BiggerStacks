/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.coremod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.transformerlib.TransformerLib;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lookup table for the class names targeted by the XML transformer definitions.
 * <p>
 * {@link TransformerLib} does not expose its parsed targets, so they are read reflectively from its (version pinned)
 * transformer list. When that fails the index degrades gracefully: every candidate class is then offered to the
 * library, which is also what the original 1.20.1 launch plugin did.
 * <p>
 * Classes targeted with {@code transformChildren="true"} may match any subclass, and the super class hierarchy is not
 * available while selecting classes, so their presence forces the processor to look at every class.
 */
final class TransformerTargetIndex
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerTargetIndex.class);
    private static final String TRANSFORMER_FIELD = "transformers";

    private static volatile TransformerTargetIndex instance;

    private final Set<String> exactTargets;
    private final boolean hasTreeTargets;
    private final int targetCount;

    private TransformerTargetIndex(Set<String> exactTargets, boolean hasTreeTargets, int targetCount)
    {
        this.exactTargets = exactTargets;
        this.hasTreeTargets = hasTreeTargets;
        this.targetCount = targetCount;
    }

    /**
     * Rebuilds the index after {@link TransformerLib#loadTransformers(Class)} / {@code loadTransformersFromXML}.
     *
     * @return the number of class targets, or {@code -1} when the target list could not be read
     */
    static synchronized int refresh()
    {
        try
        {
            Field field = TransformerLib.class.getDeclaredField(TRANSFORMER_FIELD);
            field.setAccessible(true);

            Object value = field.get(null);
            if (!(value instanceof List<?> targets))
            {
                instance = null;
                return -1;
            }

            Set<String> exactTargets = new HashSet<>();
            boolean hasTreeTargets = false;

            for (Object target : targets)
            {
                if (target == null)
                    continue;

                var targetClass = target.getClass();
                var nameMethod = targetClass.getMethod("getTargetClassName");
                Object name = nameMethod.invoke(target);
                if (!(name instanceof String targetName))
                    continue;

                Field childrenField = targetClass.getDeclaredField("transformChildren");
                childrenField.setAccessible(true);
                if (childrenField.getBoolean(target))
                    hasTreeTargets = true;
                else
                    exactTargets.add(targetName);
            }

            instance = new TransformerTargetIndex(Collections.unmodifiableSet(exactTargets), hasTreeTargets, targets.size());
            return targets.size();
        }
        catch (Throwable t)
        {
            instance = null;
            LOGGER.warn("Could not read the XML transformer targets, the processor will inspect every class", t);
            return -1;
        }
    }

    /**
     * @return the current index, or {@code null} if the targets are unknown
     */
    static TransformerTargetIndex get()
    {
        return instance;
    }

    /**
     * @param internalName internal ({@code a/b/C}) class name
     * @return {@code true} if the XML definitions may apply to that class
     */
    boolean mayTransform(String internalName)
    {
        if (targetCount == 0)
            return false;

        return hasTreeTargets || exactTargets.contains(internalName);
    }

}
