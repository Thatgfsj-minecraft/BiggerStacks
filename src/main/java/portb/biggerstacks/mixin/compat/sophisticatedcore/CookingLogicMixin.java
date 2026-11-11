/*
 * Copyright (c) PORTB 2026
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.sophisticatedcore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import portb.biggerstacks.util.StackSizeHelper;

/**
 * Sophisticated Core's cooking upgrade still compares the output stack against the vanilla maximum stack size of 99
 * that was introduced in 1.21.11. BiggerStacks raises item stack sizes above that value, so without this patch a
 * furnace/smoker/blast furnace upgrade stops smelting once the output stack reaches 99 items.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.CookingLogic", remap = false)
public abstract class CookingLogicMixin
{
    @ModifyConstant(method = "canSmelt", constant = @Constant(intValue = 99), require = 0)
    private int biggerstacks$scaleCookingOutputLimit(int vanillaLimit)
    {
        return StackSizeHelper.scaleContainerLimit(vanillaLimit);
    }
}
