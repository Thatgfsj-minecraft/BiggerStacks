/*
 * Copyright (c) PORTB 2026
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.sophisticatedcore;

import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;

/**
 * Adapts Sophisticated Core's transfer based inventory to BiggerStacks.
 * <p>
 * In 1.21.11 the slot limit of a normal storage slot is derived from the maximum stack size of the item stored in it
 * ({@code resource.getMaxStackSize()}), so an item modified by BiggerStacks already stacks up to the configured size.
 * The stack upgrade multiplier is expressed relative to the vanilla base slot limit of 64 and cancels out in
 * {@code getBaseCapacity}, which means that part of the old 1.20.1 transformer patches is no longer needed.
 * <p>
 * What is left is the vanilla base value itself: when a slot is queried with an <em>empty</em> resource (the
 * "general capacity" hint, used by {@code Slot#getMaxStackSize()} among others) Sophisticated returns the raw
 * {@code baseSlotLimit} (64, or 64 * multiplier with stack upgrades). That still has to be scaled to the configured
 * maximum stack size, otherwise third party integrations keep seeing a slot limit of 64.
 * <p>
 * The remaining hooks only guard against integer overflow for very large configured stack sizes.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler", remap = false)
public abstract class InventoryHandlerMixin
{
    @Inject(method = "getBaseCapacity(Lnet/neoforged/neoforge/transfer/item/ItemResource;)I",
            at = @At("RETURN"),
            cancellable = true)
    private void biggerstacks$scaleBaseCapacity(ItemResource resource, CallbackInfoReturnable<Integer> cir)
    {
        int capacity = cir.getReturnValue();

        if (resource.isEmpty())
        {
            // Vanilla sophisticated: 64 for a normal slot, 64 * stack upgrade multiplier for an upgraded one.
            // Scale exactly like the vanilla container limits are scaled by BiggerStacks.
            long scaled = (long) capacity * StackSizeRules.getMaxStackSize() / 64L;
            cir.setReturnValue((int) Math.max(1L, Math.min(Integer.MAX_VALUE, scaled)));
        }
        else
        {
            int clamped = StackSizeRules.clampStackSize(capacity);
            if (clamped != capacity)
                cir.setReturnValue(clamped);
        }
    }

    @ModifyVariable(method = "setBaseSlotLimit(I)V", at = @At("HEAD"), argsOnly = true)
    private int biggerstacks$clampBaseSlotLimit(int baseSlotLimit)
    {
        // A stack upgrade can compute Integer.MAX_VALUE; clamping keeps the multiplier arithmetic (and the
        // tank/battery/pump capacities derived from it) inside a sane range.
        return StackSizeRules.clampStackSize(baseSlotLimit);
    }

    @Inject(method = "getInternalSlotLimit(I)I", at = @At("RETURN"), cancellable = true)
    private void biggerstacks$clampInternalSlotLimit(int slot, CallbackInfoReturnable<Integer> cir)
    {
        // This value is used by the comparator ("analog output") calculation together with the vanilla base of 64,
        // so it must stay 64 based; only clamp absurd stack upgrade values.
        int slotLimit = cir.getReturnValue();
        int clamped = StackSizeRules.clampStackSize(slotLimit);

        if (clamped != slotLimit)
            cir.setReturnValue(clamped);
    }
}
