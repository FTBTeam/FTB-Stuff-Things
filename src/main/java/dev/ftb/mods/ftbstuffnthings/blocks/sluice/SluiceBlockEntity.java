package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import dev.ftb.mods.ftbstuffnthings.capabilities.PublicReadOnlyFluidTank;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SluiceRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SluiceBlockEntity extends BlockEntity {
    private static final float BASE_PROCESSING_TIME = 60; // 60 ticks or 3 seconds

    private final Lazy<PublicReadOnlyFluidTank> fluidTank = Lazy.of(() -> new PublicReadOnlyFluidTank(this, 10_000));
    private final Lazy<ItemStackHandler> inputInventory = Lazy.of(() -> new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    });

    private boolean processing = false;
    private int processingProgress = 0;
    private int processingTime = 0;

    public SluiceBlockEntity(BlockEntityType<?> entity, BlockPos pos, BlockState blockState) {
        super(entity, pos, blockState);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T tileEntity) {
        if (!(tileEntity instanceof SluiceBlockEntity sluice)) {
            return;
        }

        // Step one, are we processing, if we're processing, we need to process, not check for items
        if (sluice.processing) {
            sluice.processingProgress++;
            System.out.println("Processing progress: " + sluice.processingProgress + " / " + sluice.processingTime);
            if (sluice.processingProgress > sluice.processingTime) {
                sluice.processing = false;
                sluice.processingProgress = 0;
                sluice.processingTime = 0;
                // Process the item

                // Take the item from the input inventory
                ItemStack inputStack = sluice.inputInventory.get().getStackInSlot(0);
                inputStack.shrink(1);
                sluice.setChanged();

                // Get the recipe
                var recipe = sluice.getRecipeFor(inputStack);
                if (recipe.isEmpty()) {
                    return; // How did we get here?
                }

                // Get the results
                var results = recipe.get().value().getResults();
                for (var result : results) {
                    if (level.random.nextFloat() <= result.chance()) {
                        sluice.dropItemOrPushToInventory(result.item());
                    }
                }

                // And we're done
                return;
            }

            return;
        }

        // We're not processing, this means we need to find items in our input inventory and try and process them
        ItemStack inputStack = sluice.inputInventory.get().getStackInSlot(0);
        if (inputStack.isEmpty()) {
            // Nothing to do!
            return;
        }

        // We have an item, let's try and process it
        var recipe = sluice.getRecipeFor(inputStack);
        if (recipe.isEmpty()) {
            // No recipe found, not sure how we got here, maybe a hopper? Let's just pop the resource back out
            sluice.dropItemOrPushToInventory(inputStack);
            // Clear the slot
            sluice.inputInventory.get().setStackInSlot(0, ItemStack.EMPTY);
            return;
        }

        // We have a recipe, let's start processing
        sluice.processing = true;
        sluice.processingTime = (int) (BASE_PROCESSING_TIME * recipe.get().value().getProcessingTimeMultiplier());
        sluice.processingProgress = 0; // Just in case
    }

    private void dropItemOrPushToInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        stack = stack.copy();

        // See if there is an inventory at the end of the sluice block
        assert level != null;
        var inventory = availableInventory(level);

        if (inventory != null) {
            stack = ItemHandlerHelper.insertItem(inventory, stack, false);
        }

        if (!stack.isEmpty()) {
            BlockPos pos = this.worldPosition.relative(this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), 2);
            level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);

            double my = 0.14D * (level.random.nextFloat() * 0.4D);

            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
//
//            itemEntity.setNoPickUpDelay();
//            itemEntity.setDeltaMovement(0, my, 0);
            level.addFreshEntity(itemEntity);
        }
    }

    /**
     * @param level level to find the inventory from
     * @return A valid IItemHandler or a empty optional
     */
    @Nullable
    private IItemHandler availableInventory(Level level) {
        BlockPos pos = this.getBlockPos().relative(this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), 2);

        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        fluidTank.get().writeToNBT(registries, tag);

        tag.put("inputInventory", inputInventory.get().serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        fluidTank.get().readFromNBT(registries, tag);

        inputInventory.get().deserializeNBT(registries, tag.getCompound("inputInventory"));
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var data = new CompoundTag();
        this.saveAdditional(data, registries);
        return data;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(pkt.getTag(), lookupProvider);
    }

    public PublicReadOnlyFluidTank getFluidTank() {
        return fluidTank.get();
    }

    public Lazy<ItemStackHandler> getInputInventory() {
        return inputInventory;
    }

    public Optional<RecipeHolder<SluiceRecipe>> getRecipeFor(ItemStack input) {
        return RecipeCaches.SLUICE.getCachedRecipe(() -> this.searchForRecipe(input), () -> this.genRecipeHash(input));
    }

    private int genRecipeHash(ItemStack input) {
        int fluidHash = FluidStack.hashFluidAndComponents(fluidTank.get().getFluid());
        int itemHash = input.hashCode();

        return Objects.hash(fluidHash, itemHash);
    }

    private Optional<RecipeHolder<SluiceRecipe>> searchForRecipe(ItemStack input) {
        return level.getRecipeManager().getRecipesFor(RecipesRegistry.SLUICE_TYPE.get(), NoInventory.INSTANCE, level)
                .stream()
                // Test the fluid and the itemss
                .filter(r -> !fluidAndItemMatch(r.value(), input))
                .findFirst();
    }

    private boolean fluidAndItemMatch(SluiceRecipe recipe, ItemStack input) {
        boolean itemTest = recipe.getIngredient().test(input);

        FluidStack recipeFluid = recipe.getFluid();
        FluidStack tankFluid = fluidTank.get().getFluid();

        if (recipeFluid.isEmpty() && tankFluid.isEmpty()) {
            return itemTest;
        }

        return itemTest && recipeFluid.equals(tankFluid) && tankFluid.getAmount() >= recipeFluid.getAmount();
    }

    //#region BlockEntity types
    public static class Oak extends SluiceBlockEntity {
        public Oak(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.OAK_SLUICE.get(), pos, blockState);
        }
    }

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
}
