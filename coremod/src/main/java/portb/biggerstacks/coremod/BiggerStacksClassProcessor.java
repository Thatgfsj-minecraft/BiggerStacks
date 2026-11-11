/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.coremod;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.transformerlib.TransformerLib;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FancyModLoader 10 {@link ClassProcessor} that delegates to the XML driven {@link TransformerLib} engine.
 * <p>
 * This replaces the 1.20.1 {@code ILaunchPluginService} that was injected into ModLauncher through reflection: the
 * library itself is unchanged and still receives every candidate {@link ClassNode} through
 * {@link TransformerLib#handleTransformation(ClassNode)}.
 */
public final class BiggerStacksClassProcessor implements ClassProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BiggerStacksClassProcessor.class);

    private static final ProcessorName NAME = new ProcessorName("biggerstacks", "transformerlib");
    private static final AtomicInteger HANDLED = new AtomicInteger();
    private static final AtomicInteger TRANSFORMED = new AtomicInteger();

    private static volatile boolean warnedAboutUnloadedEngine;
    private static volatile boolean reportedFirstClass;

    @Override
    public ProcessorName name()
    {
        return NAME;
    }

    /**
     * The XML patches are applied to the bytecode that mixins produced, so that constants introduced or shifted around
     * by mixins are seen by the transformers as well. The frame computation marker is kept because the processor only
     * asks for {@link ComputeFlags#COMPUTE_MAXS}.
     */
    @Override
    public Set<ProcessorName> runsAfter()
    {
        return Set.of(ClassProcessorIds.MIXIN, ClassProcessorIds.COMPUTING_FRAMES);
    }

    @Override
    public void link(LinkContext context)
    {
        // FML links every processor once, before the game layer exists. In a development environment this class
        // loader can already see the XML definitions, in production the game layer (mixin plugin) does it later.
        if (!TransformerLibBootstrap.isReady() && TransformerLibBootstrap.initialize(TransformerLibBootstrap.class))
            LOGGER.info("BiggerStacks XML class processor linked, {} definition file(s) loaded", TransformerLibBootstrap.getLoadedFiles().size());
        else
            LOGGER.info("BiggerStacks XML class processor linked (definition loading deferred to the game layer)");
    }

    @Override
    public boolean handlesClass(SelectionContext context)
    {
        if (context.empty())
            return false;

        if (!TransformerLibBootstrap.isReady())
        {
            if (!warnedAboutUnloadedEngine)
            {
                warnedAboutUnloadedEngine = true;
                LOGGER.warn("Class {} was loaded before the XML transformer engine was initialised; XML patches are skipped for it",
                        context.type().getClassName());
            }
            return false;
        }

        String internalName = context.type().getInternalName();
        if (isIgnored(internalName))
            return false;

        TransformerTargetIndex index = TransformerTargetIndex.get();
        boolean candidate = index == null || index.mayTransform(internalName);

        if (candidate && !reportedFirstClass)
        {
            reportedFirstClass = true;
            LOGGER.info("XML class processor is now inspecting classes (first: {})", context.type().getClassName());
        }

        // Targets unknown: mirror the old launch plugin and let TransformerLib decide.
        return candidate;
    }

    @Override
    public ComputeFlags processClass(TransformationContext context)
    {
        ClassNode classNode = context.node();
        int handled = HANDLED.incrementAndGet();
        if (handled % 5000 == 0)
            LOGGER.info("XML transformer processor: {} class(es) inspected, {} transformed so far", handled, TRANSFORMED.get());

        try
        {
            if (TransformerLib.handleTransformation(classNode))
            {
                int transformed = TRANSFORMED.incrementAndGet();
                if (transformed <= 10 || transformed % 50 == 0)
                    LOGGER.info("Transformed {} through XML rules ({} class(es) transformed so far)",
                            classNode.name, transformed);

                context.audit("biggerstacks", NAME.toString(), classNode.name);

                return ComputeFlags.COMPUTE_MAXS;
            }
        }
        catch (Throwable t)
        {
            // A broken rule must not take the whole game down: log the class and leave the bytecode untouched.
            LOGGER.error("Failed to apply the XML transformers to " + classNode.name, t);
        }

        return ComputeFlags.NO_REWRITE;
    }

    private static boolean isIgnored(String internalName)
    {
        // Mixin and its infrastructure, our own coremod classes, generated helpers and JDK classes are never targets.
        return internalName.contains("/mixin/")
                || internalName.startsWith("org/spongepowered/")
                || internalName.startsWith("com/llamalad7/")
                || internalName.startsWith("portb/biggerstacks/coremod/")
                || internalName.startsWith("net/neoforged/fml/generated/")
                || internalName.startsWith("java/")
                || internalName.startsWith("jdk/")
                || internalName.startsWith("sun/")
                || internalName.startsWith("org/objectweb/asm/");
    }
}
