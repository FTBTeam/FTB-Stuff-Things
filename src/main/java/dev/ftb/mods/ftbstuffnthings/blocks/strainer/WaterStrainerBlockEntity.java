package dev.ftb.mods.ftbstuffnthings.blocks.strainer;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.capabilities.ComparatorItemStackHandler;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class WaterStrainerBlockEntity extends AbstractMachineBlockEntity {
    private final ComparatorItemStackHandler inventory = new ComparatorItemStackHandler(27, _ -> setChanged());
    private final ExtractOnlyHandlerWrapper extractOnly = new ExtractOnlyHandlerWrapper(inventory);

    @Nullable
    private static LootTable lootTable = null;

    public WaterStrainerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.WATER_STRAINER.get(), pos, state);
    }

    public void tickServer(ServerLevel serverLevel) {
        if (getBlockState().getValue(BlockStateProperties.WATERLOGGED) && serverLevel.getGameTime() % ModConfig.STRAINER_TICK_RATE.get() == 0) {
            LootTable table = getLootTable(serverLevel);
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                    .withParameter(LootContextParams.BLOCK_STATE, getBlockState())
                    .withParameter(LootContextParams.BLOCK_ENTITY, this)
                    .create(LootContextParamSets.CHEST);
            table.getRandomItems(params, stack -> {
                // just discard anything that doesn't fit, it's the player's job to keep it cleared :P
                if (!stack.isEmpty()) {
                    try (Transaction tx = Transaction.openRoot()) {
                        ResourceHandlerUtil.insertStacking(inventory, ItemResource.of(stack), stack.count(), tx);
                        tx.commit();
                    }
                }
            });
        }
    }

    private LootTable getLootTable(ServerLevel serverLevel) {
        if (lootTable == null) {
            try {
                Identifier tableId = ModConfig.getStrainerLootTable()
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
        return new WaterStrainerMenu(containerId, playerInventory, getBlockPos());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("Inventory", inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        inventory.deserialize(input.childOrEmpty("Inventory"));
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return side == null ? inventory : extractOnly;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return null;
    }

    public static void clearCachedLootTable() {
        lootTable = null;
    }

    public int getComparatorLevel() {
        return inventory.getComparatorLevel();
    }

    public void indexModifier(int index, ItemResource resource, int amount) {
        inventory.set(index, resource, amount);
    }

    public static class ExtractOnlyHandlerWrapper extends DelegatingResourceHandler<ItemResource> {
        public ExtractOnlyHandlerWrapper(ResourceHandler<ItemResource> delegate) {
            super(delegate);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
