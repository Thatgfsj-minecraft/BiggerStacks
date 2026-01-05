/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.ItemProperties;
import portb.configlib.TagAccessor;

import java.util.HashMap;
import java.util.Map;

import static portb.biggerstacks.BiggerStacks.LOGGER;

public class ItemStackSizeHelper
{
    private static Map<Item, Integer> itemStackSizeCache = new HashMap<>();

    public static void applyStackSizeToItem(ItemStack itemstack, CallbackInfoReturnable<Integer> returnInfo)
    {
        if (StackSizeRules.getRuleSet() != null)
        {
            var item    = itemstack.getItem();

            // Check if we already determined what size this item has, to avoid doing expensive lookups and shit
            if (itemStackSizeCache.containsKey(item))
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(itemStackSizeCache.get(item));
                return;
            }

            var itemKey = ForgeRegistries.ITEMS.getKey(item);
            
            StackSizeRules.getRuleSet().determineStackSizeForItem(
                                  new ItemProperties(
                                          itemKey.getNamespace(),
                                          itemKey.toString(),
                                          "", //fixme this was never used anyway. remove it some time.
                                          returnInfo.getReturnValue(),
                                          item.isEdible(),
                                          (item instanceof BlockItem),
                                          item.canBeDepleted(),
                                          item instanceof BucketItem,
                                          new TagAccessorImpl(itemstack),
                                          item.getClass()
                                  )
                          )
                          .ifPresent((stackSize) -> {
                              returnInfo.cancel();
                              returnInfo.setReturnValue(stackSize);
                          });

            itemStackSizeCache.put(item, returnInfo.getReturnValue());
        }
        else
        {
            LOGGER.debug("Stack size ruleset is somehow null, using fallback logic. Called from " +
                                 CallingClassUtil.getCallerClassName());
            
            if (returnInfo.getReturnValue() > 1)
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(getNewStackSize());
            }
        }
    }

    /**
     * Increases slot limit without regard for the original size
     *
     * @return The new stack size with a minimum value of 64
     */
    public static int getNewStackSize()
    {
        return Math.max(StackSizeRules.getMaxStackSize(), 64);
    }

    private record TagAccessorImpl(ItemStack item) implements TagAccessor
    {
        @Override
        public boolean doesItemHaveTag(String tag)
        {
            return item.is(new TagKey<>(Registries.ITEM, new ResourceLocation(tag)));
        }
    }

    public static void resetCache()
    {
        itemStackSizeCache.clear();
    }

}