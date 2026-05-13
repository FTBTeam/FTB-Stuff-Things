package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int w2 = (width - imageWidth) / 2;
        int h2 = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, w2, h2, 0, 0, imageWidth, ROWS * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, w2, h2 + ROWS * 18 + 17, 0, 126, imageWidth, 96, 256, 256);
    }
}
