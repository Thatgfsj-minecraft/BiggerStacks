/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import portb.biggerstacks.config.ClientConfig;

import java.text.DecimalFormat;

import static portb.biggerstacks.Constants.ONE_BILLION;
import static portb.biggerstacks.Constants.ONE_MILLION;
import static portb.biggerstacks.Constants.ONE_THOUSAND;

/**
 * Replaces the vanilla item count rendering so that counts above 1000 can be shortened (1.2K, 3.4M, ...)
 * and large counts are scaled down to fit the 16x16 item slot.
 * <p>
 * In Minecraft 1.21.9 the count rendering was extracted into {@code GuiGraphics#renderItemCount}; the old
 * {@code renderItemDecorations} override is no longer needed because the bar and cooldown are rendered separately.
 */
@Mixin(GuiGraphics.class)
public abstract class ItemRendererMixin
{
    private static final DecimalFormat BILLION_FORMAT  = new DecimalFormat("#.##B");
    private static final DecimalFormat MILLION_FORMAT  = new DecimalFormat("#.##M");
    private static final DecimalFormat THOUSAND_FORMAT = new DecimalFormat("#.##K");

    @Inject(method = "renderItemCount", at = @At("HEAD"), cancellable = true)
    private void biggerstacks$renderItemCount(Font font, ItemStack stack, int x, int y, String alternateCount, CallbackInfo ci)
    {
        if (stack.isEmpty())
            return;

        if (stack.getCount() == 1 && alternateCount == null)
        {
            //vanilla renders nothing either
            ci.cancel();
            return;
        }

        //counts that fit the vanilla layout and are not shortened are left alone
        if (stack.getCount() < ONE_THOUSAND && !ClientConfig.enableNumberShortening)
            return;

        String text = alternateCount != null ? alternateCount : getStringForBigStackCount(stack.getCount());

        double scale = calculateStringScale(font, text);
        if (scale >= 1.0 && !ClientConfig.enableNumberShortening)
            return;

        GuiGraphics graphics = (GuiGraphics) (Object) this;
        Matrix3x2fStack pose = graphics.pose();
        float scaledWidth = (float) (font.width(text) * scale);

        pose.pushMatrix();
        pose.translate(x + 17 - scaledWidth, y + 9);
        pose.scale((float) scale, (float) scale);
        graphics.drawString(font, text, 0, 0, 0xFFFFFF, true);
        pose.popMatrix();

        ci.cancel();
    }

    private static String getStringForBigStackCount(int count)
    {
        if (ClientConfig.enableNumberShortening)
        {
            double value = count;

            if (value >= ONE_BILLION)
                return BILLION_FORMAT.format(value / ONE_BILLION);
            else if (value >= ONE_MILLION)
                return MILLION_FORMAT.format(value / ONE_MILLION);
            else if (value > ONE_THOUSAND)
                return THOUSAND_FORMAT.format(value / ONE_THOUSAND);
        }

        return String.valueOf(count);
    }

    private static double calculateStringScale(Font font, String countString)
    {
        var width = font.width(countString);

        if (width < 16)
            return 1.0;
        else
            return 16.0 / width;
    }
}
