/*
 * Copyright (c) PORTB 2026
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.sophisticatedcore;

import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;

/**
 * Clamps the slot limit computed by the stack upgrades. 1.21.11 still expresses the limit as
 * {@code multiplier * 64}, which stays correct because {@code InventoryHandler#getBaseCapacity} divides by 64 again;
 * only the hard cap has to be lowered from about 2 billion to the maximum stack size BiggerStacks allows, so the
 * value can be used in the multiplier based capacity calculations of tanks, batteries and pumps without overflowing.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem", remap = false)
public abstract class StackUpgradeItemMixin
{
    @Inject(method = "getInventorySlotLimit(Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageWrapper;)I",
            at = @At("RETURN"),
            cancellable = true)
    private static void biggerstacks$clampInventorySlotLimit(IStorageWrapper storageWrapper, CallbackInfoReturnable<Integer> cir)
    {
        int slotLimit = cir.getReturnValue();
        int clamped = StackSizeRules.clampStackSize(slotLimit);

        if (clamped != slotLimit)
            cir.setReturnValue(clamped);
    }
}
