/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server side config for stack size and transfer rate tweaks.
 */
public class ServerConfig
{
    public final static ServerConfig SERVER_INSTANCE = new ServerConfig(true);
    public final static ServerConfig LOCAL_INSTANCE  = new ServerConfig(false);
    public final        ModConfigSpec SPEC;
    public final        ModConfigSpec.BooleanValue increaseTransferRate;

    ServerConfig(boolean isOnlyForDedicatedServer)
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        if (isOnlyForDedicatedServer && FMLEnvironment.getDist().isClient())
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
                "E.g. if max stack limit is 1000, it will become 1000 items per tick (where applicable).",
                "How this is done will vary for each mod"
        ).define("Increase transfer rate", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static ServerConfig get()
    {
        if (FMLEnvironment.getDist().isDedicatedServer() || !Minecraft.getInstance().hasSingleplayerServer())
            return SERVER_INSTANCE;
        else
            return LOCAL_INSTANCE;
    }
}
