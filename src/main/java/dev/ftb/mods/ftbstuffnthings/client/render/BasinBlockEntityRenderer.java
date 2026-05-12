package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.woodbasin.WoodenBasinBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import dev.ftb.mods.ftbstuffnthings.client.render.state.FluidRenderData;
import dev.ftb.mods.ftbstuffnthings.client.render.state.WoodenBasinRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class BasinBlockEntityRenderer implements BlockEntityRenderer<WoodenBasinBlockEntity, WoodenBasinRenderState> {
    public static final RenderUtil.FluidBounds BASIN_BOUNDS = new RenderUtil.FluidBounds(2f / 16f, 4f / 16f, 10.5f / 16f);

    public BasinBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredContext) {
    }

//    @Override
//    public void render(WoodenBasinBlockEntity basin, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
//        RenderUtil.renderFluid3d(BASIN_BOUNDS, basin.getTank(), bufferSource, poseStack.last().pose(), packedLight, packedOverlay);
//    }

    @Override
    public WoodenBasinRenderState createRenderState() {
        return new WoodenBasinRenderState();
    }

    @Override
    public void extractRenderState(WoodenBasinBlockEntity blockEntity, WoodenBasinRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.fluidRenderData = FluidRenderData.fromTank(BASIN_BOUNDS, blockEntity.getTank());
    }

    @Override
    public void submit(WoodenBasinRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderUtil.renderFluid3d(state.fluidRenderData, submitNodeCollector, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }
}
