package dev.ftb.mods.ftbstuffnthings.blocks.basaltgen;

import dev.ftb.mods.ftbstuffnthings.Config;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class BasaltGeneratorBlockEntity extends BlockEntity {

    private final BasaltGeneratorProperties basaltGeneratorProperties;
    protected ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private BlockCapabilityCache<IItemHandler, Direction> outputCache;
    private int ticks;

    public BasaltGeneratorBlockEntity(BlockEntityType<?> type, BasaltGeneratorProperties props, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.basaltGeneratorProperties = props;
    }

    public void tickServer() {
        ticks++;

        if (!getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return;
        }

        if (ticks % Config.DELAY_PER_OPERATION.get() != 0) {
            return;
        }

        IItemHandler inv = getConnectedInventory();
        int speed = basaltGeneratorProperties.getGeneratorSpeed();

        if (inv != null) {
            ItemStack excess = ItemHandlerHelper.insertItem(inv, new ItemStack(Items.BASALT, speed), false);
            if (!excess.isEmpty()) {
                // output handler too full, store excess internally and clear the output cache so a new output inv
                //   is searched for on the next tick
                ItemHandlerHelper.insertItem(inventory, excess, false);
                outputCache = null;
            }
        } else {
            ItemHandlerHelper.insertItem(inventory, new ItemStack(Items.BASALT, speed), false);
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
                outputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(direction), null);
                IItemHandler dest = outputCache.getCapability();
                if (dest != null && hasSpaceInInventory(dest)) {
                    return dest;
                }
            }
        }
        return outputCache == null ? null : outputCache.getCapability();
    }

    private boolean hasSpaceInInventory(IItemHandler inventory) {
        return ItemHandlerHelper.insertItem(inventory, Items.BASALT.getDefaultInstance(), true).isEmpty();
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

    public static class Stone extends BasaltGeneratorBlockEntity {
        public Stone(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.STONE_BASALT_GENERATOR.get(), BasaltGeneratorProperties.STONE, pos, blockState);
        }
    }

    public static class Iron extends BasaltGeneratorBlockEntity {
        public Iron(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.IRON_BASALT_GENERATOR.get(), BasaltGeneratorProperties.IRON, pos, blockState);
        }
    }

    public static class Gold extends BasaltGeneratorBlockEntity {
        public Gold(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.GOLD_BASALT_GENERATOR.get(), BasaltGeneratorProperties.GOLD, pos, blockState);
        }
    }

    public static class Diamond extends BasaltGeneratorBlockEntity {
        public Diamond(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DIAMOND_BASALT_GENERATOR.get(), BasaltGeneratorProperties.DIAMOND, pos, blockState);
        }
    }

    public static class Netherite extends BasaltGeneratorBlockEntity {
        public Netherite(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.NETHERITE_BASALT_GENERATOR.get(), BasaltGeneratorProperties.NETHERITE, pos, blockState);
        }
    }
}
