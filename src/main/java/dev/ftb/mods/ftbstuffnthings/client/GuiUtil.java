package dev.ftb.mods.ftbstuffnthings.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class GuiUtil {
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;

    public static void drawFluid(GuiGraphics graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
        Identifier fluidStill = Objects.requireNonNullElse(renderProps.getStillTexture(fluidStack), MissingTextureAtlasSprite.getLocation());
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int[] cols = decomposeColor(renderProps.getTintColor(fluidStack));

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(graphics, x, y, fluidStillSprite, maskTop, maskRight, 100, cols);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    private static void drawFluidTexture(GuiGraphics graphics, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, float zLevel, int[] cols) {
        float uMin = textureSprite.getU0();
        float vMin = textureSprite.getV0();
        float uMax0 = textureSprite.getU1();
        float vMax0 = textureSprite.getV1();
        float uMax = uMax0 - maskRight / 16.0f * (uMax0 - uMin);
        float vMax = vMax0 - maskTop / 16.0f * (vMax0 - vMin);

        Matrix4f posMat = graphics.pose().last().pose();

        drawWithTesselator(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, b -> {
            b.addVertex(posMat, xCoord, yCoord + 16, zLevel)
                    .setUv(uMin, vMax)
                    .setColor(cols[1], cols[2], cols[3], cols[0]);
            b.addVertex(posMat,xCoord + 16 - maskRight, yCoord + 16, zLevel)
                    .setUv(uMax, vMax)
                    .setColor(cols[1], cols[2], cols[3], cols[0]);
            b.addVertex(posMat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel)
                    .setUv(uMax, vMin)
                    .setColor(cols[1], cols[2], cols[3], cols[0]);
            b.addVertex(posMat, xCoord, yCoord + maskTop, zLevel)
                    .setUv(uMin, vMin)
                    .setColor(cols[1], cols[2], cols[3], cols[0]);
        });
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
        res[1] = (color >> 16 & 0xff) / 255f;
        res[2] = (color >> 8  & 0xff) / 255f;
        res[3] = (color       & 0xff) / 255f;
        return res;
    }

    public static void drawWithTesselator(VertexFormat.Mode mode, VertexFormat format, Consumer<BufferBuilder> consumer) {
        BufferBuilder builder = Tesselator.getInstance().begin(mode, format);
        consumer.accept(builder);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }
}
