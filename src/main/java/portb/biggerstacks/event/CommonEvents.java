/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.net.FabricPayloads;
import portb.biggerstacks.util.ConfigCommand;

import java.io.File;

/**
 * Server side event wiring for the Fabric port.
 */
public final class CommonEvents
{
    private static final Style SETUP_COMMAND_SHORTCUT = Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(
            "/biggerstacks quicksetup")).withColor(ChatFormatting.BLUE).withUnderlined(true);
    private static final Style WIKI_LINK = Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(
            "https://codeberg.org/PORTB/BiggerStacks/wiki"))).withColor(ChatFormatting.BLUE).withUnderlined(true);
    private static final Component BULLET_POINT = Component.literal("\n> ").withStyle(ChatFormatting.WHITE);

    private static ServerRulesetHandler rulesetHandler;

    private CommonEvents()
    {
    }

    public static void register()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ConfigCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
        {
            rulesetHandler = new ServerRulesetHandler(server);
            rulesetHandler.start();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
            if (rulesetHandler != null)
            {
                rulesetHandler.stop();
                rulesetHandler = null;
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            var player = handler.player;

            //send the current ruleset to the client
            FabricPayloads.sendRulesTo(player);

            warnIfNoRulesetExists(player);
        });
    }

    private static void warnIfNoRulesetExists(net.minecraft.server.level.ServerPlayer player)
    {
        if (!new File(Constants.RULESET_FILE.toUri()).exists() && StackSizeRules.maxRegisteredItemStackSize == 64)
        {
            if (player.level().getServer().isDedicatedServer()
                    && !player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_OWNER))
                return;

            player.displayClientMessage(
                    Component.literal(
                                    "Biggerstacks is installed, but you have not configured it and there are no other mods installed that use it.")
                            .append(BULLET_POINT)
                            .append("Run ")
                            .append(Component.literal("/biggerstacks quicksetup").withStyle(SETUP_COMMAND_SHORTCUT))
                            .append(" and the mod will generate a simple ruleset for you")
                            .append(BULLET_POINT)
                            .append("Or click ")
                            .append(Component.literal("here").withStyle(WIKI_LINK))
                            .append(" to see how to create a custom ruleset")
                            .withStyle(ChatFormatting.GOLD),
                    false
            );
        }
    }
}
