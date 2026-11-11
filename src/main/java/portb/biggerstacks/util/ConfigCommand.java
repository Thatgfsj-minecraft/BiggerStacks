/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import portb.biggerstacks.Constants;
import portb.biggerstacks.net.ClientboundConfigureScreenOpenPacket;
import portb.biggerstacks.net.ServerboundCreateConfigTemplatePacket;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.template.ConfigTemplate;
import portb.configlib.template.TemplateOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Supplier;

public class ConfigCommand
{
    public final static Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Despite not reaching across logical sides, this command is intended to work only on singleplayer.
     * See {@link ServerboundCreateConfigTemplatePacket#handleCreateConfigTemplate(ServerboundCreateConfigTemplatePacket, Supplier)}, where the packet the client sends to the server is ignored by a dedicated server
     *
     * @param event
     */
    public static void register(RegisterCommandsEvent event)
    {
        var cmd = Commands.literal("biggerstacks").then(Commands.literal("quicksetup").executes(
                context -> {
                    try
                    {
                        boolean         hasCustomExistingFile = false;
                        TemplateOptions template              = new TemplateOptions(64, 1, 1);
                        
                        if (Files.exists(Constants.RULESET_FILE))
                        {
                            try
                            {
                                template = ConfigTemplate.readParametersFromTemplate(new String(Files.readAllBytes(
                                        Constants.RULESET_FILE), StandardCharsets.UTF_8));
                            }
                            catch (Throwable e)
                            {
                                LOGGER.debug("Error reading template file", e);
                                hasCustomExistingFile = true;
                            }
                        }
                        
                        var player = context.getSource().getPlayerOrException();
                        
                        PacketDistributor.sendToPlayer(
                                player,
                                new ClientboundConfigureScreenOpenPacket(
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
                })
        ).then(Commands.literal("info").executes(context -> {
            var source = context.getSource();
            int maxStack = StackSizeRules.getMaxStackSize();
            boolean hasRules = StackSizeRules.getRuleSet() != null;
            source.sendSuccess(() -> Component.literal("Current BiggerStacks max stack size: " + maxStack +
                    (hasRules ? "" : " (using fallback)")), false);
            return 1;
        }));
        
        //if on a server, require permissions
        if (FMLEnvironment.getDist().isDedicatedServer())
            cmd.requires(Commands.hasPermission(Constants.CHANGE_STACK_SIZE_COMMAND_PERMISSION));
        
        event.getDispatcher().register(cmd);
    }
}
