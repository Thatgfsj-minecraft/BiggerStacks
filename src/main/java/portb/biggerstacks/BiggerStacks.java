/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.biggerstacks.config.ClientConfig;
import portb.biggerstacks.config.ServerConfig;
import portb.configlib.ConfigLib;
import portb.configlib.IMCAPI;
import portb.slw.MyLoggerFactory;

@Mod(Constants.MOD_ID)
public class BiggerStacks
{
    public final static Logger LOGGER = LogUtils.getLogger();

    public BiggerStacks(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::processIMC);

        ConfigLib.LOGGER = MyLoggerFactory.createMyLogger(LoggerFactory.getLogger(ConfigLib.class));

        registerConfigs(modContainer);
    }

    private static void registerConfigs(ModContainer modContainer)
    {
        if (FMLEnvironment.getDist().isClient())
        {
            modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, Constants.MOD_ID + "-client.toml");
            modContainer.registerConfig(ModConfig.Type.CLIENT,
                    ServerConfig.LOCAL_INSTANCE.SPEC,
                    Constants.MOD_ID + "-local.toml"
            );
        }

        modContainer.registerConfig(ModConfig.Type.SERVER,
                ServerConfig.SERVER_INSTANCE.SPEC,
                Constants.MOD_ID + "-server.toml"
        );
    }

    void processIMC(final InterModProcessEvent event)
    {
        event.getIMCStream().forEach(imcMessage -> IMCAPI.addIMCRuleSupplier(imcMessage.senderModId(),
                imcMessage.messageSupplier()
        ));
    }
}
