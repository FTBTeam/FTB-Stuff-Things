package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.render.state.AutoHammerRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AutoHammerRenderer implements BlockEntityRenderer<AutoHammerBlockEntity, AutoHammerRenderState> {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver resolver;

    public AutoHammerRenderer(BlockEntityRendererProvider.Context context) {
        this.resolver = context.blockModelResolver();
    }

    @Override
    public AutoHammerRenderState createRenderState() {
        return new AutoHammerRenderState();
    }

    @Override
    public void extractRenderState(AutoHammerBlockEntity blockEntity, AutoHammerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (blockEntity.getProcessingStack().getItem() instanceof BlockItem bi) {
            state.hammerProgress = blockEntity.getDestroyStage();
            state.blockState = bi.getBlock().defaultBlockState();
            resolver.update(state.renderState, state.blockState, DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(AutoHammerRenderState autoHammerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.35, 0.3125, 0.35);
        poseStack.scale(0.3f, 0.3f - autoHammerRenderState.hammerProgress * 0.015f, 0.3f);
        autoHammerRenderState.renderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(autoHammerRenderState.blockState);
        submitNodeCollector.submitBreakingBlockModel(poseStack, model, 0L, autoHammerRenderState.hammerProgress);
        poseStack.popPose();
    }
}
