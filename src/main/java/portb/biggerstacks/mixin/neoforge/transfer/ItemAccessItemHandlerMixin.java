/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.neoforge.transfer;

import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.util.StackSizeHelper;

/**
 * Item access based handler variant of the transfer API capacity patch.
 */
@Mixin(value = ItemAccessItemHandler.class, remap = false)
public class ItemAccessItemHandlerMixin
{
    @Inject(method = "getCapacity", at = @At("RETURN"), cancellable = true, require = 1)
    private void biggerstacks$raiseCapacity(int index, ItemResource resource, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(StackSizeHelper.scaleTransferCapacity(cir.getReturnValue()));
    }
}
