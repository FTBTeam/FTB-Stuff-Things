package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingEnergy;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SluiceRecipe;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.network.SendSluiceStartPacket;
import dev.ftb.mods.ftbstuffnthings.network.SyncDisplayItemPacket;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class SluiceBlockEntity extends AbstractMachineBlockEntity {
    private static final float BASE_PROCESSING_TIME = 60; // 60 ticks or 3 seconds

    private final ItemStacksResourceHandler inputInventory = new SluiceItemHandler();
    private final EmittingEnergy energyStorage = new EmittingEnergy(100_000, _ -> setChanged());
    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> outputCache;
    private int processingProgress = 0;
    private int processingTime = 0;
    private boolean itemSyncNeeded;
    private boolean fluidSyncNeeded;
    private final FluidStacksResourceHandler fluidTank = new SluiceFluidTank(this, 10_000, _ -> {
        setChanged();
        fluidSyncNeeded = true;
    });
    private ItemStack overflow = ItemStack.EMPTY;
    private final Lazy<SluiceProperties> props = Lazy.of(this::initProps);

    public SluiceBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntitiesRegistry.SLUICE.get(), pos, blockState);
    }

    public SluiceProperties initProps() {
        if (getBlockState().getBlock() instanceof SluiceBlock sb) return sb.getProps();
        throw new IllegalStateException("expected a sluice block at " + getBlockPos() + " !");
    }

    @Override
    public void tickClient(Level clientLevel) {
        super.tickClient(clientLevel);

        if (processingTime > 0 && processingProgress++ > processingTime) {
            processingProgress = 0;
            processingTime = 0;
        }
    }

    @Override
    public void tickServer(ServerLevel serverLevel) {
        if (itemSyncNeeded) {
            syncItemToClients();
            itemSyncNeeded = false;
        }
        if (fluidSyncNeeded) {
            syncFluidTank(false);
            fluidSyncNeeded = false;
        }

        if (!overflow.isEmpty()) {
            // Nothing else happens until the overflow is cleared
            produceOutput(overflow);
        } else {
            SluiceProperties sluiceProperties = props.get();
            if (processingTime > 0) {
                // If we're processing, we need to process, not check for items
                processingProgress++;
                setChanged();

                if (processingProgress > processingTime) {
                    // Process the item
                    processingProgress = 0;
                    processingTime = 0;

                    // Get the recipe
                    getRecipeFor(ItemUtil.getStack(inputInventory, 0)).ifPresent(recipe -> {
                        try (Transaction tx = Transaction.openRoot()) {
                            boolean itemOK = MiscUtil.removeResourceFromSlot(inputInventory, 0, 1, tx) == 1;
                            boolean fluidOK = recipe.value().getFluid().map(fluid -> {
                                // TODO consumption upgrade
                                // This is safe to assume we have the fluid as you can only insert fluid to this tank,
                                //   and we checked it before starting the processing
                                int toDrain = (int) (fluid.amount() * sluiceProperties.fluidMod().get());
                                return MiscUtil.removeResourceFromSlot(fluidTank, 0, toDrain, tx) == toDrain;
                            }).orElse(true);
                            boolean energyOK = energyStorage.extract(sluiceProperties.energyCost().get(), tx) == sluiceProperties.energyCost().get();

                            if (itemOK && fluidOK && energyOK) {
                                // All good!
                                tx.commit();
                                for (var result : recipe.value().getResults()) {
                                    // TODO luck upgrade?
                                    if (serverLevel.getRandom().nextFloat() <= result.chance()) {
                                        produceOutput(result.item().create());
                                    }
                                }
                            }
                        }
                    });
                }
            } else {
                ItemStack inputStack = ItemUtil.getStack(inputInventory, 0);
                if (!inputStack.isEmpty()) {
                    setChanged();
                    getRecipeFor(inputStack).ifPresentOrElse(
                            recipe -> {
                                // Recipe found, but also make sure there's enough fluid and (possibly) energy in the sluice
                                if (hasEnoughEnergy() && recipe.value().testFluid(FluidUtil.getStack(fluidTank, 0), true, sluiceProperties.fluidMod().get())) {
                                    // TODO speed upgrade?
                                    double time = BASE_PROCESSING_TIME * sluiceProperties.timeMod().get() * recipe.value().getProcessingTimeMultiplier();
                                    processingTime = Math.max(1, (int) time);
                                    processingProgress = 0;
                                    PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) getLevel(),
                                            ChunkPos.containing(getBlockPos()), new SendSluiceStartPacket(getBlockPos(), processingTime));
                                }
                            },
                            () -> {
                                // No recipe found, not sure how we got here, maybe a hopper? Let's just pop the resource back out
                                produceOutput(inputStack);
                                // Clear the slot
                                inputInventory.set(0, ItemResource.EMPTY, 0);
                            }
                    );
                }
            }
        }
    }

    private boolean hasEnoughEnergy() {
        return energyStorage.getAmountAsInt() >= props.get().energyCost().get();
    }

    private void setOverflowItem(ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(overflow, stack)) {
            setChanged();
        }
        overflow = stack;
    }

    private void produceOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        stack = stack.copy();

        // See if there is an inventory at the end of the sluice block
        assert level instanceof ServerLevel;
        var inventory = getOutputInventory();
        if (inventory != null) {
            // there is an inventory to push to, hopefully it has room...
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = ResourceHandlerUtil.insertStacking(inventory, ItemResource.of(stack), stack.count(), tx);
                if (inserted > 0) {
                    tx.commit();
                }
                if (inserted < stack.count()) {
                    // can't push some or all of the itemstack - mark it as overflow, which stops processing until it's cleared
                    setOverflowItem(stack.copyWithCount(stack.count() - inserted));
                } else {
                    // all pushed!
                    setOverflowItem(ItemStack.EMPTY);
                }
            }
        } else if (!stack.isEmpty()) {
            // there's no inventory to push, drop the result in-world
            BlockPos pos = worldPosition.relative(this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), 2);
            double my = 0.14D * (level.getRandom().nextFloat() * 0.4D);

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);

            itemEntity.setDeltaMovement(0, my, 0);
            level.addFreshEntity(itemEntity);

            setOverflowItem(ItemStack.EMPTY);
        }
    }

    public int getProgress() {
        return processingProgress;
    }

    @Override
    protected void dropItemContents() {
        super.dropItemContents();

        assert getLevel() != null;

        Block.popResource(getLevel(), getBlockPos(), getBlockState().getValue(SluiceBlock.MESH).createItemStack());

        if (!overflow.isEmpty()) {
            Block.popResource(getLevel(), getBlockPos(), overflow);
        }
    }

    @Nullable
    private ResourceHandler<ItemResource> getOutputInventory() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (outputCache == null) {
            Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            outputCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, serverLevel,
                    getBlockPos().relative(facing, 2), facing.getOpposite());
        }
        return outputCache.getCapability();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("processingProgress", processingProgress);
        output.putInt("processingTime", processingTime);

        output.putChild("FluidTank", fluidTank);
        output.putChild("InputInv", inputInventory);
        if (energyStorage.getAmountAsInt() > 0) output.putChild("Energy", energyStorage);

        if (!overflow.isEmpty()) {
            output.store("Overflow", ItemStack.CODEC, overflow);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        processingProgress = input.getInt("processingProgress").orElse(0);
        processingTime = input.getInt("processingTime").orElse(0);

        fluidTank.deserialize(input.childOrEmpty("FluidTank"));
        inputInventory.deserialize(input.childOrEmpty("InputInv"));
        energyStorage.deserialize(input.childOrEmpty("Energy"));

        overflow = input.read("Overflow", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        loadAdditional(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        saveAdditional(output);
        return output.buildResult();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction dir) {
        return dir == null || props.get().itemIO().get() ? inputInventory : null;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction dir) {
        // pumps can insert regardless of the sluice fluid IO ability
        return dir == null || props.get().fluidIO().get() || level.getBlockState(getBlockPos().relative(dir)).getBlock() instanceof PumpBlock ? fluidTank : null;
    }

    @Override
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction dir) {
        return dir == null || props.get().energyCost().get() > 0 ? energyStorage : null;
    }

    public ItemStack getDisplayedItem() {
        return inputInventory.getResource(0).toStack();
    }

    public Optional<RecipeHolder<SluiceRecipe>> getRecipeFor(ItemStack input) {
        return RecipeCaches.SLUICE.getCachedRecipe(level, l -> this.searchForRecipe(l, input), () -> this.genRecipeHash(input));
    }

    private int genRecipeHash(ItemStack input) {
        int fluidHash = FluidStack.hashFluidAndComponents(fluidTank.getResource(0).toStack(fluidTank.getAmountAsInt(0)));
        int itemHash = ItemStack.hashItemAndComponents(input);

        return Objects.hash(fluidHash, itemHash, getInstalledMesh());
    }

    private Optional<RecipeHolder<SluiceRecipe>> searchForRecipe(Level level, ItemStack input) {
        return RecipesRegistry.SLUICE_TYPE.get().streamRecipes(level)
                .filter(r -> fluidItemAndMeshMatch(r.value(), input))
                .findFirst();
    }

    private boolean fluidItemAndMeshMatch(SluiceRecipe recipe, ItemStack input) {
        return recipe.getIngredient().test(input)
                && recipe.testFluid(FluidUtil.getStack(fluidTank, 0), false)
                && recipe.getMeshTypes().contains(getInstalledMesh());
    }

    private MeshType getInstalledMesh() {
        return getBlockState().getValue(SluiceBlock.MESH);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        handleUpdateTag(valueInput);
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getProcessingProgress() {
        return processingProgress;
    }

    public void syncItemToClients() {
        if (getLevel() instanceof ServerLevel sl) {
            PacketDistributor.sendToPlayersTrackingChunk(sl, ChunkPos.containing(getBlockPos()), SyncDisplayItemPacket.forSluice(this));
        }
    }

    @Override
    public void syncItemFromServer(ItemStack stack) {
        inputInventory.set(0, ItemResource.of(stack), stack.count());
    }

    @Override
    public void syncFluidFromServer(FluidStack fluidStack) {
        fluidTank.set(0, FluidResource.of(fluidStack), fluidStack.amount());
    }

    public FluidStacksResourceHandler getFluidTank() {
        // used by sluice renderer and the Pump for direct access to the sluice's fluid
        // everyone else should use capability access via getFluidHandler() !
        return fluidTank;
    }

    public void syncProcessingTimeFromServer(int processingTime) {
        this.processingProgress = 0;
        this.processingTime = processingTime;
    }

    //#region BlockEntity types
    //#region Wood Types
//    public static class Oak extends SluiceBlockEntity {
//        public Oak(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.OAK_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Spruce extends SluiceBlockEntity {
//        public Spruce(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.SPRUCE_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Birch extends SluiceBlockEntity {
//        public Birch(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.BIRCH_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Jungle extends SluiceBlockEntity {
//        public Jungle(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.JUNGLE_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Acacia extends SluiceBlockEntity {
//        public Acacia(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.ACACIA_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class DarkOak extends SluiceBlockEntity {
//        public DarkOak(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.DARK_OAK_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Mangrove extends SluiceBlockEntity {
//        public Mangrove(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.MANGROVE_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Cherry extends SluiceBlockEntity {
//        public Cherry(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.CHERRY_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class PaleOak extends SluiceBlockEntity {
//        public PaleOak(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.PALE_OAK_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Crimson extends SluiceBlockEntity {
//        public Crimson(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.CRIMSON_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Warped extends SluiceBlockEntity {
//        public Warped(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.WARPED_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Bamboo extends SluiceBlockEntity {
//        public Bamboo(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.BAMBOO_SLUICE.get(), pos, blockState);
//        }
//    }
//    //#endregion
//
//    public static class Iron extends SluiceBlockEntity {
//        public Iron(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.IRON_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Diamond extends SluiceBlockEntity {
//        public Diamond(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.DIAMOND_SLUICE.get(), pos, blockState);
//        }
//    }
//
//    public static class Netherite extends SluiceBlockEntity {
//        public Netherite(BlockPos pos, BlockState blockState) {
//            super(BlockEntitiesRegistry.NETHERITE_SLUICE.get(), pos, blockState);
//        }
//    }
    //#endregion

    public static class SluiceFluidTank extends EmittingFluidTank {
        private final SluiceBlockEntity owner;

        public SluiceFluidTank(SluiceBlockEntity owner, int capacity, Consumer<EmittingFluidTank> onChange) {
            super(capacity, onChange);
            this.owner = owner;
        }

        public SluiceBlockEntity getOwner() {
            return owner;
        }
    }

    private class SluiceItemHandler extends ItemStacksResourceHandler {
        public SluiceItemHandler() {
            super(1);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);

            itemSyncNeeded = true;
            setChanged();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return getRecipeFor(resource.toStack()).isPresent();
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }
    }
}
