/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.client;

import net.fabricmc.api.ClientModInitializer;
import portb.biggerstacks.config.ClientConfig;
import portb.biggerstacks.event.ClientEvents;

public class BiggerStacksClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientConfig.load();
        ClientEvents.register();
    }
}
