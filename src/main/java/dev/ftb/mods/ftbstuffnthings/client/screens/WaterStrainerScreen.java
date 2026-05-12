package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class WaterStrainerScreen extends AbstractContainerScreen<WaterStrainerMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int ROWS = 3;

    public WaterStrainerScreen(WaterStrainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        inventoryLabelY = 74;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int w2 = (width - imageWidth) / 2;
        int h2 = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, w2, h2, 0, 0, imageWidth, ROWS * 18 + 17);
        guiGraphics.blit(TEXTURE, w2, h2 + ROWS * 18 + 17, 0, 126, imageWidth, 96);
    }
}
