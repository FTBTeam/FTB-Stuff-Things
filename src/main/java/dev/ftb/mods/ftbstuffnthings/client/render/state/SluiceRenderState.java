package dev.ftb.mods.ftbstuffnthings.client.render.state;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;

public class SluiceRenderState extends BlockEntityRenderState {
    public FluidState fluidState;
    public Direction facing;
    public ItemStackRenderState itemState = new ItemStackRenderState();
    public int time;
    public int progress;
    public FluidModel fluidModel;
    public int fluidTint;
}
