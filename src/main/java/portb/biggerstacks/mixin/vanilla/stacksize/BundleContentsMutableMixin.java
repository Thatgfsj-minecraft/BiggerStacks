/*
 * Copyright (c) PORTB
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla.stacksize;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.util.StackSizeHelper;

@Mixin(BundleContents.Mutable.class)
public class BundleContentsMutableMixin
{
    @Inject(method = "getMaxAmountToAdd", at = @At("RETURN"), cancellable = true)
    private void biggerstacks$scaleCapacity(ItemStack stack, CallbackInfoReturnable<Integer> cir)
    {
        int vanillaLimit = cir.getReturnValue();
        cir.setReturnValue(StackSizeHelper.scaleSlotLimit(vanillaLimit));
    }
}
