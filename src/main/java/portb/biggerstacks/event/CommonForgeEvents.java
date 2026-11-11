/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.util.ConfigCommand;

import java.nio.file.Files;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonForgeEvents
{
    private final static Style     SETUP_COMMAND_SHORTCUT = Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(
                                                                                                      "/biggerstacks quicksetup"
    )).withColor(ChatFormatting.BLUE).withUnderlined(true);
    private final static Style     WIKI_LINK              = Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(
                                                                                                      "https://codeberg.org/PORTB/BiggerStacks/wiki"
    ))).withColor(ChatFormatting.BLUE).withUnderlined(true);
    private final static Component BULLET_POINT           = Component.literal("\n> ").withStyle(ChatFormatting.WHITE);
    
    /**
     * Instructs the player on what to do if they haven't set the mod up yet
     *
     * @param event
     */
    @SubscribeEvent
    public static void warnIfNoRulesetExists(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!Files.exists(Constants.RULESET_FILE) && StackSizeRules.maxRegisteredItemStackSize == 64)
        {
            if (FMLEnvironment.getDist().isDedicatedServer() &&
                        !event.getEntity().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_OWNER))
                return;
            
            //can't be bothered to make language entries for this
            event.getEntity().displayClientMessage(
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
    
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event)
    {
        ConfigCommand.register(event);
    }
}
