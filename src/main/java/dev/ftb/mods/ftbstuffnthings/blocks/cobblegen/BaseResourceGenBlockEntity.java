package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class BaseResourceGenBlockEntity extends BlockEntity {
    protected final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private BlockCapabilityCache<IItemHandler, Direction> outputCache;
    private int ticks;
    private final IResourceGenProps props;

    protected BaseResourceGenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, IResourceGenProps props) {
        super(type, pos, blockState);
        this.props = props;
    }

    public abstract Item generatedItem();

    protected abstract int tickRate();

    public void tickServer() {
        ticks++;

        if (!getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return;
        }

        if (ticks % tickRate() != 0) {
            return;
        }

        IItemHandler inv = getConnectedInventory();
        int amount = props.itemsPerOperation();

        if (inv != null) {
            ItemStack excess = ItemHandlerHelper.insertItem(inv, new ItemStack(generatedItem(), amount), false);
            if (!excess.isEmpty()) {
                // output handler too full, store excess internally and clear the output cache so a new output inv
                //   is searched for on the next tick
                ItemHandlerHelper.insertItem(inventory, excess, false);
                outputCache = null;
            }
        } else {
            ItemHandlerHelper.insertItem(inventory, new ItemStack(generatedItem(), amount), false);
        }
    }

    public ItemStackHandler getInternalInventory() {
        return inventory;
    }

    public boolean isActive() {
        if (!getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return false;
        }

        if (inventory.getStackInSlot(0).getCount() >= 64) {
            IItemHandler connectedInventory = getConnectedInventory();
            return connectedInventory != null && hasSpaceInInventory(connectedInventory);
        }

        return true;
    }

    @Nullable
    private IItemHandler getConnectedInventory() {
        if (outputCache == null || outputCache.getCapability() == null) {
            for (Direction direction : Direction.values()) {
                outputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(direction), direction.getOpposite());
                IItemHandler dest = outputCache.getCapability();
                if (dest != null && hasSpaceInInventory(dest)) {
                    return dest;
                }
            }
        }
        return outputCache == null ? null : outputCache.getCapability();
    }

    private boolean hasSpaceInInventory(IItemHandler inventory) {
        return ItemHandlerHelper.insertItem(inventory, generatedItem().getDefaultInstance(), true).isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }
}
