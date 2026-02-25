/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.ItemProperties;
import portb.configlib.TagAccessor;

import java.util.HashMap;
import java.util.Map;

public class ItemStackSizeHelper
{
    private final static Logger LOGGER = LogUtils.getLogger();
    private static final Map<Item, Integer> itemStackSizeCache = new HashMap<>();

    public static void applyStackSizeToItem(Item item, CallbackInfoReturnable<Integer> returnInfo)
    {
        // Avoid getting a default instance if we already know the stack size for the item
        if (itemStackSizeCache.containsKey(item))
        {
            var cached = itemStackSizeCache.get(item);

            // If the cached value is null, we must use the original return value See the ifPresentOrElse below
            if (cached != null)
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(cached);
            }

            return;
        }

        // If we don't know, then get the default item instance and calculate the stack size
        applyStackSizeToItem(item.getDefaultInstance(), item, returnInfo);
    }

    public static void applyStackSizeToItem(ItemStack itemstack, CallbackInfoReturnable<Integer> returnInfo)
    {
        applyStackSizeToItem(itemstack, null, returnInfo);
    }

    public static void applyStackSizeToItem(ItemStack itemstack, Item item, CallbackInfoReturnable<Integer> returnInfo)
    {
        if (StackSizeRules.getRuleSet() != null)
        {
            if (item == null)
            {
                item = itemstack.getItem();
            }

            // Check if we already determined what size this item has, to avoid doing expensive lookups and shit
            if (itemStackSizeCache.containsKey(item))
            {
                var cached = itemStackSizeCache.get(item);

                // If the cached value is null, we must use the original return value. See the ifPresentOrElse below
                if (cached != null)
                {
                    returnInfo.cancel();
                    returnInfo.setReturnValue(cached);
                }

                return;
            }

            var itemKey = ForgeRegistries.ITEMS.getKey(item);
            final var itemFinal = item; // Needed because java wants a final reference to pass into the lambda

            StackSizeRules.getRuleSet()
                    .determineStackSizeForItem(
                        new ItemProperties(
                                itemKey.getNamespace(),
                                itemKey.toString(),
                                returnInfo.getReturnValue(),
                                item.isEdible(),
                                (item instanceof BlockItem),
                                item.canBeDepleted(),
                                item instanceof BucketItem,
                                new TagAccessorImpl(itemstack),
                                item.getClass()
                        )
                    )
                    .ifPresentOrElse(
                            (stackSize) ->
                            {
                                // If we found a match, put it in the cache
                                returnInfo.cancel();
                                returnInfo.setReturnValue(stackSize);
                                itemStackSizeCache.put(itemFinal, stackSize);
                            },
                            // If there was no matching rule for this item, indicate that we should use the original
                            // return value instead of using a cached value. This is intended to prevent items, which
                            // somehow use their NBT to determine what their stack size should be, from having their
                            // stack size for every 'item' (again, indicated by their NBT) incorrectly set to whatever
                            // value is seen first. Since this mod does not support matching items by NBT, this is okay.
                            // See https://codeberg.org/PORTB/BiggerStacks/issues/82#issuecomment-10939013
                            () -> itemStackSizeCache.put(itemFinal, null)
                    );
        }
        else
        {
            LOGGER.debug("Stack size ruleset is somehow null, using fallback logic. Called from {}", CallingClassUtil.getCallerClassName());

            if (returnInfo.getReturnValue() > 1)
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(SlotLimitHelper.getNewStackSize());
            }
        }
    }

    public static void resetCache()
    {
        itemStackSizeCache.clear();
    }

    private record TagAccessorImpl(ItemStack item) implements TagAccessor
    {
        @Override
        public boolean doesItemHaveTag(String tag)
        {
            return item.is(new TagKey<>(Registries.ITEM, new ResourceLocation(tag)));
        }
    }
}