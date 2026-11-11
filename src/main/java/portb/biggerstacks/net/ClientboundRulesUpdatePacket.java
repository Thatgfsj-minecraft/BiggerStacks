/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import portb.biggerstacks.Constants;
import portb.configlib.xml.RuleSet;

/**
 * Packet sent to clients when the config file is updated.
 */
public record ClientboundRulesUpdatePacket(RuleSet rules) implements CustomPacketPayload
{
    public static final Type<ClientboundRulesUpdatePacket>                                 TYPE         = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "rules_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRulesUpdatePacket> STREAM_CODEC = StreamCodec.of(
            ClientboundRulesUpdatePacket::write,
            ClientboundRulesUpdatePacket::read);
    
    private static ClientboundRulesUpdatePacket read(RegistryFriendlyByteBuf buf)
    {
        return new ClientboundRulesUpdatePacket(RuleSet.fromBytes(buf.readByteArray()));
    }
    
    private static void write(RegistryFriendlyByteBuf buf, ClientboundRulesUpdatePacket packet)
    {
        buf.writeByteArray(packet.rules().toBytes());
    }
    
    @Override
    public Type<ClientboundRulesUpdatePacket> type()
    {
        return TYPE;
    }
}
