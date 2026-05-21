package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class FusingMachineScreen extends BaseFluidAndEnergyScreen<FusingMachineMenu> {
    private static final Identifier TEXTURE = FTBStuffNThings.id("textures/gui/fusing_machine_background.png");

    public FusingMachineScreen(FusingMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 140, 90, TEXTURE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int i, int j) {
        graphics.text(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
    }

    @Override
    public int getEnergyAmount() {
        return menu.getBlockEntity().getEnergy();
    }

    @Override
    public int getEnergyCapacity() {
        return this.menu.getBlockEntity().getMaxEnergy();
    }

    @Override
    public int getFluidCapacity() {
        return this.menu.getBlockEntity().getMaxFluid();
    }

    @Override
    public FluidStack getFluidStack() {
        return this.menu.getBlockEntity().getFluid();
    }

    @Override
    public int getProgress() {
        return this.menu.getBlockEntity().getProgress();
    }

    @Override
    public int getProgressRequired() {
        return menu.getBlockEntity().getMaxProgress();
    }
}
