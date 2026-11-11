/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.gui.ConfigureScreen;

/**
 * Handles packets for ruleset config.
 */
public final class PacketHandler
{
    private static final String PROTOCOL_VERSION = "1";
    
    private PacketHandler()
    {
    }
    
    /**
     * Registers packets.
     */
    public static void register(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        
        registrar.configurationToClient(ClientboundRulesHandshakePacket.TYPE,
                                        ClientboundRulesHandshakePacket.STREAM_CODEC,
                                        PacketHandler::handleHandshake);
        registrar.playToClient(ClientboundRulesUpdatePacket.TYPE,
                               ClientboundRulesUpdatePacket.STREAM_CODEC,
                               PacketHandler::handleUpdate);
        registrar.playToClient(ClientboundConfigureScreenOpenPacket.TYPE,
                               ClientboundConfigureScreenOpenPacket.STREAM_CODEC,
                               PacketHandler::handleOpenScreenPacket);
        registrar.playToServer(ServerboundCreateConfigTemplatePacket.TYPE,
                               ServerboundCreateConfigTemplatePacket.STREAM_CODEC,
                               ServerboundCreateConfigTemplatePacket::handleCreateConfigTemplate);
    }
    
    private static void handleHandshake(ClientboundRulesHandshakePacket packet, IPayloadContext context)
    {
        StackSizeRules.setRuleSet(packet.rules());
    }
    
    private static void handleUpdate(ClientboundRulesUpdatePacket packet, IPayloadContext context)
    {
        StackSizeRules.setRuleSet(packet.rules());
    }
    
    private static void handleOpenScreenPacket(ClientboundConfigureScreenOpenPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> ConfigureScreen.open(packet));
    }

}
