/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.xml.RuleSet;

import java.util.function.Consumer;

/**
 * Configuration task responsible for providing the current rule set to clients during the configuration phase.
 */
public record RulesetSyncTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask
{
    private static final Type TYPE = new Type(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "ruleset_sync"));
    
    @Override
    public void run(Consumer<CustomPacketPayload> sender)
    {
        RuleSet ruleSet = StackSizeRules.getRuleSet();
        if (ruleSet != null)
        {
            sender.accept(new ClientboundRulesHandshakePacket(ruleSet));
        }
        listener.finishCurrentTask(type());
    }
    
    @Override
    public Type type()
    {
        return TYPE;
    }
}
