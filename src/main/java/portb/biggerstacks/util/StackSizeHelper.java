/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;

public class StackSizeHelper
{
    /**
     * Scales the slot limit based on the original value
     */
    public static void scaleSlotLimit(CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        int original = callbackInfoReturnable.getReturnValue();
        callbackInfoReturnable.setReturnValue(scaleSlotLimit(original));
        callbackInfoReturnable.cancel();
    }
    
    /**
     * Scales the slot limit based on the original limit
     *
     * @param original The original stack size
     * @return 64 if stack size has been lowered (to account for possible blacklisted/whitelisted items) or the scaled stack size. If the original slot has a limit of 1, 1 is returned.
     */
    public static int scaleSlotLimit(int original)
    {
        int newStackSize = StackSizeRules.getMaxStackSize();
        int baseline     = StackSizeRules.getBaselineStackSize();
        
        //don't scale slots that are only meant to hold a single item
        if (original == 1)
            return 1;
        else if (newStackSize < baseline) //can't trust original to be the actual original value
            return baseline;

        long scaled = (long) original * newStackSize / baseline;
        scaled = Math.max(original, scaled);
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }
    
    /**
     * Increases slot limit without regard for the original size
     *
     * @return The new stack size with a minimum value of 64
     */
    public static int getNewStackSize()
    {
        return Math.max(StackSizeRules.getMaxStackSize(), StackSizeRules.getBaselineStackSize());
    }

    /**
     * Scales a vanilla container limit (99 in 1.21.11, 64 in older versions) up to the configured maximum.
     * Limits that are intentionally small (e.g. single item slots) are left untouched.
     */
    public static int scaleContainerLimit(int original)
    {
        if (original == 1)
            return 1;

        if (original == 64 || original == 99)
            return getNewStackSize();

        return original;
    }

    public static void scaleTransferRate(CallbackInfoReturnable<Integer> callbackInfoReturnable, boolean respectSingle)
    {
        int original = callbackInfoReturnable.getReturnValue();
        callbackInfoReturnable.setReturnValue(scaleTransferRate(original, respectSingle));
        callbackInfoReturnable.cancel();
    }

    public static int scaleTransferRate(int originalRate, boolean respectSingle)
    {
        if (originalRate == 1 && respectSingle)
            return 1;
        
        long scaled = (long) originalRate * StackSizeRules.getMaxStackSize() / StackSizeRules.getBaselineStackSize();
        scaled = Math.max(1, scaled);
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }
    
    public static int increaseTransferRate(int originalRate)
    {
        if (originalRate == 1)
            return 1;
        
        return Math.max(1, StackSizeRules.getMaxStackSize());
    }
}
