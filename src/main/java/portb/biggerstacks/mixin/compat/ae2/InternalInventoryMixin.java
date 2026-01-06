/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.ae2;

import appeng.api.inventories.InternalInventory;
import org.spongepowered.asm.mixin.Mixin;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(InternalInventory.class)
public interface InternalInventoryMixin extends InternalInventory
{
    /**
     * Fixes shift-clicking from the crafting output slot making stacks of 64 and ignoring the new stack limit
     */
    @Override
    default int getSlotLimit(int slot)
    {
        return SlotLimitHelper.getNewStackSize();
    }
}
