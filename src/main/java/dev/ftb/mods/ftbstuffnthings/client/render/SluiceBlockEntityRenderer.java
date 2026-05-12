package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.render.state.SluiceRenderState;
import net.minecraft.client.Minecraft;
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
    }

    @Override
    public void submit(SluiceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        submitFluid(state, poseStack, submitNodeCollector);

        if (state.time != 0) {
            submitItem(state, poseStack, submitNodeCollector);
        }
    }

    private static void submitFluid(SluiceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state.fluidState);
        TextureAtlasSprite sprite = model.flowingMaterial().sprite();
        int color = 0xFF000000 | (model.fluidTintSource() == null ? 0xFFFFF : model.fluidTintSource().color(state.fluidState.createLegacyBlock()));
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

        float v = state.facing.toYRot();
        poseStack.pushPose();
        poseStack.translate(.5F, .85F - (offset / 250F), .5F);
        poseStack.scale(1.4F, 1.4F, 1.4F);
        poseStack.mulPose(Axis.YN.rotationDegrees(45 + v));

        state.itemState.submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

//    @Override
//    public void render(SluiceBlockEntity sluice, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int otherlight) {
//        FluidStack fluid = sluice.getFluidTank().getFluid();
//        if (!fluid.isEmpty()) {
//            this.renderFluid(sluice, fluid, matrix, renderer, light, otherlight);
//        }
//
//        if (sluice.getProcessingTime() == 0) {
//            return;
//        }
//        ItemStack resource = sluice.getDisplayedItem();
//        if (resource.isEmpty()) {
//            return;
//        }
//
//        int progress = (sluice.getProcessingProgress() * 100) / Math.max(1, sluice.getProcessingTime());
//        float offset = sluice.getProcessingProgress() < 0 ? 0 : progress;
//
//        float v = sluice.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
//        matrix.pushPose();
//        matrix.translate(.5F, .85F - (offset / 250F), .5F);
//        matrix.scale(1.4F, 1.4F, 1.4F);
//        matrix.mulPose(Axis.YN.rotationDegrees(45 + v));
//
//        Minecraft.getInstance().getItemRenderer().renderStatic(
//                resource, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, light, otherlight, matrix,renderer, Minecraft.getInstance().level, 0
//        );
//
//        matrix.popPose();
//    }
//
//    // Lats code from jars (simpler this way)
//    private void renderFluid(SluiceBlockEntity te, FluidStack fluid, PoseStack poseStack, MultiBufferSource renderer, int light, int otherlight) {
////        VertexConsumer builder = renderer.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
////
////        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid.getFluid());
////        Identifier texture = renderProps.getFlowingTexture(fluid);
////        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
//
//        var state = te.getFluidTank().getResource(0).getFluid().defaultFluidState();
//        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
//        TextureAtlasSprite sprite = model.stillMaterial().sprite();
//        int color = 0xFF000000 | (model.fluidTintSource() == null ? 0xFFFFF : model.fluidTintSource().color(state.createLegacyBlock()));
//
////        int[] cols = decomposeColor(renderProps.getTintColor(fluid));
////        float r = cols[1] / 255F;
////        float g = cols[2] / 255F;
////        float b = cols[3] / 255F;
////        float a = cols[0] / 255F;
//
//        float y1 = .5F;
//
//        float u0top = sprite.getU(3F / 16F);
//        float v0top = sprite.getV(3F / 16F);
//        float u1top = sprite.getU(13F / 16F);
//        float v1top = sprite.getV(13F / 16F);
//
//        Direction facing = te.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
//
//        poseStack.pushPose();
//        poseStack.translate(.5, 0, .5);
//        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
//
//        PoseStack.Pose last = poseStack.last();
//
//        Matrix4f matrix4 = last.pose();
//        builder.addVertex(matrix4, -.38F, y1, -.39F).setColor(r, g, b, a).setUv(u0top, v0top).setOverlay(otherlight).setLight(light).setNormal(0F, 1F, 0F);
//        builder.addVertex(matrix4, -.38F, .13F, .45F).setColor(r, g, b, a).setUv(u0top, v1top).setOverlay(otherlight).setLight(light).setNormal(0F, 1F, 0F);
//        builder.addVertex(matrix4, .38F, .13F, .45F).setColor(r, g, b, a).setUv(u1top, v1top).setOverlay(otherlight).setLight(light).setNormal(0F, 1F, 0F);
//        builder.addVertex(matrix4, .38F, y1, -.39F).setColor(r, g, b, a).setUv(u1top, v0top).setOverlay(otherlight).setLight(light).setNormal(0F, 1F, 0F);
//        poseStack.popPose();
//
//        // Second block fluid
//        poseStack.pushPose();
//        poseStack.translate(0, -.87F, 0);
////        matrix.translate(facing.getStepX(), 0, facing.getStepZ());
//        poseStack.translate((facing.getAxisDirection() == Direction.AxisDirection.POSITIVE && facing.getAxis() == Direction.Axis.X) || (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE && facing.getAxis() == Direction.Axis.Z)
//                ? 1F : 0, 0, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1F : 0);
//
//        if (facing.getAxis() == Direction.Axis.Z) {
//            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getAxisDirection() != Direction.AxisDirection.POSITIVE ? 180 : 0));
//        } else {
//            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 90 : -90));
//        }
//
//        last = poseStack.last();
//        matrix4 = last.pose();
//        builder.addVertex(matrix4, .1F, 1F, 0F).setColor(r, g, b, a).setUv(u0top, v0top).setOverlay(otherlight).setLight(light).setNormal(last, 0F, 1F, 0F);
//        builder.addVertex(matrix4, .1F, 1F, 1F).setColor(r, g, b, a).setUv(u0top, v1top).setOverlay(otherlight).setLight(light).setNormal(last, 0F, 1F, 0F);
//        builder.addVertex(matrix4, .9F, 1F, 1F).setColor(r, g, b, a).setUv(u1top, v1top).setOverlay(otherlight).setLight(light).setNormal(last, 0F, 1F, 0F);
//        builder.addVertex(matrix4, .9F, 1F, 0F).setColor(r, g, b, a).setUv(u1top, v0top).setOverlay(otherlight).setLight(light).setNormal(last, 0F, 1F, 0F);
//        poseStack.popPose();
//    }
//
//    public static int[] decomposeColor(int color) {
//        int[] res = new int[4];
//        res[0] = color >> 24 & 0xff;
//        res[1] = color >> 16 & 0xff;
//        res[2] = color >> 8  & 0xff;
//        res[3] = color       & 0xff;
//        return res;
//    }

}
