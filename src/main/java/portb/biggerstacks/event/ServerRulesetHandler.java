/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.minecraft.server.MinecraftServer;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.net.FabricPayloads;
import portb.configlib.ConfigFileWatcher;
import portb.configlib.ConfigLib;
import portb.configlib.xml.RuleSet;

import static portb.biggerstacks.Constants.RULESET_FILE;

/**
 * Loads the ruleset on server start and keeps it in sync with the config file.
 */
public class ServerRulesetHandler
{
    private final MinecraftServer server;
    private       ConfigFileWatcher watcher;
    private       boolean           stopped = false;

    public ServerRulesetHandler(MinecraftServer server)
    {
        this.server = server;
    }

    public void start()
    {
        StackSizeRules.setRuleSet(ConfigLib.readRuleset(RULESET_FILE));

        watcher = new ConfigFileWatcher(RULESET_FILE);
        watcher.setOnUpdateAction(this::onRulesetChanged);
        watcher.start();
    }

    private void onRulesetChanged(RuleSet ruleSet)
    {
        StackSizeRules.setRuleSet(ruleSet);
        FabricPayloads.sendRulesUpdate(server.getPlayerList());
    }

    public void stop()
    {
        if (!stopped && watcher != null)
        {
            watcher.stop();
            stopped = true;
        }
    }
}
