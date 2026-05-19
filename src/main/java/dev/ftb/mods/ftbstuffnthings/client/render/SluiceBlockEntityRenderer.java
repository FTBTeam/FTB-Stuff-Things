package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.render.state.SluiceRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SluiceBlockEntityRenderer implements BlockEntityRenderer<SluiceBlockEntity, SluiceRenderState> {
    private final ItemModelResolver itemModelResolver;

    public SluiceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AABB getRenderBoundingBox(SluiceBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1);
    }

    @Override
    public SluiceRenderState createRenderState() {
        return new SluiceRenderState();
    }

    @Override
    public void extractRenderState(SluiceBlockEntity blockEntity, SluiceRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.fluidState = blockEntity.getFluidTank().getResource(0).getFluid().defaultFluidState();
        state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.time = blockEntity.getProcessingTime();
        state.progress = blockEntity.getProcessingProgress();
        itemModelResolver.updateForTopItem(state.itemState, blockEntity.getDisplayedItem(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        state.fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state.fluidState);
        state.fluidTint = 0xFF000000 | (state.fluidModel.fluidTintSource() == null ?
                0xFFFFF :
                state.fluidModel.fluidTintSource().colorInWorld(state.fluidState, state.fluidState.createLegacyBlock(),
                        (ClientLevel)blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    @Override
    public void submit(SluiceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.fluidState.isEmpty()) {
            submitFluid(state, poseStack, submitNodeCollector);
        }
        if (state.time != 0) {
            submitItem(state, poseStack, submitNodeCollector);
        }
    }

    private static void submitFluid(SluiceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        TextureAtlasSprite sprite = state.fluidModel.flowingMaterial().sprite();
        int color = state.fluidTint;
        int light = state.lightCoords;
        var overlay = OverlayTexture.NO_OVERLAY;

        float y1 = .5F;

        float u0top = sprite.getU(3F / 16F);
        float v0top = sprite.getV(3F / 16F);
        float u1top = sprite.getU(13F / 16F);
        float v1top = sprite.getV(13F / 16F);

        Direction facing = state.facing;

        poseStack.pushPose();
        poseStack.translate(.5, 0, .5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(Sheets.BLOCKS_MAPPER.sheet()), (pose, builder) -> {
            builder.addVertex(pose, -.38F, y1, -.39F).setColor(color).setUv(u0top, v0top).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            builder.addVertex(pose, -.38F, .13F, .45F).setColor(color).setUv(u0top, v1top).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            builder.addVertex(pose, .38F, .13F, .45F).setColor(color).setUv(u1top, v1top).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
            builder.addVertex(pose, .38F, y1, -.39F).setColor(color).setUv(u1top, v0top).setOverlay(overlay).setLight(light).setNormal(0F, 1F, 0F);
        });

        poseStack.popPose();

        // Second block
        poseStack.pushPose();
        poseStack.translate(0, -.87F, 0);
        poseStack.translate((facing.getAxisDirection() == Direction.AxisDirection.POSITIVE && facing.getAxis() == Direction.Axis.X) || (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE && facing.getAxis() == Direction.Axis.Z)
                ? 1F : 0, 0, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1F : 0);

        if (facing.getAxis() == Direction.Axis.Z) {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getAxisDirection() != Direction.AxisDirection.POSITIVE ? 180 : 0));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 90 : -90));
        }

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(Sheets.BLOCKS_MAPPER.sheet()), (pose, builder) -> {
            builder.addVertex(pose, .1F, 1F, 0F).setColor(color).setUv(u0top, v0top).setOverlay(overlay).setLight(light).setNormal(pose, 0F, 1F, 0F);
            builder.addVertex(pose, .1F, 1F, 1F).setColor(color).setUv(u0top, v1top).setOverlay(overlay).setLight(light).setNormal(pose, 0F, 1F, 0F);
            builder.addVertex(pose, .9F, 1F, 1F).setColor(color).setUv(u1top, v1top).setOverlay(overlay).setLight(light).setNormal(pose,0F, 1F, 0F);
            builder.addVertex(pose, .9F, 1F, 0F).setColor(color).setUv(u1top, v0top).setOverlay(overlay).setLight(light).setNormal(pose, 0F, 1F, 0F);
        });

        poseStack.popPose();
    }

    private static void submitItem(SluiceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        int progress = (state.progress * 100) / Math.max(1, state.time);
        float offset = state.progress < 0 ? 0 : progress;

        poseStack.pushPose();
        poseStack.translate(.5F, .85F - (offset / 250F), .5F);

        state.itemState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

}
