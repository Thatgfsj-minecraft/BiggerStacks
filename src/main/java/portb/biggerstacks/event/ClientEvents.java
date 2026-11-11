/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.ClientConfig;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.ConfigLib;
import portb.configlib.xml.RuleSet;

import java.nio.file.Files;

import static portb.biggerstacks.Constants.TOOLTIP_NUMBER_FORMAT;

@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID)
public class ClientEvents
{
    /**
     * Shows item count on the tooltip
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static public void showExactItemStackCount(ItemTooltipEvent event)
    {
        if (!ClientConfig.enableNumberShortening.get())
            return;
        
        var stack = event.getItemStack();
        
        if (stack.getCount() > Constants.ONE_THOUSAND)
        {
            event.getToolTip()
                 .add(1,
                      Component.translatable("biggerstacks.exact.count",
                                             Component.literal(TOOLTIP_NUMBER_FORMAT.format(stack.getCount()))
                                                      .withStyle(ClientConfig.getNumberColour())
                               )
                               .withStyle(ChatFormatting.GRAY)
                 );
        }
    }
    
    /**
     * Loads the ruleset when the client connects to a remote server
     * (In single-player, the integrated server handles this)
     */
    @SubscribeEvent
    static public void loadRulesetOnLogin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        // Only load ruleset for remote servers
        // In single-player, the integrated server will send the ruleset to the client
        if (event.getPlayer() != null && event.getConnection().isMemoryConnection()) {
            return; // This is a single-player game, let the server handle it
        }
        
        // Try to load ruleset from local config file when connecting to remote server
        try {
            if (Files.exists(Constants.RULESET_FILE)) {
                RuleSet ruleSet = ConfigLib.readRuleset(Constants.RULESET_FILE);
                StackSizeRules.setRuleSet(ruleSet);
            }
        } catch (Exception e) {
            // If we can't load the ruleset, we'll fall back to the default behavior
            StackSizeRules.setRuleSet(null);
        }
    }
    
    /**
     * Unloads the ruleset when the client disconnects from a world/server
     */
    @SubscribeEvent
    static public void forgetRuleset(ClientPlayerNetworkEvent.LoggingOut event)
    {
        // Only reset ruleset if we're disconnecting from a remote server
        // For single-player, the server will handle the ruleset
        if (event.getPlayer() != null && event.getPlayer().level().isClientSide()) {
            StackSizeRules.setRuleSet(null);
        }
    }
}
