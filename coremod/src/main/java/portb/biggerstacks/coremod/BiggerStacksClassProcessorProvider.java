/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.coremod;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the BiggerStacks XML class processor through the Java {@link java.util.ServiceLoader} mechanism.
 * <p>
 * The service file lives at
 * {@code META-INF/services/net.neoforged.neoforgespi.transformation.ClassProcessorProvider}. FancyModLoader collects
 * providers with {@code ServiceLoader.load(ClassProcessorProvider.class)} before the game layer is built, so this
 * class must not touch game layer types.
 */
public final class BiggerStacksClassProcessorProvider implements ClassProcessorProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BiggerStacksClassProcessorProvider.class);

    @Override
    public void createProcessors(Context context, Collector collector)
    {
        collector.add(new BiggerStacksClassProcessor());
        LOGGER.info("Registered the BiggerStacks XML class processor");
    }
}
