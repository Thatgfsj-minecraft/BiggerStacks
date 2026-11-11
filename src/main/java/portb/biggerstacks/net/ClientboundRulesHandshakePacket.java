/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.xml.RuleSet;

/**
 * Sent to a client when it joins the server so that the client knows the stack size rules before they are needed.
 */
public record ClientboundRulesHandshakePacket(RuleSet rules) implements CustomPacketPayload
{
    public static final Type<ClientboundRulesHandshakePacket>                         TYPE         = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "rules_handshake"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundRulesHandshakePacket> STREAM_CODEC = StreamCodec.of(
            ClientboundRulesHandshakePacket::write,
            ClientboundRulesHandshakePacket::read);

    private static ClientboundRulesHandshakePacket read(FriendlyByteBuf buf)
    {
        return new ClientboundRulesHandshakePacket(RuleSet.fromBytes(buf.readByteArray()));
    }

    private static void write(FriendlyByteBuf buf, ClientboundRulesHandshakePacket packet)
    {
        buf.writeByteArray(packet.rules().toBytes());
    }

    public ClientboundRulesHandshakePacket()
    {
        this(StackSizeRules.getRuleSet());
    }

    @Override
    public Type<ClientboundRulesHandshakePacket> type()
    {
        return TYPE;
    }
}
