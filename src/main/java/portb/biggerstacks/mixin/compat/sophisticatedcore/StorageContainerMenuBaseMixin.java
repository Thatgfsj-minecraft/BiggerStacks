/*
 * Copyright (c) PORTB 2026
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.sophisticatedcore;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Limits how often a single shift click may repeat {@code quickMoveStack}.
 * <p>
 * With stack sizes far above the vanilla 99 a single stack can be moved in many small chunks. 1.21.11's
 * {@code doClick} repeats {@code quickMoveStack} until the source slot no longer changes; with a very large
 * configured stack size that loop can run long enough to stall the server thread. Returning an empty stack ends the
 * loop, just like vanilla does when the source slot runs empty.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase", remap = false)
public abstract class StorageContainerMenuBaseMixin
{
    @Unique
    private static final int BIGGERSTACKS_MAX_QUICK_MOVE_CALLS = 4096;

    @Unique
    private int biggerstacks$quickMoveCalls;

    @Unique
    private boolean biggerstacks$handlingClick;

    @Inject(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"))
    private void biggerstacks$beginClick(int slotId, int dragType, ClickType clickType, Player player, CallbackInfo ci)
    {
        biggerstacks$quickMoveCalls = 0;
        biggerstacks$handlingClick = true;
    }

    @Inject(method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("RETURN"))
    private void biggerstacks$endClick(int slotId, int dragType, ClickType clickType, Player player, CallbackInfo ci)
    {
        biggerstacks$handlingClick = false;
    }

    @Inject(method = "quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true)
    private void biggerstacks$capQuickMoveCalls(Player player, int index, CallbackInfoReturnable<ItemStack> cir)
    {
        if (!biggerstacks$handlingClick)
            return;

        if (++biggerstacks$quickMoveCalls > BIGGERSTACKS_MAX_QUICK_MOVE_CALLS)
            cir.setReturnValue(ItemStack.EMPTY);
    }
}
