/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import portb.biggerstacks.Constants;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEvents
{
    private static ServerLifecycleHandler handler;
    
    /**
     * Creates a ServerLifecycleHandler that manages config updates
     */
    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event)
    {
        handler = new ServerLifecycleHandler();
        
        NeoForge.EVENT_BUS.register(handler);
    }
    
    /**
     * Unregisters the handler
     */
    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event)
    {
        NeoForge.EVENT_BUS.unregister(handler);
        
        handler.ensureStopped();
    }
    
}
