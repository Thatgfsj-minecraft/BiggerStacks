/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import portb.biggerstacks.net.ClientboundConfigureScreenOpenPacket;
import portb.biggerstacks.net.ServerboundCreateConfigTemplatePacket;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import portb.configlib.template.TemplateOptions;

import java.util.function.Consumer;

public class ConfigureScreen extends Screen
{
    private static final int WIDTH  = 220;
    private static final int HEIGHT = 200;

    private final TemplateOptions previousOptions;
    private final boolean         isAlreadyUsingCustomFile;
    private       MultiLineLabel  OVERWRITE_WARNING_LABEL;
    private       MultiLineLabel  STACK_LIMIT_HINT_LABEL;
    private       EditBox         potionsBox;
    private       EditBox         enchBooksBox;
    private       EditBox         normalItemsBox;
    private       Button          confirmButton;

    protected ConfigureScreen(ClientboundConfigureScreenOpenPacket options)
    {
        super(Component.literal("Configure stack size"));

        previousOptions = options;
        isAlreadyUsingCustomFile = options.isAlreadyUsingCustomFile();
    }

    public static void open(ClientboundConfigureScreenOpenPacket packet)
    {
        Minecraft.getInstance().setScreen(new ConfigureScreen(packet));
    }

    private static boolean isEditBoxInputValid(String inputString)
    {
        if (inputString.matches("[0-9]+"))
        {
            try
            {
                int value = Integer.parseInt(inputString);

                return value > 0 && value <= (Integer.MAX_VALUE / 2);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    protected void init()
    {
        int relX = (this.width - WIDTH) / 2, relY = (this.height - HEIGHT) / 2;

        int editBoxStartX = 120, editBoxStartY = 30;

        OVERWRITE_WARNING_LABEL = MultiLineLabel.create(font,
                Component.translatable("biggerstacks.overwrite.warn").withStyle(Style.EMPTY.withColor(0xffaaaa)),
                WIDTH
        );
        STACK_LIMIT_HINT_LABEL = MultiLineLabel.create(
                font,
                Component.translatable("biggerstacks.stacklimit.tip").withStyle(Style.EMPTY.withColor(0xfff2a7)),
                WIDTH - 20
        );

        normalItemsBox = new EditBoxWithADifferentBorderColour(font,
                relX + editBoxStartX, relY + editBoxStartY, 60, 20,
                Component.translatable("biggerstacks.normalbox.alt"));

        potionsBox = new EditBoxWithADifferentBorderColour(font,
                relX + editBoxStartX, relY + editBoxStartY + 30, 60, 20,
                Component.translatable("biggerstacks.potsbox.alt"));

        enchBooksBox = new EditBoxWithADifferentBorderColour(font,
                relX + editBoxStartX, relY + editBoxStartY + 60, 60, 20,
                Component.translatable("biggerstacks.enchbox.alt"));

        enchBooksBox.setValue(String.valueOf(previousOptions.getEnchBookLimit()));
        potionsBox.setValue(String.valueOf(previousOptions.getPotionStackLimit()));
        normalItemsBox.setValue(String.valueOf(previousOptions.getNormalStackLimit()));

        enchBooksBox.setResponder(verifyInputBoxNumber(enchBooksBox));
        potionsBox.setResponder(verifyInputBoxNumber(potionsBox));
        normalItemsBox.setResponder(verifyInputBoxNumber(normalItemsBox));

        confirmButton = Button.builder(Component.translatable("biggerstacks.save"), this::onConfirmButtonClicked)
                .bounds(relX + (WIDTH - 80) / 2, relY + HEIGHT - 30, 80, 20)
                .build();

        addRenderableWidget(normalItemsBox);
        addRenderableWidget(potionsBox);
        addRenderableWidget(enchBooksBox);
        addRenderableWidget(confirmButton);

        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int relX = (this.width - WIDTH) / 2, relY = (this.height - HEIGHT) / 2;

        graphics.drawCenteredString(font, Component.translatable("biggerstacks.config.title"), width / 2, relY + 10, 0xffffff);

        int centreOffset = (20 - font.lineHeight) / 2;
        int labelStartX = 20, labelStartY = 30;

        graphics.drawString(font, Component.translatable("biggerstacks.normalbox.label"),
                relX + labelStartX, centreOffset + relY + labelStartY, 0xffffff, false);

        graphics.drawString(font, Component.translatable("biggerstacks.potsbox.label"),
                relX + labelStartX, centreOffset + relY + labelStartY + 30, 0xffffff, false);

        graphics.drawString(font, Component.translatable("biggerstacks.enchbox.label"),
                relX + labelStartX, centreOffset + relY + labelStartY + 60, 0xffffff, false);

        STACK_LIMIT_HINT_LABEL.visitLines(TextAlignment.CENTER, width / 2, relY + 100, 9, graphics.textRenderer());

        if (isAlreadyUsingCustomFile)
            OVERWRITE_WARNING_LABEL.visitLines(TextAlignment.CENTER, width / 2, relY + 125, 9, graphics.textRenderer());

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        //panel + simple bevel, so the config screen does not need a texture
        graphics.fill(relX - 1, relY - 1, relX + WIDTH + 1, relY + HEIGHT + 1, 0xFF000000);
        graphics.fill(relX, relY, relX + WIDTH, relY + HEIGHT, 0xFFC6C6C6);
        graphics.fill(relX, relY, relX + WIDTH, relY + 1, 0xFFFFFFFF);
        graphics.fill(relX, relY + HEIGHT - 1, relX + WIDTH, relY + HEIGHT, 0xFF555555);
        graphics.fill(relX, relY, relX + 1, relY + HEIGHT, 0xFFFFFFFF);
        graphics.fill(relX + WIDTH - 1, relY, relX + WIDTH, relY + HEIGHT, 0xFF555555);
    }

    private void onConfirmButtonClicked(Button button)
    {
        if (isStateValid())
            submit();
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.key() == GLFW.GLFW_KEY_ENTER && isStateValid())
        {
            submit();
            return true;
        }

        return super.keyPressed(event);
    }

    private boolean isStateValid()
    {
        return isEditBoxInputValid(normalItemsBox.getValue())
                && isEditBoxInputValid(enchBooksBox.getValue())
                && isEditBoxInputValid(potionsBox.getValue());
    }

    private void submit()
    {
        ClientPacketDistributor.sendToServer(new ServerboundCreateConfigTemplatePacket(
                Integer.parseInt(normalItemsBox.getValue()),
                Integer.parseInt(potionsBox.getValue()),
                Integer.parseInt(enchBooksBox.getValue())
        ));

        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    private Consumer<String> verifyInputBoxNumber(EditBox editBox)
    {
        return inputString ->
        {
            if (isEditBoxInputValid(inputString))
            {
                editBox.setTextColor(0xffffff);
                confirmButton.active = true;
            }
            else
            {
                editBox.setTextColor(0xff0000);
                confirmButton.active = false;
            }
        };
    }
}
