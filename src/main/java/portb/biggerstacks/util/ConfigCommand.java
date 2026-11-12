/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.net.ClientboundConfigureScreenOpenPacket;
import portb.configlib.template.ConfigTemplate;
import portb.configlib.template.TemplateOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigCommand
{
    public final static Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        var cmd = Commands.literal("biggerstacks")
                .then(Commands.literal("quicksetup").executes(context ->
                {
                    try
                    {
                        boolean hasCustomExistingFile = false;
                        TemplateOptions template = new TemplateOptions(64, 1, 1);

                        if (Files.exists(Constants.RULESET_FILE))
                        {
                            try
                            {
                                template = ConfigTemplate.readParametersFromTemplate(new String(
                                        Files.readAllBytes(Constants.RULESET_FILE), StandardCharsets.UTF_8));
                            }
                            catch (Throwable e)
                            {
                                LOGGER.debug("Error reading template file", e);
                                hasCustomExistingFile = true;
                            }
                        }

                        ServerPlayer player = context.getSource().getPlayerOrException();

                        ServerPlayNetworking.send(player, new ClientboundConfigureScreenOpenPacket(
                                hasCustomExistingFile,
                                template.getNormalStackLimit(),
                                template.getPotionStackLimit(),
                                template.getEnchBookLimit()
                        ));
                    }
                    catch (CommandSyntaxException e)
                    {
                        context.getSource().sendFailure(Component.translatable("biggerstacks.player.expected"));
                        return 0;
                    }

                    return 1;
                }))
                .then(Commands.literal("info").executes(context ->
                {
                    var source = context.getSource();
                    int maxStack = StackSizeRules.getMaxStackSize();
                    boolean hasRules = StackSizeRules.getRuleSet() != null;
                    source.sendSuccess(() -> Component.literal("Current BiggerStacks max stack size: " + maxStack +
                            (hasRules ? "" : " (using fallback)")), false);
                    return 1;
                }));

        // If on a dedicated server, require permissions. Some command sources (for example the ones used
        // to filter the command tree sent to a joining player) have no server attached, so guard against null.
        cmd.requires(commandSourceStack ->
        {
            var server = commandSourceStack.getServer();
            return server == null
                    || !server.isDedicatedServer()
                    || commandSourceStack.permissions().hasPermission(
                    net.minecraft.server.permissions.Permissions.COMMANDS_OWNER);
        });

        dispatcher.register(cmd);
    }
}
