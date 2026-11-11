/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.biggerstacks.config.ServerConfig;
import portb.biggerstacks.event.CommonEvents;
import portb.biggerstacks.net.FabricPayloads;
import portb.configlib.ConfigLib;
import portb.slw.MyLoggerFactory;

public class BiggerStacks implements ModInitializer
{
    public final static Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        ConfigLib.LOGGER = MyLoggerFactory.createMyLogger(LoggerFactory.getLogger(ConfigLib.class));

        ServerConfig.load();

        FabricPayloads.register();
        CommonEvents.register();

        LOGGER.info("BiggerStacks initializing (Fabric, Minecraft 1.21.11)");
    }
}
