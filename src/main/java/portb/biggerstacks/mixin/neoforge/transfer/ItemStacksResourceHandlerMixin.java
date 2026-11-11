/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.neoforge.transfer;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.util.StackSizeHelper;

/**
 * Lifts the {@code Item.ABSOLUTE_MAX_STACK_SIZE} (99) cap of the NeoForge transfer API, so mods
 * implemented against it can store stacks larger than 99 without per-mod patches.
 */
@Mixin(value = ItemStacksResourceHandler.class, remap = false)
public class ItemStacksResourceHandlerMixin
{
    @Inject(method = "getCapacity", at = @At("RETURN"), cancellable = true, require = 1)
    private void biggerstacks$raiseCapacity(int index, ItemResource resource, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(StackSizeHelper.scaleTransferCapacity(cir.getReturnValue()));
    }
}
