package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingEnergy;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SluiceRecipe;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.network.SendSluiceStartPacket;
import dev.ftb.mods.ftbstuffnthings.network.SyncDisplayItemPacket;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class SluiceBlockEntity extends AbstractMachineBlockEntity {
    private static final float BASE_PROCESSING_TIME = 60; // 60 ticks or 3 seconds

    private final ItemStacksResourceHandler inputInventory = new SluiceItemHandler();
    private final EmittingEnergy energyStorage = new EmittingEnergy(100_000, energy -> setChanged());
    private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> outputCache;
    private int processingProgress = 0;
    private int processingTime = 0;
    private boolean itemSyncNeeded;
    private boolean fluidSyncNeeded;
    private final FluidStacksResourceHandler fluidTank = new SluiceFluidTank(this, 10_000, tank -> {
        setChanged();
        fluidSyncNeeded = true;
    });
    private ItemStack overflow = ItemStack.EMPTY;

    public SluiceBlockEntity(BlockEntityType<?> entity, BlockPos pos, BlockState blockState) {
        super(entity, pos, blockState);
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
            dropItemOrPushToInventory(overflow);
        } else if (processingTime > 0) {
            // If we're processing, we need to process, not check for items
            processingProgress++;
            setChanged();

            if (processingProgress > processingTime) {
                // Process the item
                processingProgress = 0;
                processingTime = 0;

                // Take the item from the input inventory
                ItemStack inputStack = inputInventory.extractItem(0, 1, false);

                // Get the recipe
                getRecipeFor(inputStack).ifPresent(recipe -> {
                    recipe.value().getFluid().ifPresent(fluid -> {
                        // TODO consumption upgrade
                        // This is safe to assume we have the fluid as you can only insert fluid to this tank,
                        //   and we checked it before starting the processing
                        fluidTank.drain((int) (fluid.amount() * getProps().fluidMod().get()), IFluidHandler.FluidAction.EXECUTE);
                    });

                    energyStorage.extractEnergy(getProps().energyCost().get(), false);

                    for (var result : recipe.value().getResults()) {
                        // TODO luck upgrade
                        if (serverLevel.getRandom().nextFloat() <= result.chance()) {
                            dropItemOrPushToInventory(result.item());
                        }
                    }
                });
            }
        } else {
            ItemStack inputStack = inputInventory.getStackInSlot(0);
            if (!inputStack.isEmpty()) {
                setChanged();
                getRecipeFor(inputStack).ifPresentOrElse(
                        recipe -> {
                            // Recipe found, but also make sure there's enough fluid and (possibly) energy in the sluice
                            if (hasEnoughEnergy() && recipe.value().testFluid(fluidTank.getFluid(), true, getProps().fluidMod().get())) {
                                // TODO speed upgrade
                                double time = BASE_PROCESSING_TIME * getProps().timeMod().get() * recipe.value().getProcessingTimeMultiplier();
                                processingTime = Math.max(1, (int) time);
                                processingProgress = 0;
                                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) getLevel(),
                                        new ChunkPos(getBlockPos()), new SendSluiceStartPacket(getBlockPos(), processingTime));
                            }
                        },
                        () -> {
                            // No recipe found, not sure how we got here, maybe a hopper? Let's just pop the resource back out
                            dropItemOrPushToInventory(inputStack);
                            // Clear the slot
                            inputInventory.set(0, ItemResource.EMPTY, 0);
                        }
                );
            }
        }
    }

    private boolean hasEnoughEnergy() {
        return energyStorage.getAmountAsInt() >= getProps().energyCost().get();
    }

    private void setOverflowItem(ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(overflow, stack)) {
            setChanged();
        }
        overflow = stack;
    }

    private void dropItemOrPushToInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        stack = stack.copy();

        // See if there is an inventory at the end of the sluice block
        assert level instanceof ServerLevel;
        var inventory = getOutputInventory();
        if (inventory != null) {
            stack = ItemHandlerHelper.insertItem(inventory, stack, false);
            if (!stack.isEmpty()) {
                // can't push to inventory? mark it as overflow, which stops processing until it's cleared
                setOverflowItem(stack);
                return;
            }
        }

        if (!stack.isEmpty()) {
            BlockPos pos = worldPosition.relative(this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), 2);
            double my = 0.14D * (level.random.nextFloat() * 0.4D);

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);

            itemEntity.setDeltaMovement(0, my, 0);
            level.addFreshEntity(itemEntity);
        }

        // if we got here, the output was cleared, one way or another
        setOverflowItem(ItemStack.EMPTY);
    }

    public int getProgress() {
        return processingProgress;
    }

    @Override
    protected void dropItemContents() {
        super.dropItemContents();

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
        var tag = super.getUpdateTag(registries);
        this.saveAdditional(TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries));
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public SluiceProperties getProps() {
        if (getBlockState().getBlock() instanceof SluiceBlock sb) return sb.getProps();
        throw new IllegalStateException("expected a sluice block at " + getBlockPos() + " !");
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction dir) {
        return dir == null || getProps().itemIO().get() ? inputInventory : null;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction dir) {
        // pumps can insert regardless of the sluice fluid IO ability
        return dir == null || getProps().fluidIO().get() || level.getBlockState(getBlockPos().relative(dir)).getBlock() instanceof PumpBlock ? fluidTank : null;
    }

    @Override
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction dir) {
        return dir == null || getProps().energyCost().get() > 0 ? energyStorage : null;
    }

    public ItemStack getDisplayedItem() {
        return inputInventory.getResource(0).toStack();
    }

    public Optional<RecipeHolder<SluiceRecipe>> getRecipeFor(ItemStack input) {
        return RecipeCaches.SLUICE.getCachedRecipe(() -> this.searchForRecipe(input), () -> this.genRecipeHash(input));
    }

    private int genRecipeHash(ItemStack input) {
        int fluidHash = FluidStack.hashFluidAndComponents(fluidTank.getResource(0).toStack(fluidTank.getAmountAsInt(0)));
        int itemHash = ItemStack.hashItemAndComponents(input);

        return Objects.hash(fluidHash, itemHash, getInstalledMesh());
    }

    private Optional<RecipeHolder<SluiceRecipe>> searchForRecipe(ItemStack input) {
        assert level instanceof ServerLevel;

        return level.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.SLUICE_TYPE.get(), NoInventory.INSTANCE, level)
                .filter(r -> fluidItemAndMeshMatch(r.value(), input))
                .findFirst();
    }

    private boolean fluidItemAndMeshMatch(SluiceRecipe recipe, ItemStack input) {
        return recipe.getIngredient().test(input)
                && recipe.testFluid(fluidTank.getFluid(), false)
                && recipe.getMeshTypes().contains(getInstalledMesh());
    }

    private @NotNull MeshType getInstalledMesh() {
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
    public static class Oak extends SluiceBlockEntity {
        public Oak(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.OAK_SLUICE.get(), pos, blockState);
        }
    }

    public static class Spruce extends SluiceBlockEntity {
        public Spruce(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.SPRUCE_SLUICE.get(), pos, blockState);
        }
    }

    public static class Birch extends SluiceBlockEntity {
        public Birch(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.BIRCH_SLUICE.get(), pos, blockState);
        }
    }

    public static class Jungle extends SluiceBlockEntity {
        public Jungle(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.JUNGLE_SLUICE.get(), pos, blockState);
        }
    }

    public static class Acacia extends SluiceBlockEntity {
        public Acacia(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.ACACIA_SLUICE.get(), pos, blockState);
        }
    }

    public static class DarkOak extends SluiceBlockEntity {
        public DarkOak(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DARK_OAK_SLUICE.get(), pos, blockState);
        }
    }

    public static class Mangrove extends SluiceBlockEntity {
        public Mangrove(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.MANGROVE_SLUICE.get(), pos, blockState);
        }
    }

    public static class Cherry extends SluiceBlockEntity {
        public Cherry(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.CHERRY_SLUICE.get(), pos, blockState);
        }
    }

    public static class PaleOak extends SluiceBlockEntity {
        public PaleOak(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.PALE_OAK_SLUICE.get(), pos, blockState);
        }
    }

    public static class Crimson extends SluiceBlockEntity {
        public Crimson(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.CRIMSON_SLUICE.get(), pos, blockState);
        }
    }

    public static class Warped extends SluiceBlockEntity {
        public Warped(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.WARPED_SLUICE.get(), pos, blockState);
        }
    }

    public static class Bamboo extends SluiceBlockEntity {
        public Bamboo(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.BAMBOO_SLUICE.get(), pos, blockState);
        }
    }
    //#endregion

    public static class Iron extends SluiceBlockEntity {
        public Iron(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.IRON_SLUICE.get(), pos, blockState);
        }
    }

    public static class Diamond extends SluiceBlockEntity {
        public Diamond(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DIAMOND_SLUICE.get(), pos, blockState);
        }
    }

    public static class Netherite extends SluiceBlockEntity {
        public Netherite(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.NETHERITE_SLUICE.get(), pos, blockState);
        }
    }
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
