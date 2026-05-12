package dev.ftb.mods.ftbstuffnthings.blocks.hammer;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.HammerRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AutoHammerBlockEntity extends BlockEntity {
    private final AutoHammerProperties props;
    private final AutoHammerItemHandler itemHandler = new AutoHammerItemHandler();  // internal handler
    private final InputHandler inputHandler = new InputHandler(itemHandler);  // public handler for capabilities

    private boolean active;
    private int progress;
    private int displayProgress; // client side only
    private ItemStack processingStack = ItemStack.EMPTY;
    private int timeout = 0;
    private int maxTimeout;
    private final List<ItemStack> overflow = new ArrayList<>(); // output items which won't fit into output inv
    private final OutputHandler outputHandler = new OutputHandler(overflow);
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> inputCache;
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> outputCache;
    @Nullable
    private HammerRecipe currentRecipe = null;
    private int lastPulledSlot;

    protected AutoHammerBlockEntity(BlockEntityType<?> type, AutoHammerProperties props, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.props = props;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends AutoHammerBlockEntity> machine) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, machine, AutoHammerBlockEntity::getItemHandler);
    }

    @Nullable
    private ResourceHandler<ItemResource> getItemHandler(Direction side) {
        Direction dir = getInputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        if (side == dir) {
            return inputHandler;
        } else if (side == dir.getOpposite()) {
            return outputHandler;
        } else {
            return null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.store("ProcessingStack", ItemStack.OPTIONAL_CODEC, registries.createSerializationContext(NbtOps.INSTANCE), processingStack);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        processingStack = input.read("ProcessingStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        displayProgress = 0;
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        handleUpdateTag(valueInput);
    }

    public void tickClient(Level level) {
        if (!processingStack.isEmpty() && getBlockState().getValue(AbstractMachineBlock.ACTIVE)) {
            if (++displayProgress >= props.getHammerSpeed()) {
                displayProgress = 0;
                if (processingStack.getItem() instanceof BlockItem blockItem) {
                    level.addDestroyBlockEffect(getBlockPos(), blockItem.getBlock().defaultBlockState());
                }
            }
        }
    }

    private ItemStack getNextInputItem(ServerLevel level) {
        if (itemHandler.getStack().isEmpty()) {
            tryPullFromInput(level);
        }
        return itemHandler.getStack();
    }

    public void tickServer(ServerLevel serverLevel) {
        if (!getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return;
        }

        ItemStack prevProcessingStack = processingStack.copy();
        if (timeout > 0) {
            // on cooldown
            timeout--;
            if (timeout == 0) maxTimeout = 0;
        } else if (!overflow.isEmpty()) {
            // try to empty the overflow before doing anything else
            List<ItemStackTemplate> retry = overflow.stream().map(ItemStackTemplate::fromNonEmptyStack).toList();
            overflow.clear();
            if (!tryPushToOutput(retry)) {
                goOnTimeout(30);
            }
            setChanged();
        } else if (progress == 0) {
            ItemStack inputStack = getNextInputItem(serverLevel);
            if (inputStack.isEmpty()) {
                currentRecipe = null;
                goOnTimeout(20);
            } else {
                getRecipeForStack(serverLevel, inputStack).ifPresentOrElse(
                        recipe -> {
                            currentRecipe = recipe;
                            try (Transaction tx = Transaction.openRoot()) {
                                ItemResource resource = itemHandler.getResource(0);
                                int extracted = itemHandler.extract(0, resource, 1, tx);
                                processingStack = resource.toStack(extracted);
                                tx.commit();
                            }
                            progress = 1;
                            active = true;
                            setChanged();
                        },
                        () -> {
                            // invalid item, shouldn't happen!
                            Block.popResource(serverLevel, getBlockPos().above(), inputStack);
                            itemHandler.setStack(ItemStack.EMPTY);
                            goOnTimeout(20);
                        }
                );
            }
        } else {
            if (progress <= props.getHammerSpeed()) {
                progress++;
                setChanged();
            } else {
                if (currentRecipe == null) {
                    // can happen on restore from NBT
                    currentRecipe = getRecipeForStack(serverLevel, processingStack).orElse(null);
                }
                if (currentRecipe != null) {  // should always be the case!
                    // completed one cycle, try to move output to adjacent inventory
                    progress = 0;
                    if (tryPushToOutput(currentRecipe.getResults())) {
                        // done!
                        setChanged();
                    } else {
                        // nowhere to send the outputs, stall for a bit and try again
                        goOnTimeout(30);
                    }
                    processingStack = ItemStack.EMPTY;
                }
            }
        }

        boolean stateActive = getBlockState().getValue(AbstractMachineBlock.ACTIVE);
        if (stateActive && !active) {
            serverLevel.setBlock(getBlockPos(), getBlockState().setValue(AbstractMachineBlock.ACTIVE, false), Block.UPDATE_ALL);
        } else if (!stateActive && active) {
            serverLevel.setBlock(getBlockPos(), getBlockState().setValue(AbstractMachineBlock.ACTIVE, true), Block.UPDATE_ALL);
        }

        if (!ItemStack.isSameItemSameComponents(prevProcessingStack, processingStack)) {
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static Optional<HammerRecipe> getRecipeForStack(ServerLevel level, ItemStack inputStack) {
        if (inputStack.isEmpty()) {
            return Optional.empty();
        }
        return RecipeCaches.HAMMER.getCachedRecipe(level,
                l -> searchForRecipe(l, inputStack),
                () -> genIngredientHash(inputStack)
        ).map(RecipeHolder::value);
    }

    public static Optional<RecipeHolder<HammerRecipe>> searchForRecipe(ServerLevel level, ItemStack stack) {
        return level.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.HAMMER_TYPE.get(), NoInventory.INSTANCE, level)
                .filter(r -> r.value().getIngredient().test(stack))
                .findFirst();
    }

    public static int genIngredientHash(ItemStack stack) {
        return ItemStack.hashItemAndComponents(stack);
    }

    private void tryPullFromInput(ServerLevel level) {
        if (inputCache == null) {
            Direction dir = getInputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            inputCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite());
        }

        ResourceHandler<ItemResource> src = inputCache.getCapability();
        // we don't pull from other auto-hammers, since they autopush output anyway
        if (src != null && !(src instanceof OutputHandler)) {
            try (Transaction tx = Transaction.openRoot()) {
                ResourceStack<ItemResource> extracted = ResourceHandlerUtil.extractFirst(
                        src, resource -> getRecipeForStack(level, resource.toStack()).isPresent(), 1, tx
                );
                if (extracted != null) {
                    int inserted = itemHandler.insert(extracted.resource(), extracted.amount(), tx);
                    if (inserted == extracted.amount()) {
                        tx.commit();
                    }
                }
            }


//            if (lastPulledSlot >= src.size()) {
//                lastPulledSlot = 0;
//            }
//            for (int i = 0; i < src.size(); i++) {
//                int actualSlot = i + lastPulledSlot >= src.size() ? i + lastPulledSlot - src.size() : i + lastPulledSlot;
//                ItemStack stack = src.getStackInSlot(actualSlot);
//                if (getRecipeForStack(level, stack).isPresent()) {
//                    ItemStack in = src.extractItem(actualSlot, 1, true);
//                    if (!in.isEmpty()) {
//                        if (itemHandler.insertItem(0, in, false).isEmpty()) {
//                            src.extractItem(actualSlot, 1, false);
//                            return;
//                        }
//                    }
//                }
//            }
        }
    }

    public static Direction getInputDirection(Direction facing) {
        return facing.getCounterClockWise();
    }

    private boolean tryPushToOutput(List<ItemStackTemplate> outputs) {
        if (outputCache == null) {
            Direction dir = getOutputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            outputCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite());
        }

        ResourceHandler<ItemResource> dest = outputCache.getCapability();
        if (dest != null) {
            for (ItemStackTemplate stackTmpl : outputs) {
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = dest.insert(ItemResource.of(stackTmpl), stackTmpl.count(), tx);
                    if (inserted < stackTmpl.count()) {
                        int excess = stackTmpl.count() - inserted;
                        if (excess > stackTmpl.getMaxStackSize()) {
                            // thanks, Functional Storage! https://github.com/FTBTeam/FTB-Mods-Issues/issues/1603
                            int nStacks = excess / stackTmpl.getMaxStackSize();
                            int remainder = excess % stackTmpl.getMaxStackSize();
                            for (int i = 0; i < nStacks; i++) {
                                overflow.addLast(stackTmpl.withCount(stackTmpl.getMaxStackSize()).create());
                            }
                            overflow.addLast(stackTmpl.withCount(remainder).create());
                        } else {
                            overflow.addLast(stackTmpl.withCount(excess).create());
                        }
                    }
                    tx.commit();
                }
            }
        } else {
            for (ItemStackTemplate output : outputs) {
                overflow.addLast(output.create());
            }
        }
        return overflow.isEmpty();
    }

    public static Direction getOutputDirection(Direction facing) {
        return facing.getClockWise();
    }

    private void goOnTimeout(int timeout) {
        this.timeout = this.maxTimeout = timeout;
        active = false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        ItemStack stack = itemHandler.getStack();
        if (!stack.isEmpty()) {
            Block.popResource(level, getBlockPos(), stack);
        }
        if (!processingStack.isEmpty()) {
            Block.popResource(level, getBlockPos(), processingStack);
        }
        overflow.forEach(os -> Block.popResource(level, getBlockPos(), os));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input.childOrEmpty("Input"));
        active = input.getBooleanOr("Active", false);
        progress = input.getIntOr("Progress", 0);
        processingStack = input.read("ProcessingStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        overflow.clear();
        overflow.addAll(input.read("Overflow", ItemStack.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("Input", itemHandler);
        if (active) output.putBoolean("Active", true);
        if (progress != 0) output.putInt("Progress", progress);
        if (!processingStack.isEmpty()) output.store("ProcessingStack", ItemStack.CODEC, processingStack);
        if (!overflow.isEmpty()) output.store("Overflow", ItemStack.CODEC.listOf(), overflow);
    }

    public ItemStack getProcessingStack() {
        return processingStack;
    }

    public int getDestroyStage() {
        return (int) ((float) displayProgress / props.getHammerSpeed() * 10f);
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return props.getHammerSpeed();
    }

    public Collection<ItemStack> getOverflow() {
        return overflow;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxTimeout() {
        return maxTimeout;
    }

    public void clearCapabilityCaches() {
        inputCache = outputCache = null;
    }

    public static class Iron extends AutoHammerBlockEntity {
        public Iron(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.IRON_HAMMER.get(), AutoHammerProperties.IRON, pos, blockState);
        }
    }

    public static class Gold extends AutoHammerBlockEntity {
        public Gold(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.GOLD_HAMMER.get(), AutoHammerProperties.GOLD, pos, blockState);
        }
    }

    public static class Diamond extends AutoHammerBlockEntity {
        public Diamond(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DIAMOND_HAMMER.get(), AutoHammerProperties.DIAMOND, pos, blockState);
        }
    }

    public static class Netherite extends AutoHammerBlockEntity {
        public Netherite(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.NETHERITE_HAMMER.get(), AutoHammerProperties.NETHERITE, pos, blockState);
        }
    }

    private class AutoHammerItemHandler extends ItemStackResourceHandler {
        private ItemStack stack = ItemStack.EMPTY;

        public AutoHammerItemHandler() {
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return level instanceof ServerLevel serverLevel && getRecipeForStack(serverLevel, resource.toStack()).isPresent();
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            return 1;
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            setChanged();
        }

        @Override
        protected ItemStack getStack() {
            return stack;
        }

        @Override
        protected void setStack(ItemStack stack) {
            this.stack = stack;
        }
    }

    private static class InputHandler extends DelegatingResourceHandler<ItemResource> {
        public InputHandler(ResourceHandler<ItemResource> delegate) {
            super(delegate);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    // FIXME this needs a complete rework - dynamically-sizing item handler
    private record OutputHandler(List<ItemStack> overflow) implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return overflow.size();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(overflow.get(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return overflow.get(index).count();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return resource.getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return true;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return overflow.isEmpty() ? 0 : Math.min(amount, overflow.getFirst().count());
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return overflow.isEmpty() ? ItemStack.EMPTY : (simulate ? overflow.getFirst() : overflow.removeFirst());
        }

    }
}
