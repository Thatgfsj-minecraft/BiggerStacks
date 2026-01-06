/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.modularrouters;

import me.desht.modularrouters.logic.compiled.CompiledModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import portb.biggerstacks.config.ServerConfig;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(CompiledModule.class)
public class CompiledModuleMixin
{
    @ModifyConstant(method = "getItemsPerTick", constant = @Constant(intValue = 64), remap = false, require = 0)
    private int increaseTransferRate(int value)
    {
        if (ServerConfig.get().increaseTransferRate.get())
            return SlotLimitHelper.increaseTransferRate(value);
        else
            return value;
    }
}

