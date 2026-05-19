package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.BaseResourceGenBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.render.state.ResourcegenRenderState;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ResourcegenBlockEntityRenderer implements BlockEntityRenderer<BaseResourceGenBlockEntity, ResourcegenRenderState> {
    private static final float TEX_ANIM = 10f;
    private static final float SPEED_FACTOR = 3f;  // Increase this value to slow down the animation
    private static final int TEXTURE_OFFSET = 2;   // Adjust as needed
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver resolver;

    public ResourcegenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.blockModelResolver();
    }

    @Override
    public ResourcegenRenderState createRenderState() {
        return new ResourcegenRenderState();
    }

    @Override
    public void extractRenderState(BaseResourceGenBlockEntity blockEntity, ResourcegenRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (blockEntity.getBlockState().getValue(BlockStateProperties.ENABLED)) {
            state.blockState = blockEntity.generatedItem() instanceof BlockItem bi ?
                    bi.getBlock().defaultBlockState() :
                    null;
            var tick = blockEntity.getLevel().getGameTime();
            int time = (int) ((tick / SPEED_FACTOR) % TEX_ANIM);
            state.time = (time + TEXTURE_OFFSET) % (int) TEX_ANIM;
        } else {
            state.blockState = null;
        }
        resolver.update(state.renderState, Objects.requireNonNullElse(state.blockState, Blocks.AIR.defaultBlockState()), DISPLAY_CONTEXT);
    }

    @Override
    public void submit(ResourcegenRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.blockState != null) {
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state.blockState);

            poseStack.pushPose();
            poseStack.translate(0.375, 0.125, 0.375);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            state.renderState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            submitNodeCollector.submitBreakingBlockModel(poseStack, model, 0L, state.time);
            poseStack.popPose();
        }
    }
}
