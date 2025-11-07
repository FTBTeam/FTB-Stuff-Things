package dev.ftb.mods.ftbstuffnthings.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.woodbasin.WoodenBasinBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BasinBlockEntityRenderer implements BlockEntityRenderer<WoodenBasinBlockEntity> {
    public static final RenderUtil.FluidBounds BASIN_BOUNDS = new RenderUtil.FluidBounds(2f / 16f, 4f / 16f, 10.5f / 16f);

    public BasinBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(WoodenBasinBlockEntity basin, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderUtil.renderFluid3d(BASIN_BOUNDS, basin.getTank(), bufferSource, poseStack.last().pose(), packedLight, packedOverlay);
    }
}
