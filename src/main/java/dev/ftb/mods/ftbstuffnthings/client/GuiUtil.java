package dev.ftb.mods.ftbstuffnthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class GuiUtil {
    public static void drawFluid(GuiGraphicsExtractor graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, int capacity) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite fluidStillSprite = model.stillMaterial().sprite();
        int tintColor = model.fluidTintSource() == null ?
                0xFFFFFFFF :
                ARGB.opaque(model.fluidTintSource().colorAsStack(fluidStack));

        int scaledAmount = capacity == 0 ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / capacity;
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());
        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) {
            yStart -= (bounds.getHeight() - scaledAmount);
        }

        graphics.blitTiledSprite(RenderPipelines.GUI_TEXTURED, fluidStillSprite,
                bounds.getX(), yStart - scaledAmount, bounds.getWidth(), scaledAmount,
                0, 0, 16, 16, 16, 16, tintColor);
    }

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[1] = (color       & 0xff) / 255f;
        res[2] = (color >> 8  & 0xff) / 255f;
        res[3] = (color >> 16 & 0xff) / 255f;
        return res;
    }
}
