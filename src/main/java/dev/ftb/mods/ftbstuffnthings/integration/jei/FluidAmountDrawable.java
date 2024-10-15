package dev.ftb.mods.ftbstuffnthings.integration.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public record FluidAmountDrawable(int amount) implements IDrawable {
    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        Font font = Minecraft.getInstance().font;
        String txt = amount >= 1000 ? amount / 1000.0 + "B" : amount + "mB";

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xOffset + 16 - font.width(txt) / 2f, yOffset + 16 - font.lineHeight / 2f, 0f);
        guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        guiGraphics.drawString(font, txt, 0, 0, 0xFFFFFFFF);
        guiGraphics.pose().popPose();
    }
}
