/*
 * Copyright (c) PORTB 2023
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.compat.modularrouters;

import me.desht.modularrouters.item.upgrade.StackUpgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.ServerConfig;
import portb.biggerstacks.util.ModularRoutersHelper;

@Mixin(StackUpgrade.class)
public class ModularRoutersStackUpgradeMixin
{
    @Inject(method = "getStackLimit", at = @At("RETURN"),
            remap = false, require = 0,
            cancellable = true)
    private void increaseStackLimit(int slot, CallbackInfoReturnable<Integer> returnInfo)
    {
        if (ServerConfig.get().increaseTransferRate.get())
        {
            returnInfo.cancel();
            returnInfo.setReturnValue(ModularRoutersHelper.getMaxStackUpgrades());
        }
    }
}
