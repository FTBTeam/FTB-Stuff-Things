package dev.ftb.mods.ftbstuffnthings.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

import java.util.function.Consumer;

/**
 * An ItemStackHandler which supports cached comparator signal level calculation.
 * Only recalculates the signal when the contents have changed.
 */
public class ComparatorItemStackHandler extends EmittingStackHandler {
    private int signalLevel = -1;  // -1 indicates recalc needed

    public ComparatorItemStackHandler(int invSize, Consumer<EmittingStackHandler> onChange) {
        super(invSize, onChange);
    }

    public ComparatorItemStackHandler(int invSize) {
        super(invSize, _ -> {});
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        super.onContentsChanged(index, previousContents);

        invalidateComparatorValue();
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);

        invalidateComparatorValue();
    }

    public int getComparatorLevel() {
        if (signalLevel < 0) {
            signalLevel = ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(this);
        }
        return signalLevel;
    }

    public void invalidateComparatorValue() {
        signalLevel = -1;
    }
}
