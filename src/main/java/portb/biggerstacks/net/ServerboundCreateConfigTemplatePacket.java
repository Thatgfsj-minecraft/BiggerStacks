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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.template.ConfigTemplate;
import portb.configlib.xml.Condition;
import portb.configlib.xml.Operator;
import portb.configlib.xml.OrBlock;
import portb.configlib.xml.Property;
import portb.configlib.xml.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ServerboundCreateConfigTemplatePacket extends GenericTemplateOptionsPacket implements CustomPacketPayload
{
    public static final Type<ServerboundCreateConfigTemplatePacket>                                 TYPE         = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "create_template"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCreateConfigTemplatePacket> STREAM_CODEC = StreamCodec.of(
            ServerboundCreateConfigTemplatePacket::write,
            ServerboundCreateConfigTemplatePacket::read);
    
    public ServerboundCreateConfigTemplatePacket(int normalStackLimit, int potionsStackLimit, int enchantedBooksStackLimit)
    {
        super(normalStackLimit, potionsStackLimit, enchantedBooksStackLimit);
    }
    
    public ServerboundCreateConfigTemplatePacket(RegistryFriendlyByteBuf buf)
    {
        super(buf);
    }
    
    private static ServerboundCreateConfigTemplatePacket read(RegistryFriendlyByteBuf buf)
    {
        return new ServerboundCreateConfigTemplatePacket(buf);
    }
    
    private static void write(RegistryFriendlyByteBuf buf, ServerboundCreateConfigTemplatePacket packet)
    {
        packet.encode(buf);
    }
    
    static void handleCreateConfigTemplate(ServerboundCreateConfigTemplatePacket serverboundCreateConfigTemplatePacket, IPayloadContext context)
    {
        //If on a server, check that the player actually has permissions to do this
        if (FMLEnvironment.getDist().isDedicatedServer())
        {
            if (!(context.player() instanceof ServerPlayer sender) ||
                        !sender.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_OWNER))
                return;
        }
        
        ConfigTemplate template = ConfigTemplate.generateTemplate(serverboundCreateConfigTemplatePacket);
        
        try
        {
            Files.writeString(Constants.RULESET_FILE,
                              template.toXML(),
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING
            );
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void encode(FriendlyByteBuf buf)
    {
        super.encode(buf);
    }
    
    @Override
    public Type<ServerboundCreateConfigTemplatePacket> type()
    {
        return TYPE;
    }
}
