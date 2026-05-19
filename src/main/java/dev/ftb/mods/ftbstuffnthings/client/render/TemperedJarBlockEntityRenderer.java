package dev.ftb.mods.ftbstuffnthings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import dev.ftb.mods.ftbstuffnthings.client.render.state.FluidRenderData;
import dev.ftb.mods.ftbstuffnthings.client.render.state.TemperedJarRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jspecify.annotations.Nullable;

public class TemperedJarBlockEntityRenderer implements BlockEntityRenderer<TemperedJarBlockEntity, TemperedJarRenderState> {
    private final ItemModelResolver itemModelResolver;

    public TemperedJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public TemperedJarRenderState createRenderState() {
        return new TemperedJarRenderState();
    }

    @Override
    public void extractRenderState(TemperedJarBlockEntity blockEntity, TemperedJarRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.fluidRenderData = FluidRenderData.fromTank(blockEntity, JarBlockEntityRenderer.JAR_BOUNDS, blockEntity.getFluidHandler());

        ResourceHandler<ItemResource> handler = blockEntity.getInputItemHandler();
        for (int i = 0; i < handler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(handler, i);
            if (!stack.isEmpty()) {
                ItemStackRenderState itemState = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
                state.itemStates.add(itemState);
            }
        }
        state.ticks = blockEntity.getLevel().getGameTime() + partialTicks;
    }

    @Override
    public void submit(TemperedJarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        state.fluidRenderData.submit(submitNodeCollector, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY);

        submitItems(state, poseStack, submitNodeCollector);
    }

    private static void submitItems(TemperedJarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        float circleRadius = state.itemStates.size() == 1 ? 0 : 0.17f;
        float degreesPerStack = 360f / state.itemStates.size();
        float ticks = state.ticks;
        float yBob = state.floatingItems ? Mth.sin((ticks  / 10f) % 360) * 0.01f : 0;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.1, 0.5);

        for (int i = 0; i < state.itemStates.size(); i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * degreesPerStack + (state.floatingItems ? ticks / 3f % 360 : 0.0f)));
            poseStack.translate(circleRadius, yBob,0);
            poseStack.scale(0.25f, 0.25f, 0.25f);

            state.itemStates.get(i).submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
