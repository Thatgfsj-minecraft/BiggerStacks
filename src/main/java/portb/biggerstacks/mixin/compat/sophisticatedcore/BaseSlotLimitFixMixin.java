/*
 * Copyright (c) PORTB 2026
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.sophisticatedcore;

import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryHandler.class)
public class BaseSlotLimitFixMixin
{
    @Shadow
    @Final
    protected IStorageWrapper storageWrapper;

    @Redirect(method = "*", at = @At(value = "FIELD", target="Lnet/p3pp3rf1y/sophisticatedcore/inventory/InventoryHandler;baseSlotLimit:I", opcode = Opcodes.GETFIELD))
    private int biggerstacks_computeBaseStackLimit(InventoryHandler inventoryHandler)
    {
        /*
        At some point, somehow, sophisticatedcore started having issues with the stack size being changed after a backpack was opened and a stack upgrade was inserted into it. I don't know why this wasn't an issue before, but some mechanism that caches the InventoryHandler has probably been introduced which caused the server to use an out of date value for the baseSlotLimit. Meanwhile, the client has the correct version due to creating a new InventoryHandler every time the container is opened. This not only causes the old, incorrect stack limit to be used, but also creates a client/server mismatch where the client is using the new, correct baseSlotLimit while the server is not. Sophisticated stuff is a very, erm, sophisticated mod, and it would probably take me a long time to find why exactly this is happening, but this seems to fix the problem.
         */
        return StackUpgradeItem.getInventorySlotLimit(storageWrapper);
    }
}
