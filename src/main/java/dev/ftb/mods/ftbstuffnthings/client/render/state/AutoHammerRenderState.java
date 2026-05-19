package dev.ftb.mods.ftbstuffnthings.client.render.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;

public class AutoHammerRenderState extends BlockEntityRenderState {
    public int hammerProgress;
    public BlockState blockState;
    public BlockModelRenderState renderState = new BlockModelRenderState();
}
