package dev.ftb.mods.ftbstuffnthings.capabilities;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.BiConsumer;

public class IOStackHandler implements ResourceHandler<ItemResource> {
    private final EmittingStackHandler input;
    private final EmittingStackHandler output;

    public IOStackHandler(int inputSlots, int outputSlots, BiConsumer<IOStackHandler, IO> onChange) {
        this.input = new EmittingStackHandler(inputSlots, _ -> onChange.accept(this, IO.INPUT));
        this.output = new EmittingStackHandler(outputSlots, _ -> onChange.accept(this, IO.OUTPUT));
    }

    @Override
    public int size() {
        return input.size() + output.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return index < input.size() ? input.getResource(index) : output.getResource(index - input.size());
    }

    @Override
    public long getAmountAsLong(int index) {
        return index < input.size() ? input.getAmountAsLong(index) : output.getAmountAsLong(index - input.size());
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return index < input.size() ? input.getAmountAsLong(index) : output.getAmountAsLong(index - input.size());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index < input.size() && input.isValid(index, resource);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        int size = input.size();
        for (int index = 0; index < size; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return index < input.size() ? input.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int extracted = 0;
        int inputSize = input.size();
        int outputSize = output.size();
        for (int index = inputSize; index < inputSize + outputSize; index++) {
            extracted += extract(index, resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return index < input.size() ? 0 : output.extract(index - input.size(), resource, amount, transaction);
    }

    public ItemStacksResourceHandler getInput() {
        return input;
    }

    public ItemStacksResourceHandler getOutput() {
        return output;
    }

    public enum IO {
        INPUT,
        OUTPUT
    }
}
