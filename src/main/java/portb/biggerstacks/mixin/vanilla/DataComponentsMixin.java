/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.UnaryOperator;

@Mixin(DataComponents.class)
public class DataComponentsMixin
{
    @ModifyArg(method = "<clinit>",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/core/component/DataComponents;register(Ljava/lang/String;Ljava/util/function/UnaryOperator;)Lnet/minecraft/core/component/DataComponentType;",
                        ordinal = 1),
               index = 1,
               require = 0)
    private static UnaryOperator<DataComponentType.Builder<Integer>> allowLargeComponentStackSizes(
            UnaryOperator<DataComponentType.Builder<Integer>> original)
    {
        return builder -> {
            DataComponentType.Builder<Integer> configured = original.apply(builder);
            return configured.persistent(ExtraCodecs.intRange(1, Integer.MAX_VALUE))
                             .networkSynchronized(ByteBufCodecs.VAR_INT);
        };
    }
}
