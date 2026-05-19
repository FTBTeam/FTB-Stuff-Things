package dev.ftb.mods.ftbstuffnthings.client.render.state;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.ArrayList;
import java.util.List;

public record FluidRenderData(FluidBounds bounds, List<Layer> fluidLayers, boolean empty) {
    public static FluidRenderData fromTank(BlockEntity blockEntity, FluidBounds bounds, ResourceHandler<FluidResource> handler) {
        int totalCapacity = 0;
        for (int i = 0; i < handler.size(); i++) {
            totalCapacity += handler.getCapacityAsInt(i, FluidResource.EMPTY);
        }
        if (totalCapacity == 0) {
            return new FluidRenderData(bounds, List.of(), true);
        }

        BlockAndTintGetter tintGetter = blockEntity.getLevel() instanceof BlockAndTintGetter tg ? tg : BlockAndTintGetter.EMPTY;
        BlockPos pos = blockEntity.getBlockPos();

        int totalFluid = 0;
        List<Layer> layers = new ArrayList<>();
        for (int i = 0; i < handler.size(); i++) {
            totalFluid += handler.getAmountAsInt(i);
            Fluid fluid = handler.getResource(i).getFluid();
            FluidState fluidState = fluid.defaultFluidState();
            var fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
            int tint = 0xFF000000 | (fluidModel.fluidTintSource() == null ?
                    0xFFFFFFF :
                    fluidModel.fluidTintSource().colorInWorld(fluidState, fluidState.createLegacyBlock(), tintGetter, pos)
            );
            layers.add(new Layer(fluidModel, tint, (float) handler.getAmountAsInt(i) / totalCapacity));
        }

        return new FluidRenderData(bounds, layers, totalFluid == 0);
    }

    public void submit(SubmitNodeCollector collector, PoseStack poseStack, int packedLight, int packedOverlay) {
        if (empty) {
            return;
        }

        float baseY = bounds.minY();

        for (FluidRenderData.Layer layer : fluidLayers) {
            var model = layer.fluidModel();
            var sprite = model.stillMaterial().sprite();
            int color = layer.fluidTint();

            float s0 = bounds.horizontalInset();
            float s1 = 1F - s0;

            float y0 = baseY;
            float y1 = y0 + layer.fullness() * bounds.yHeight();

            float u0 = sprite.getU(bounds.horizontalInset());
            float v0 = sprite.getV0();
            float u1 = sprite.getU((1f - bounds.horizontalInset()));
            float v1 = sprite.getV(y1);

            float u0top = sprite.getU(bounds.horizontalInset());
            float v0top = sprite.getV(bounds.horizontalInset());
            float u1top = sprite.getU(1f - bounds.horizontalInset());
            float v1top = sprite.getV(1f - bounds.horizontalInset());

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

    public record Layer(FluidModel fluidModel, int fluidTint, float fullness) {
    }

    public record FluidBounds(float horizontalInset, float minY, float yHeight) {
    }
}
