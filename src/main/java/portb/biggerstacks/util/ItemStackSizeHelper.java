/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.BiggerStacks;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.ItemProperties;
import portb.configlib.TagAccessor;

import java.util.HashMap;
import java.util.Map;

public class ItemStackSizeHelper
{
    private static final Map<Item, Integer> itemStackSizeCache = new HashMap<>();

    public static void applyStackSizeToItem(Item item, CallbackInfoReturnable<Integer> returnInfo)
    {
        // Avoid getting a default instance if we already know the stack size for the item
        if (itemStackSizeCache.containsKey(item))
        {
            var cached = itemStackSizeCache.get(item);

            // If the cached value is null, we must use the original return value
            if (cached != null)
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(cached);
            }

            return;
        }

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
                item = itemstack.getItem();

            // Check if we already determined what size this item has, to avoid doing expensive lookups
            if (itemStackSizeCache.containsKey(item))
            {
                var cached = itemStackSizeCache.get(item);

                if (cached != null)
                {
                    returnInfo.cancel();
                    returnInfo.setReturnValue(cached);
                }

                return;
            }

            var itemKey = BuiltInRegistries.ITEM.getKey(item);
            var namespace = itemKey != null ? itemKey.getNamespace() : Constants.MOD_ID;
            var identifier = itemKey != null ? itemKey.toString() : item.getClass().getName();
            boolean isEdible = item.components().has(DataComponents.FOOD);
            boolean isPlaceable = item instanceof BlockItem;
            boolean isDamageable = itemstack.isDamageableItem();
            boolean isBucket = item instanceof BucketItem;
            int currentMaxStack = returnInfo.getReturnValue();

            final var itemFinal = item; // Needed because java wants a final reference to pass into the lambda

            StackSizeRules.getRuleSet()
                    .determineStackSizeForItem(
                            new ItemProperties(
                                    namespace,
                                    identifier,
                                    currentMaxStack,
                                    isEdible,
                                    isPlaceable,
                                    isDamageable,
                                    isBucket,
                                    new TagAccessorImpl(itemstack),
                                    item.getClass()
                            )
                    )
                    .ifPresentOrElse(
                            (stackSize) ->
                            {
                                int clamped = StackSizeRules.clampStackSize(stackSize);
                                returnInfo.cancel();
                                returnInfo.setReturnValue(clamped);
                                itemStackSizeCache.put(itemFinal, clamped);
                            },
                            // No matching rule: remember that this item uses its default value, so that
                            // items whose stack size depends on their components/NBT are not cached wrongly.
                            () -> itemStackSizeCache.put(itemFinal, null)
                    );
        }
        else
        {
            BiggerStacks.LOGGER.debug("Stack size ruleset is somehow null, using fallback logic. Called from {}",
                    CallingClassUtil.getCallerClassName());

            if (returnInfo.getReturnValue() > 1)
            {
                returnInfo.cancel();
                returnInfo.setReturnValue(StackSizeHelper.getNewStackSize());
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
            Identifier tagId = Identifier.parse(tag);
            return item.is(TagKey.create(Registries.ITEM, tagId));
        }
    }
}
