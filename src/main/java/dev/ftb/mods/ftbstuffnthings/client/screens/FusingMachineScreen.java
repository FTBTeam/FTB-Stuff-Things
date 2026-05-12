package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
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
    protected void renderLabels(GuiGraphics graphics, int i, int j) {
        graphics.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public int getEnergyAmount() {
        return menu.blockEntity.getEnergy();
    }

    @Override
    public int getEnergyCapacity() {
        return this.menu.blockEntity.getMaxEnergy();
    }

    @Override
    public int getFluidCapacity() {
        return this.menu.blockEntity.getMaxFluid();
    }

    @Override
    public FluidStack getFluidStack() {
        return this.menu.blockEntity.getFluid();
    }

    @Override
    public int getProgress() {
        return this.menu.blockEntity.getProgress();
    }

    @Override
    public int getProgressRequired() {
        return menu.blockEntity.getMaxProgress();
    }
}
