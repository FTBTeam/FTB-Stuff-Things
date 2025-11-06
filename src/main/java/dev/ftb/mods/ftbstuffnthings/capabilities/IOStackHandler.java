package dev.ftb.mods.ftbstuffnthings.capabilities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class IOStackHandler implements IItemHandler {
    private final ItemStackHandler input;
    private final ItemStackHandler output;

    public IOStackHandler(int inputSlots, int outputSlots, BiConsumer<IOStackHandler, IO> onChange) {
        this.input = new EmittingStackHandler(inputSlots, (contents) -> onChange.accept(this, IO.INPUT));
        this.output = new EmittingStackHandler(outputSlots, (contents) -> onChange.accept(this, IO.OUTPUT));
    }

    @Override
    public int getSlots() {
        return input.getSlots() + output.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return i < input.getSlots() ? input.getStackInSlot(i) : output.getStackInSlot(i - input.getSlots());
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack arg, boolean bl) {
        return i < input.getSlots() ? input.insertItem(i, arg, bl) : arg;
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int j, boolean bl) {
        return i < input.getSlots() ? ItemStack.EMPTY : output.extractItem(i - input.getSlots(), j, bl);
    }

    @Override
    public int getSlotLimit(int i) {
        return i < input.getSlots() ? input.getSlotLimit(i) : output.getSlotLimit(i - input.getSlots());
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack arg) {
        return i < input.getSlots() && input.isItemValid(i, arg);
    }

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public enum IO {
        INPUT,
        OUTPUT
    }
}
