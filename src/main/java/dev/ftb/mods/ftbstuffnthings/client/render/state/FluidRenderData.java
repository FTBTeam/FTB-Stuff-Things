package dev.ftb.mods.ftbstuffnthings.client.render.state;

import dev.ftb.mods.ftbstuffnthings.client.RenderUtil;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.ArrayList;
import java.util.List;

public record FluidRenderData(RenderUtil.FluidBounds bounds, List<Layer> fluidLayers, boolean empty) {
    public static FluidRenderData fromTank(RenderUtil.FluidBounds bounds, ResourceHandler<FluidResource> handler) {
        int totalCapacity = 0;
        for (int i = 0; i < handler.size(); i++) {
            totalCapacity += handler.getCapacityAsInt(i, FluidResource.EMPTY);
        }
        if (totalCapacity == 0) {
            return new FluidRenderData(bounds, List.of(), true);
        }

        int totalFluid = 0;
        List<Layer> layers = new ArrayList<>();
        for (int i = 0; i < handler.size(); i++) {
            totalFluid += handler.getAmountAsInt(i);
            layers.add(new Layer(handler.getResource(i).getFluid(), (float) handler.getAmountAsInt(i) / totalCapacity));
        }

        return new FluidRenderData(bounds, layers, totalFluid == 0);
    }

    public record Layer(Fluid fluid, float fullness) {
    }
}
