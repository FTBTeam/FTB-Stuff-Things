package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.supercooler.SuperCoolerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class SuperCoolerScreen extends BaseFluidAndEnergyScreen<SuperCoolerMenu> {
    private static final Identifier TEXTURE = FTBStuffNThings.id("textures/gui/super_cooler_background.png");

    public SuperCoolerScreen(SuperCoolerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 3, 79, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 155 - font.width(title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int i, int j) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
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
        return menu.getBlockEntity().getProgress();
    }

    @Override
    public int getProgressRequired() {
        return menu.getBlockEntity().getMaxProgress();
    }
}
