package dev.ftb.mods.ftbstuffnthings.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.client.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.lwjgl.opengl.GL11;

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        if (mouseX > leftPos + fluidXOffset && mouseX < leftPos + fluidXOffset + 19 && mouseY > topPos + 3 && mouseY < topPos + 5 + 65) {
            List<Component> tooltip = getFluidStack().isEmpty() ?
                    List.of(Component.translatable("ftblibrary.empty")) :
                    List.of(getFluidStack().getHoverName(),
                            Component.literal(getFluidStack().getAmount() + " / " + getFluidCapacity() + " mB"));
            graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        if (mouseX > leftPos + 166 && mouseX < leftPos + 174 && mouseY > topPos + 3 && mouseY < topPos + 5 + 65) {
            MutableComponent energyText = Component.literal(getEnergyAmount() + " / " + getEnergyCapacity() + " FE");
            graphics.renderTooltip(font, energyText, mouseX, mouseY);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        PoseStack poseStack = graphics.pose();
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);

        if (getEnergyCapacity() > 0) {
            // Energy
            float x = (float) getEnergyAmount() / getEnergyCapacity();
            int energyHeight = (int) (x * 65);
            graphics.blit(texture, leftPos + imageWidth - 9, topPos + 4 + 65 - energyHeight, 197, 4 + 65 - energyHeight, 5, energyHeight);
            poseStack.popPose();
        }

        RenderSystem.disableBlend();

        if (getFluidCapacity() > 0) {
            // Fluid texture
            Rect2i bounds = new Rect2i(leftPos + fluidXOffset + 1, topPos + 4, 16, 65);
            GuiUtil.drawFluid(graphics, bounds, getFluidStack(), new FluidTank(getFluidCapacity()));

            // Fluid gauge
            poseStack.pushPose();
            poseStack.translate(0, 0, 101);
            graphics.blit(texture, leftPos + (fluidXOffset + 1), topPos + 6, 178, 3, 18, 67);
            poseStack.popPose();
        }

        if (getProgressRequired() > 0) {
            // Finally, draw the progress bar
            float computedPercentage = (float) getProgress() / getProgressRequired() * 24;
            graphics.blit(texture, leftPos + progressXOffset, topPos + 28, 203, 0, (int) computedPercentage + 1, 16);
        }
    }

    public abstract int getEnergyAmount();
    public abstract int getEnergyCapacity();

    public abstract int getFluidCapacity();
    public abstract FluidStack getFluidStack();

    public abstract int getProgress();
    public abstract int getProgressRequired();
}
