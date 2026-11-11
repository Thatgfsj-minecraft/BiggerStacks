/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;

public class EditBoxWithADifferentBorderColour extends EditBox
{
    private static final int BORDER_COLOUR = 0xff_c7c7c7;

    public EditBoxWithADifferentBorderColour(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
    }

    public EditBoxWithADifferentBorderColour(Font pFont, int pX, int pY, int pWidth, int pHeight, @Nullable EditBox p_94111_, Component pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, p_94111_, pMessage);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (!this.isVisible() || !this.isBordered())
            return;

        int borderColour = this.isFocused() ? -1 : BORDER_COLOUR;
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();

        graphics.fill(left - 1, top - 1, right + 1, top, borderColour);
        graphics.fill(left - 1, bottom, right + 1, bottom + 1, borderColour);
        graphics.fill(left - 1, top, left, bottom, borderColour);
        graphics.fill(right, top, right + 1, bottom, borderColour);
    }
}
