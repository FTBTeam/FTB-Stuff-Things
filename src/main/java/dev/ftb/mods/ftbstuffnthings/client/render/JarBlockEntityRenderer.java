package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.JarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import dev.ftb.mods.ftbstuffnthings.client.render.state.FluidRenderData;
import dev.ftb.mods.ftbstuffnthings.client.render.state.JarRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class JarBlockEntityRenderer implements BlockEntityRenderer<JarBlockEntity, JarRenderState> {
    public static final RenderUtil.FluidBounds JAR_BOUNDS = new RenderUtil.FluidBounds(3.2f / 16f, 0.9f / 16f, 11f / 16f);

    public JarBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredContext) {
    }

//    @Override
//    public void render(JarBlockEntity jar, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
//        RenderUtil.renderFluid3d(JAR_BOUNDS, jar.getTank(), bufferSource, poseStack.last().pose(), packedLight, packedOverlay);
//    }

    @Override
    public JarRenderState createRenderState() {
        return new JarRenderState();
    }

    @Override
    public void extractRenderState(JarBlockEntity blockEntity, JarRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.fluidRenderData = FluidRenderData.fromTank(JAR_BOUNDS, blockEntity.getTank());
    }

    @Override
    public void submit(JarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderUtil.renderFluid3d(state.fluidRenderData, submitNodeCollector, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }
}
