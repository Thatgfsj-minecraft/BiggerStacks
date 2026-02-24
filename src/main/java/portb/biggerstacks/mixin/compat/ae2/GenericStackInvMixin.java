/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.ae2;

import appeng.api.stacks.AEKeyType;
import appeng.helpers.externalstorage.GenericStackInv;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(GenericStackInv.class)
public class GenericStackInvMixin
{
    @Inject(method = "getCapacity", at = @At("RETURN"), cancellable = true, remap = false)
    private void increaseStackCapacity(AEKeyType space, CallbackInfoReturnable<Long> cir)
    {
        if (space == AEKeyType.items())
        {
            cir.cancel();
            cir.setReturnValue((long) SlotLimitHelper.getNewStackSize());
        }
    }
}
