package dev.ftb.mods.ftbstuffnthings.capabilities;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

public class SimpleFluidTank extends FluidStacksResourceHandler {
    public SimpleFluidTank(int capacity) {
        super(1, capacity);
    }

    public FluidStack copyStack() {
        return getResource(0).toStack(getAmountAsInt(0));
    }
}
