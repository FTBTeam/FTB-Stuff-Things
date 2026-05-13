package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public abstract class BaseResourceGenBlockEntity extends BlockEntity {
    protected final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> outputCache;
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

        var connectedInventory = getConnectedInventory();
        ItemResource resource = ItemResource.of(generatedItem());
        int amount = props.itemsPerOperation();

        try (Transaction tx = Transaction.openRoot()) {
            if (connectedInventory != null) {
                int inserted = ResourceHandlerUtil.insertStacking(connectedInventory, resource, amount, tx);
                if (inserted < amount) {
                    // output handler too full, store excess internally and clear the output cache so a new output inv
                    //   is searched for on the next tick
                    inventory.insert(resource, amount - inserted, tx);
                    outputCache = null;
                }
            } else {
                inventory.insert(resource, amount, tx);
            }
            tx.commit();
        }
    }

    public ResourceHandler<ItemResource> getInternalInventory() {
        return inventory;
    }

    public boolean isActive() {
        if (!getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return false;
        }

        if (inventory.getAmountAsInt(0) >= 64) {
            var connectedInventory = getConnectedInventory();
            return connectedInventory != null && !ResourceHandlerUtil.isFull(connectedInventory);
        }

        return true;
    }

    @Nullable
    private ResourceHandler<ItemResource> getConnectedInventory() {
        if (outputCache == null || outputCache.getCapability() == null) {
            for (Direction direction : Direction.values()) {
                outputCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(direction), direction.getOpposite());
                var dest = outputCache.getCapability();
                if (dest != null && !ResourceHandlerUtil.isFull(dest)) {
                    return dest;
                }
            }
        }
        return outputCache == null ? null : outputCache.getCapability();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("inventory", inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        inventory.deserialize(input.childOrEmpty("inventory"));
    }
}
