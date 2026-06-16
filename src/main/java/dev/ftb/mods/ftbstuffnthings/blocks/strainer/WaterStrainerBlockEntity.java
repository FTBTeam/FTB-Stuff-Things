package dev.ftb.mods.ftbstuffnthings.blocks.strainer;

import dev.ftb.mods.ftbstuffnthings.Config;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.capabilities.ComparatorItemStackHandler;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class WaterStrainerBlockEntity extends AbstractMachineBlockEntity {
    private final ComparatorItemStackHandler inventory = new ComparatorItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private final IItemHandler extractOnly = new ExtractOnlyHandlerWrapper(inventory);

    private static LootTable lootTable = null;

    public WaterStrainerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.WATER_STRAINER.get(), pos, state);
    }

    public void tickServer(ServerLevel serverLevel) {
        if (getBlockState().getValue(BlockStateProperties.WATERLOGGED) && serverLevel.getGameTime() % Config.STRAINER_TICK_RATE.get() == 0) {
            LootTable table = getLootTable(serverLevel);
            if (table != null) {
                LootParams params = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                        .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
                        .withParameter(LootContextParams.BLOCK_ENTITY, this)
                        .create(LootContextParamSets.CHEST);
                table.getRandomItems(params, stack -> {
                    // just discard anything that doesn't fit, it's the player's job to keep it cleared :P
                    if (!stack.isEmpty()) {
                        ItemHandlerHelper.insertItem(inventory, stack, false);
                    }
                });
            }
        }
    }

    private LootTable getLootTable(ServerLevel serverLevel) {
        if (lootTable == null) {
            try {
                ResourceLocation tableId = Config.getStrainerLootTable()
                        .orElseThrow(() -> new IllegalStateException("invalid strainer loot table resource location"));
                lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, tableId));
            } catch (IllegalStateException e) {
                FTBStuffNThings.LOGGER.error("can't retrieve water strainer loot table (using empty loot table): {}", e.getMessage());
                lootTable = LootTable.EMPTY;
            }
        }
        return lootTable;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WaterStrainerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public @Nullable IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? inventory : extractOnly;
    }

    @Override
    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    public @Nullable IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return null;
    }

    public static void clearCachedLootTable() {
        lootTable = null;
    }

    public int getComparatorLevel() {
        return inventory.getComparatorLevel();
    }

    public record ExtractOnlyHandlerWrapper(IItemHandler wrapped) implements IItemHandler {
        @Override
        public int getSlots() {
            return wrapped.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return wrapped.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            // no insertion!
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return wrapped.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return wrapped.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return wrapped.isItemValid(slot, stack);
        }
    }
}
