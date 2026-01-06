/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.ae2;

import appeng.client.gui.AEBaseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import portb.biggerstacks.util.SlotLimitHelper;

@Mixin(AEBaseScreen.class)
public class AEBaseScreenMixin
{
    @ModifyConstant(method = "checkHotbarKeyPressed", constant = @Constant(intValue = 64))
    private int increaseStackLimit(int val)
    {
        return SlotLimitHelper.getNewStackSize();
    }
}
