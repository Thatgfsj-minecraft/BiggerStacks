/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import portb.biggerstacks.BiggerStacks;
import portb.configlib.xml.RuleSet;

public class StackSizeRules
{
    private static final int ABSOLUTE_MAX_STACK_SIZE = 1_073_741_823;
    private static boolean clampWarningLogged = false;

    /**
     * Tracks the maximum stack size from registered items (see
     * {@link portb.biggerstacks.mixin.vanilla.ItemPropertiesMixin}).
     */
    public static int maxRegisteredItemStackSize = 64;

    /**
     * The global ruleset that is either received from the server or read from the config file
     */
    private static RuleSet ruleSet;

    public static RuleSet getRuleSet()
    {
        return ruleSet;
    }

    public static void setRuleSet(RuleSet ruleSet)
    {
        StackSizeRules.ruleSet = ruleSet;

        if (ruleSet != null)
            BiggerStacks.LOGGER.info("Loaded BiggerStacks ruleset. Max declared stack size: {}", ruleSet.getMaxStacksize());
        else
            BiggerStacks.LOGGER.warn("Stack size ruleset reset to null; falling back to defaults.");
    }

    public static int getMaxStackSize()
    {
        int configuredMax = maxRegisteredItemStackSize;
        if (ruleSet != null)
            configuredMax = Math.max(ruleSet.getMaxStacksize(), maxRegisteredItemStackSize);

        return clampStackSize(configuredMax);
    }

    public static int getBaselineStackSize()
    {
        return Math.max(64, Math.min(maxRegisteredItemStackSize, ABSOLUTE_MAX_STACK_SIZE));
    }

    public static int clampStackSize(int requestedSize)
    {
        if (requestedSize > ABSOLUTE_MAX_STACK_SIZE)
        {
            if (!clampWarningLogged)
            {
                BiggerStacks.LOGGER.warn("Requested stack size {} exceeds hard cap of {}; larger values will be clamped.",
                        requestedSize, ABSOLUTE_MAX_STACK_SIZE);
                clampWarningLogged = true;
            }
            return ABSOLUTE_MAX_STACK_SIZE;
        }
        return requestedSize;
    }
}
