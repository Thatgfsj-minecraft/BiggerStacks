/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.List;

/**
 * Payload registration and sending helpers for the Fabric networking API.
 */
public final class FabricPayloads
{
    private FabricPayloads()
    {
    }

    public static void register()
    {
        PayloadTypeRegistry.playS2C().register(ClientboundRulesHandshakePacket.TYPE,
                ClientboundRulesHandshakePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundRulesUpdatePacket.TYPE,
                ClientboundRulesUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundConfigureScreenOpenPacket.TYPE,
                ClientboundConfigureScreenOpenPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerboundCreateConfigTemplatePacket.TYPE,
                ServerboundCreateConfigTemplatePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundCreateConfigTemplatePacket.TYPE,
                (payload, context) -> context.player().level().getServer().execute(
                        () -> ServerboundCreateConfigTemplatePacket.handle(payload, context.player())
                ));
    }

    public static void sendRulesTo(ServerPlayer player)
    {
        var rules = portb.biggerstacks.config.StackSizeRules.getRuleSet();
        if (rules != null)
            ServerPlayNetworking.send(player, new ClientboundRulesHandshakePacket(rules));
    }

    public static void sendRulesUpdate(PlayerList players)
    {
        var rules = portb.biggerstacks.config.StackSizeRules.getRuleSet();
        if (rules == null)
            return;

        ClientboundRulesUpdatePacket packet = new ClientboundRulesUpdatePacket(rules);
        List<ServerPlayer> list = players.getPlayers();
        for (ServerPlayer player : list)
            ServerPlayNetworking.send(player, packet);
    }
}
