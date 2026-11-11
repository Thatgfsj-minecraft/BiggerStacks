/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.ClientConfig;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.gui.ConfigureScreen;
import portb.biggerstacks.net.ClientboundConfigureScreenOpenPacket;
import portb.biggerstacks.net.ClientboundRulesHandshakePacket;
import portb.biggerstacks.net.ClientboundRulesUpdatePacket;

import static portb.biggerstacks.Constants.TOOLTIP_NUMBER_FORMAT;

/**
 * Client side event wiring for the Fabric port.
 */
public final class ClientEvents
{
    private ClientEvents()
    {
    }

    public static void register()
    {
        //server -> client ruleset sync
        ClientPlayNetworking.registerGlobalReceiver(ClientboundRulesHandshakePacket.TYPE,
                (payload, context) -> context.client().execute(() -> StackSizeRules.setRuleSet(payload.rules())));

        ClientPlayNetworking.registerGlobalReceiver(ClientboundRulesUpdatePacket.TYPE,
                (payload, context) -> context.client().execute(() -> StackSizeRules.setRuleSet(payload.rules())));

        //server -> client request to open the config screen
        ClientPlayNetworking.registerGlobalReceiver(ClientboundConfigureScreenOpenPacket.TYPE,
                (payload, context) -> context.client().execute(() -> ConfigureScreen.open(payload)));

        //clear the ruleset when leaving a world/server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> StackSizeRules.setRuleSet(null));

        //show the exact item count in tooltips when number shortening is enabled
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) ->
        {
            if (!ClientConfig.enableNumberShortening)
                return;

            if (stack.getCount() > Constants.ONE_THOUSAND)
            {
                lines.add(1, Component.translatable("biggerstacks.exact.count",
                                Component.literal(TOOLTIP_NUMBER_FORMAT.format(stack.getCount()))
                                        .withStyle(ClientConfig.getNumberColour()))
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
