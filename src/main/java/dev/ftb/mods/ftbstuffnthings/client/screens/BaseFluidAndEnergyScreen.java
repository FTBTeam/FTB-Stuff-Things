package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.client.GuiUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public abstract class BaseFluidAndEnergyScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final int fluidXOffset;
    private final int progressXOffset;
    private final Identifier texture;

    public BaseFluidAndEnergyScreen(T menu, Inventory inventory, Component title, int fluidXOffset, int progressXOffset, Identifier texture) {
        super(menu, inventory, title);
        this.fluidXOffset = fluidXOffset;
        this.progressXOffset = progressXOffset;
        this.texture = texture;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (mouseX > leftPos + fluidXOffset && mouseX < leftPos + fluidXOffset + 19 && mouseY > topPos + 3 && mouseY < topPos + 5 + 65) {
            List<Component> tooltip = getFluidStack().isEmpty() ?
                    List.of(Component.translatable("ftblibrary.empty")) :
                    List.of(getFluidStack().getHoverName(),
                            Component.literal(getFluidStack().getAmount() + " / " + getFluidCapacity() + " mB"));
            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
        } else if (mouseX > leftPos + 166 && mouseX < leftPos + 174 && mouseY > topPos + 3 && mouseY < topPos + 5 + 65) {
            MutableComponent energyText = Component.literal(getEnergyAmount() + " / " + getEnergyCapacity() + " FE");
            graphics.setTooltipForNextFrame(font, energyText, mouseX, mouseY);
        } else {
            super.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos,
                0, 0, imageWidth, imageHeight, 256, 256);

        if (getEnergyCapacity() > 0) {
            // Energy
            float x = (float) getEnergyAmount() / getEnergyCapacity();
            int energyHeight = (int) (x * 65);
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + imageWidth - 9, topPos + 4 + 65 - energyHeight,
                    197, 4 + 65 - energyHeight, 5, energyHeight, 256, 256);
        }

        if (getFluidCapacity() > 0) {
            // Fluid texture
            Rect2i bounds = new Rect2i(leftPos + fluidXOffset + 1, topPos + 4, 16, 65);
            GuiUtil.drawFluid(graphics, bounds, getFluidStack(), getFluidCapacity());

            // Fluid gauge
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + (fluidXOffset + 1), topPos + 6, 178, 3, 18, 67, 256, 256);
        }

        if (getProgressRequired() > 0) {
            // Finally, draw the progress bar
            float computedPercentage = (float) getProgress() / getProgressRequired() * 24;
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + progressXOffset, topPos + 28, 203, 0, (int) computedPercentage + 1, 16, 256, 256);
        }
    }

    public abstract int getEnergyAmount();
    public abstract int getEnergyCapacity();

    public abstract int getFluidCapacity();
    public abstract FluidStack getFluidStack();

    public abstract int getProgress();
    public abstract int getProgressRequired();
}
