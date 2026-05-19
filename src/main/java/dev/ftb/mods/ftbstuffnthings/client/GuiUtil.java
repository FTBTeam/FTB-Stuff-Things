package dev.ftb.mods.ftbstuffnthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class GuiUtil {
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    public static void drawFluid(GuiGraphicsExtractor graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, int capacity) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite fluidStillSprite = model.stillMaterial().sprite();
        int tintColor = 0xFF000000 | (model.fluidTintSource() == null ? 0xFFFFF : model.fluidTintSource().color(fluid.defaultFluidState().createLegacyBlock()));

        int scaledAmount = capacity == 0 ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / capacity;
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;
                    // FIXME: tint color
                    drawFluidTexture(graphics, x, y, fluidStillSprite, maskTop, maskRight, tintColor);
                }
            }
        }
    }

    private static void drawFluidTexture(GuiGraphicsExtractor graphics, int xCoord, int yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, int color) {
        float uMin = textureSprite.getU0();
        float vMin = textureSprite.getV0();
        float uMax0 = textureSprite.getU1();
        float vMax0 = textureSprite.getV1();
        float uMax = uMax0 - maskRight / 16.0f * (uMax0 - uMin);
        float vMax = vMax0 - maskTop / 16.0f * (vMax0 - vMin);

        graphics.blit(textureSprite.atlasLocation(),
                xCoord, yCoord + 16, xCoord + 16 - maskRight, yCoord + 16 - maskTop,
                uMin, uMax, vMin, vMax
        );
    }

    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8  & 0xff;
        res[3] = color       & 0xff;
        return res;
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
