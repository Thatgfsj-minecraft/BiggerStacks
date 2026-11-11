/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import portb.biggerstacks.Constants;
import portb.biggerstacks.net.PacketHandler;
import portb.biggerstacks.net.RulesetSyncTask;
import portb.biggerstacks.net.ClientboundRulesHandshakePacket;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonModEvents
{
    /**
     * Registers network packets.
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PacketHandler.register(event);
    }
    
    @SubscribeEvent
    public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event)
    {
        var listener = event.getListener();
        if (listener.hasChannel(ClientboundRulesHandshakePacket.TYPE))
        {
            event.register(new RulesetSyncTask(listener));
        }
    }
}
