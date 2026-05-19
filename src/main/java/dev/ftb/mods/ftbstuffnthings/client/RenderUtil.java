package dev.ftb.mods.ftbstuffnthings.client;

public class RenderUtil {
//    public static void submitFluid(FluidRenderData info, SubmitNodeCollector collector, PoseStack poseStack, int packedLight, int packedOverlay) {
//        if (info.empty()) {
//            return;
//        }
//
//        FluidBounds fluidBounds = info.bounds();
//        float baseY = fluidBounds.minY();
//
//        for (FluidRenderData.Layer layer : info.fluidLayers()) {
//            var model = layer.fluidModel();
//            var sprite = model.stillMaterial().sprite();
//            int color = layer.fluidTint();
//
//            float s0 = fluidBounds.horizontalInset();
//            float s1 = 1F - s0;
//
//            float y0 = baseY;
//            float y1 = y0 + layer.fullness() * fluidBounds.yHeight();
//
//            float u0 = sprite.getU(fluidBounds.horizontalInset());
//            float v0 = sprite.getV0();
//            float u1 = sprite.getU((1f - fluidBounds.horizontalInset()));
//            float v1 = sprite.getV(y1);
//
//            float u0top = sprite.getU(fluidBounds.horizontalInset());
//            float v0top = sprite.getV(fluidBounds.horizontalInset());
//            float u1top = sprite.getU(1f - fluidBounds.horizontalInset());
//            float v1top = sprite.getV(1f - fluidBounds.horizontalInset());
//
//            collector.submitCustomGeometry(poseStack, RenderTypes.text(Sheets.BLOCKS_MAPPER.sheet()), (pose, builder) -> {
//                // top
//                builder.addVertex(pose, s0, y1, s0).setColor(color).setUv(u0top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//                builder.addVertex(pose, s0, y1, s1).setColor(color).setUv(u0top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//                builder.addVertex(pose, s1, y1, s1).setColor(color).setUv(u1top, v1top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//                builder.addVertex(pose, s1, y1, s0).setColor(color).setUv(u1top, v0top).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//
//                // south
//                builder.addVertex(pose, s0, y1, s1).setColor(color).setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);
//                builder.addVertex(pose, s0, y0, s1).setColor(color).setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 1F, 0F);
//                builder.addVertex(pose, s1, y0, s1).setColor(color).setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);
//                builder.addVertex(pose, s1, y1, s1).setColor(color).setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight).setNormal(0F, 0F, 1F);
//
//                // down (not needed)

}
