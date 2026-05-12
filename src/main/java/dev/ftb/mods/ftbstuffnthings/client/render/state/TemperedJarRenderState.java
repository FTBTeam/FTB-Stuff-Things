package dev.ftb.mods.ftbstuffnthings.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.ArrayList;
import java.util.List;

public class TemperedJarRenderState extends BlockEntityRenderState {
    public FluidRenderData fluidRenderData;
    public List<ItemStackRenderState> itemStates = new ArrayList<>();
    public float ticks;
    public boolean floatingItems;
}
