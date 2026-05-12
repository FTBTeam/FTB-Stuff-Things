package dev.ftb.mods.ftbstuffnthings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.client.render.state.FluidRenderData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class RenderUtil {

    public static void renderFluid3d(FluidRenderData info, SubmitNodeCollector collector, PoseStack poseStack, int packedLight, int packedOverlay) {
        if (info.empty()) {
            return;
        }

        FluidBounds fluidBounds = info.bounds();
        float baseY = fluidBounds.minY();

        for (FluidRenderData.Layer layer : info.fluidLayers()) {
            var state = layer.fluid().defaultFluidState();
            var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
            TextureAtlasSprite sprite = model.stillMaterial().sprite();
            int color = 0xFF000000 | (model.fluidTintSource() == null ? 0xFFFFF : model.fluidTintSource().color(state.createLegacyBlock()));

            float s0 = fluidBounds.horizontalInset();
            float s1 = 1F - s0;

            float y0 = baseY;
            float y1 = y0 + layer.fullness() * fluidBounds.yHeight();

            float u0 = sprite.getU(fluidBounds.horizontalInset());
            float v0 = sprite.getV0();
            float u1 = sprite.getU((1f - fluidBounds.horizontalInset()));
            float v1 = sprite.getV(y1);

            float u0top = sprite.getU(fluidBounds.horizontalInset());
            float v0top = sprite.getV(fluidBounds.horizontalInset());
            float u1top = sprite.getU(1f - fluidBounds.horizontalInset());
            float v1top = sprite.getV(1f - fluidBounds.horizontalInset());

            collector.submitCustomGeometry(poseStack, RenderTypes.text(Sheets.BLOCKS_MAPPER.sheet()), (pose, builder) -> {
                // top
                builder.addVertex(pose, s0, y1, s0).setColor(color).setUv(u0top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
                builder.addVertex(pose, s0, y1, s1).setColor(color).setUv(u0top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
                builder.addVertex(pose, s1, y1, s1).setColor(color).setUv(u1top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
                builder.addVertex(pose, s1, y1, s0).setColor(color).setUv(u1top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);

                // south
                builder.addVertex(pose, s0, y1, s1).setColor(color).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);
                builder.addVertex(pose, s0, y0, s1).setColor(color).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
                builder.addVertex(pose, s1, y0, s1).setColor(color).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);
                builder.addVertex(pose, s1, y1, s1).setColor(color).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);

                // down (not needed)
//        builder.addVertex(pose, s0, y0, s0).setColor(color).setUv(u0top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//        builder.addVertex(pose, s1, y0, s0).setColor(color).setUv(u1top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//        builder.addVertex(pose, s1, y0, s1).setColor(color).setUv(u1top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//        builder.addVertex(pose, s0, y0, s1).setColor(color).setUv(u0top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);

                // north
                builder.addVertex(pose, s0, y1, s0).setColor(color).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, -1F);
                builder.addVertex(pose, s1, y1, s0).setColor(color).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, -1F);
                builder.addVertex(pose, s1, y0, s0).setColor(color).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, -1F);
                builder.addVertex(pose, s0, y0, s0).setColor(color).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, -1F);

                // west
                builder.addVertex(pose, s0, y1, s0).setColor(color).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1F, 0F, 0F);
                builder.addVertex(pose, s0, y0, s0).setColor(color).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
                builder.addVertex(pose, s0, y0, s1).setColor(color).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
                builder.addVertex(pose, s0, y1, s1).setColor(color).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);

                // east
                builder.addVertex(pose, s1, y1, s0).setColor(color).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
                builder.addVertex(pose, s1, y1, s1).setColor(color).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
                builder.addVertex(pose, s1, y0, s1).setColor(color).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
                builder.addVertex(pose, s1, y0, s0).setColor(color).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(1F, 0F, 0F);
            });

            baseY = y1;  // move up for the next layer
        }
    }

//    public static void renderBlock(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int breakProgress) {
//        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
//            poseStack.scale(0.25f, 0.25f, 0.25f);
//
//            VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(bufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(breakProgress)), poseStack.last(), 1.0F);
//            MultiBufferSource bufferSource1 = type -> {
//                VertexConsumer vc = bufferSource.getBuffer(type);
//                return type.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vc) : vc;
//            };
//
//            BlockState state = blockItem.getBlock().defaultBlockState();
//            Minecraft.getInstance().getBlockModelResolver().update();
//            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource1, combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
//        }
//    }

    public record FluidBounds(float horizontalInset, float minY, float yHeight) {
    }

}
