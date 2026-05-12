package dev.ftb.mods.ftbstuffnthings.capabilities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.function.Consumer;

public class EmittingStackHandler extends ItemStacksResourceHandler {
    private final Consumer<EmittingStackHandler> onChange;

    public EmittingStackHandler(int size, Consumer<EmittingStackHandler> onChange) {
        super(size);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        super.onContentsChanged(index, previousContents);

        onChange.accept(this);
    }
}
