package dev.ftb.mods.ftbstuffnthings.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.blocks.basaltgen.BasaltGeneratorBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BasaltGeneratorBlockEntityRenderer implements BlockEntityRenderer<BasaltGeneratorBlockEntity> {
    public BasaltGeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BasaltGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        var tick = blockEntity.getLevel().getGameTime();

        float texAnim = 10f;
        float speedFactor = 3f; // Increase this value to slow down the animation
        int time = (int) ((tick / speedFactor) % texAnim);

        int textureOffset = 2; // Adjust the offset as needed
        time = (time + textureOffset) % (int)texAnim;

        if (blockEntity.getLevel().isLoaded(blockEntity.getBlockPos()) && blockEntity.getBlockState().getValue(BlockStateProperties.ENABLED)) {
            poseStack.pushPose();
            poseStack.translate(0.375, 0.125, 0.375);
            RenderUtil.renderBlock(poseStack, bufferSource, packedLight, packedOverlay, Items.BASALT.getDefaultInstance(), time);
            poseStack.popPose();
        }

    }

}
