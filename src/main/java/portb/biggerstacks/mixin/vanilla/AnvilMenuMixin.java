/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin
{
    /**
     * Stops stacked enchanted books from being deleted when taking the result out of an anvil:
     * vanilla clears the second input slot, we only consume one book from the stack.
     * <p>
     * Uses MixinExtras {@code @WrapOperation} instead of a plain {@code @Redirect}: with Mixin 0.8.7 the
     * {@code @Redirect.at} member became an array, which crashes MixinExtras 0.5.0 (bundled by older
     * Fabric Loader versions such as 0.18.4) while it scans the mixin.
     */
    @WrapOperation(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3))
    void fixStackedEnchantedBooksBeingDeleted(Container inputSlots, int i, ItemStack itemStack, Operation<Void> original)
    {
        inputSlots.getItem(1).shrink(1);
    }
}
