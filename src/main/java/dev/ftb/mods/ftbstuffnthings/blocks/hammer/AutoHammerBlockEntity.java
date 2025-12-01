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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import org.jetbrains.annotations.Nullable;

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
    private BlockCapabilityCache<IItemHandler, Direction> inputCache;
    private BlockCapabilityCache<IItemHandler, Direction> outputCache;
    private HammerRecipe currentRecipe = null;
    private int lastPulledSlot;

    protected AutoHammerBlockEntity(BlockEntityType<?> type, AutoHammerProperties props, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.props = props;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends AutoHammerBlockEntity> machine) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, machine, AutoHammerBlockEntity::getItemHandler);
    }

    private IItemHandler getItemHandler(Direction side) {
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
        tag.put("ProcessingStack", processingStack.saveOptional(registries));
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        processingStack = ItemStack.parseOptional(lookupProvider, tag.getCompound("ProcessingStack"));
        displayProgress = 0;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
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
            List<ItemStack> retry = new ArrayList<>(overflow);
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
                            processingStack = itemHandler.extractItem(0, 1, false);
                            progress = 1;
                            active = true;
                            setChanged();
                        },
                        () -> {
                            // invalid item, shouldn't happen!
                            Block.popResource(serverLevel, getBlockPos().above(), inputStack);
                            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
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

    public static Optional<HammerRecipe> getRecipeForStack(Level level, ItemStack inputStack) {
        if (inputStack.isEmpty()) return Optional.empty();
        return RecipeCaches.HAMMER.getCachedRecipe(
                () -> searchForRecipe(level, inputStack),
                () -> genIngredientHash(inputStack)
        ).map(RecipeHolder::value);
    }

    public static Optional<RecipeHolder<HammerRecipe>> searchForRecipe(Level level, ItemStack stack) {
        return level.getRecipeManager().getRecipesFor(RecipesRegistry.HAMMER_TYPE.get(), NoInventory.INSTANCE, level).stream()
                .filter(r -> r.value().getIngredient().test(stack))
                .findFirst();
    }

    public static int genIngredientHash(ItemStack stack) {
        return ItemStack.hashItemAndComponents(stack);
    }

    private void tryPullFromInput(ServerLevel level) {
        if (inputCache == null) {
            Direction dir = getInputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            inputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite());
        }

        IItemHandler src = inputCache.getCapability();
        // we don't pull from other auto-hammers, since they autopush output anyway
        if (src != null && !(src instanceof OutputHandler)) {
            if (lastPulledSlot >= src.getSlots()) {
                lastPulledSlot = 0;
            }
            for (int i = 0; i < src.getSlots(); i++) {
                int actualSlot = i + lastPulledSlot >= src.getSlots() ? i + lastPulledSlot - src.getSlots() : i + lastPulledSlot;
                ItemStack stack = src.getStackInSlot(actualSlot);
                if (getRecipeForStack(level, stack).isPresent()) {
                    ItemStack in = src.extractItem(actualSlot, 1, true);
                    if (!in.isEmpty()) {
                        if (itemHandler.insertItem(0, in, false).isEmpty()) {
                            src.extractItem(actualSlot, 1, false);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static Direction getInputDirection(Direction facing) {
        return facing.getCounterClockWise();
    }

    private boolean tryPushToOutput(List<ItemStack> outputs) {
        if (outputCache == null) {
            Direction dir = getOutputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            outputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite());
        }

        IItemHandler dest = outputCache.getCapability();
        if (dest != null) {
            for (ItemStack stack : outputs) {
                // it _shouldn't be necessary to copy the stack here, but seems like not everyone plays nice...
                //   https://github.com/FTBTesting/Testing-Issues/issues/2491
                ItemStack excess = ItemHandlerHelper.insertItem(dest, stack.copy(), false);
                if (!excess.isEmpty()) {
                    if (excess.getCount() > excess.getMaxStackSize()) {
                        // thanks, Functional Storage! https://github.com/FTBTeam/FTB-Mods-Issues/issues/1603
                        int nStacks = excess.getCount() / excess.getMaxStackSize();
                        int remainder = excess.getCount() % excess.getMaxStackSize();
                        for (int i = 0; i < nStacks; i++) {
                            overflow.addLast(excess.copyWithCount(excess.getMaxStackSize()));
                        }
                        overflow.addLast(excess.copyWithCount(remainder));
                    } else {
                        overflow.addLast(excess);
                    }
                }
            }
        } else {
            for (ItemStack output : outputs) {
                overflow.addLast(output.copy());
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("Input"));
        active = tag.getBoolean("Active");
        progress = tag.getInt("Progress");
        processingStack = ItemStack.parseOptional(registries, tag.getCompound("ProcessingStack"));
        if (tag.contains("Overflow")) {
            overflow.clear();
            ItemStackHandler o = new ItemStackHandler();
            o.deserializeNBT(registries, tag.getCompound("Overflow"));
            for (int i = 0; i < o.getSlots(); i++) {
                overflow.addLast(o.getStackInSlot(i));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Input", itemHandler.serializeNBT(registries));
        if (active) tag.putBoolean("Active", true);
        if (progress != 0) tag.putInt("Progress", progress);
        if (!processingStack.isEmpty()) tag.put("ProcessingStack", processingStack.save(registries));
        if (!overflow.isEmpty()) {
            ItemStackHandler o = new ItemStackHandler(overflow.size());
            for (int i = 0; i < overflow.size(); i++) {
                o.setStackInSlot(i, overflow.get(i));
            }
            tag.put("Overflow", o.serializeNBT(registries));
        }
    }

    public void dropInventoryOnBreak(Level level) {
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            Block.popResource(level, getBlockPos(), stack);
        }
        if (!processingStack.isEmpty()) {
            Block.popResource(level, getBlockPos(), processingStack);
        }
        overflow.forEach(os -> Block.popResource(level, getBlockPos(), os));
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

    private class AutoHammerItemHandler extends ItemStackHandler {
        public AutoHammerItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return getRecipeForStack(level, stack).isPresent();
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        private ItemStack getStack() {
            return getStackInSlot(0);
        }
    }

    private static class InputHandler extends ForwardingItemHandler {
        public InputHandler(IItemHandler delegate) {
            super(delegate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    private record OutputHandler(List<ItemStack> overflow) implements IItemHandler {
        @Override
        public int getSlots() {
            return overflow.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return overflow.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return overflow.isEmpty() ? ItemStack.EMPTY : (simulate ? overflow.getFirst() : overflow.removeFirst());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
