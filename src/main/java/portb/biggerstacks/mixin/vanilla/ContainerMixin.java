/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.util.StackSizeHelper;

/**
 * Vanilla {@link Container#getMaxStackSize()} returns 99 in 1.21.11; raise it to the configured maximum.
 */
@Mixin(Container.class)
public interface ContainerMixin
{
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void biggerstacks$increaseContainerStackLimit(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(StackSizeHelper.scaleContainerLimit(cir.getReturnValue()));
    }
}
