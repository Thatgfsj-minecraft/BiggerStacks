/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import portb.biggerstacks.Constants;

public class ClientboundConfigureScreenOpenPacket extends GenericTemplateOptionsPacket implements CustomPacketPayload
{
    public static final Type<ClientboundConfigureScreenOpenPacket>                                 TYPE         = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundConfigureScreenOpenPacket> STREAM_CODEC = StreamCodec.of(
            ClientboundConfigureScreenOpenPacket::write,
            ClientboundConfigureScreenOpenPacket::read);
    
    private final boolean isAlreadyUsingCustomFile;
    
    public ClientboundConfigureScreenOpenPacket(boolean hasExistingCustomFile, int normalStackLimit, int potionsStackLimit, int enchantedBooksStackLimit)
    {
        super(normalStackLimit, potionsStackLimit, enchantedBooksStackLimit);
        
        this.isAlreadyUsingCustomFile = hasExistingCustomFile;
    }
    
    public ClientboundConfigureScreenOpenPacket(FriendlyByteBuf buf)
    {
        super(buf);
        
        isAlreadyUsingCustomFile = buf.readBoolean();
    }
    
    private static ClientboundConfigureScreenOpenPacket read(RegistryFriendlyByteBuf buf)
    {
        return new ClientboundConfigureScreenOpenPacket(buf);
    }
    
    private static void write(RegistryFriendlyByteBuf buf, ClientboundConfigureScreenOpenPacket packet)
    {
        packet.encode(buf);
    }
    
    public void encode(FriendlyByteBuf buf)
    {
        super.encode(buf);
        
        buf.writeBoolean(isAlreadyUsingCustomFile);
    }
    
    public boolean isAlreadyUsingCustomFile()
    {
        return isAlreadyUsingCustomFile;
    }
    
    @Override
    public Type<ClientboundConfigureScreenOpenPacket> type()
    {
        return TYPE;
    }
}
