/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.ae2;

import appeng.parts.automation.IOBusPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.ServerConfig;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(IOBusPart.class)
public class IOBusPartMixin
{
    @Inject(method = "getOperationsPerTick", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void increaseTransferRate(CallbackInfoReturnable<Integer> returnInfo)
    {
        if (ServerConfig.get().increaseTransferRate.get())
            SlotLimitHelper.scaleSlotLimit(returnInfo);
    }
    
}
