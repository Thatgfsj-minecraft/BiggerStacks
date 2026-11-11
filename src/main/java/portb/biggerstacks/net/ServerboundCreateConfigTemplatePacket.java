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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import portb.biggerstacks.Constants;
import portb.configlib.template.ConfigTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ServerboundCreateConfigTemplatePacket extends GenericTemplateOptionsPacket implements CustomPacketPayload
{
    public static final Type<ServerboundCreateConfigTemplatePacket>                         TYPE         = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "create_template"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundCreateConfigTemplatePacket> STREAM_CODEC = StreamCodec.of(
            ServerboundCreateConfigTemplatePacket::write,
            ServerboundCreateConfigTemplatePacket::read);

    public ServerboundCreateConfigTemplatePacket(int normalStackLimit, int potionsStackLimit, int enchantedBooksStackLimit)
    {
        super(normalStackLimit, potionsStackLimit, enchantedBooksStackLimit);
    }

    public ServerboundCreateConfigTemplatePacket(FriendlyByteBuf buf)
    {
        super(buf);
    }

    private static ServerboundCreateConfigTemplatePacket read(FriendlyByteBuf buf)
    {
        return new ServerboundCreateConfigTemplatePacket(buf);
    }

    private static void write(FriendlyByteBuf buf, ServerboundCreateConfigTemplatePacket packet)
    {
        packet.encode(buf);
    }

    /**
     * Writes the template to the ruleset file on the server. Called on the server thread.
     */
    public static void handle(ServerboundCreateConfigTemplatePacket packet, ServerPlayer player)
    {
        //if on a dedicated server, check that the player actually has permissions to do this
        if (player.level().getServer().isDedicatedServer()
                && !player.permissions().hasPermission(Permissions.COMMANDS_OWNER))
            return;

        ConfigTemplate template = ConfigTemplate.generateTemplate(packet);

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
