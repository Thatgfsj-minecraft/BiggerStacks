/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.betterbundles;

import de.cheaterpaul.betterbundles.SizedBundleItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(SizedBundleItem.class)
public class SizedBundleItemMixin
{
    @Final
    @Shadow
    protected int size;

    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lde/cheaterpaul/betterbundles/SizedBundleItem;size:I", opcode = Opcodes.GETFIELD))
    private int redirectSizeField_biggerstacks(SizedBundleItem sbi)
    {
        return SlotLimitHelper.scaleSlotLimit(this.size);
    }

    @ModifyConstant(method = {"overrideStackedOnOther", "getBarWidth", "getWeight", "appendHoverText", "add"},
            constant = @Constant(intValue = 64), remap = false)
    private static int increaseStackLimit(int value)
    {
        return SlotLimitHelper.getNewStackSize();
    }
}
