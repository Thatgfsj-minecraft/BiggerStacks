/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Server side config for stack size and transfer rate tweaks.
 */
public class ServerConfig
{
    public final static ServerConfig SERVER_INSTANCE = new ServerConfig(true);
    public final static ServerConfig LOCAL_INSTANCE = new ServerConfig(false);
    public final ForgeConfigSpec SPEC;
    public final ForgeConfigSpec.BooleanValue increaseTransferRate;

    ServerConfig(boolean isOnlyForDedicatedServer)
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        if (isOnlyForDedicatedServer && FMLEnvironment.dist.isClient())
        {
            builder.comment(
                    "IGNORE THIS CONFIG FILE!!!!",
                    "IGNORE THIS CONFIG FILE!!!!",
                    "IGNORE THIS CONFIG FILE!!!!",
                    "IT IS ONLY USED WHEN HOSTING ON LAN"
            );
        }

        builder.push("biggerstacks");

        increaseTransferRate = builder.comment(
                "Whether to increase max transfer rate of some mods to the new stack limit/t.",
                "E.g. if max stack limit is 1000, it will become 1000 items per tick (where applicable)."
        ).define("Increase transfer rate", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static ServerConfig get()
    {
        if (FMLEnvironment.dist.isDedicatedServer() || !Minecraft.getInstance().hasSingleplayerServer())
        {
            return SERVER_INSTANCE;
        }
        else
        {
            return LOCAL_INSTANCE;
        }
    }
}
