package dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProcessorContainerData;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProvider;
import dev.ftb.mods.ftbstuffnthings.blocks.ProgressProvider;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingEnergy;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingStackHandler;
import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.FusingMachineRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FusingMachineBlockEntity extends AbstractMachineBlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    private final EmittingEnergy energyHandler = new EmittingEnergy(1_000_000, 10_000, 10_000, (energy) -> setChanged());
    private final ExtractOnlyFluidTank fluidHandler = new ExtractOnlyFluidTank(10000, _ -> setChanged());
    private final EmittingStackHandler itemHandler = new EmittingStackHandler(2, _ -> onItemHandlerChange());

    private int progress = 0;
    private int progressRequired = 0;
    private boolean recheckRecipe = false;
    @Nullable
    private FusingMachineRecipe currentRecipe = null;
    private final FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    public FusingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.FUSING_MACHINE.get(), pos, state);
    }

    @Override
    public void tickServer(ServerLevel serverLevel) {
        if (!hasEnoughEnergy() || !hasOccupiedInputSlots()) {
            resetProgress(true);
            return;
        }

        // We need to find the recipe before we can check the fluid tank
        if (recheckRecipe || progress == 0) {
            recheckRecipe = false;

            currentRecipe = RecipeCaches.FUSING_MACHINE.getCachedRecipe(serverLevel, this::searchForRecipe, this::genIngredientHash)
                    .map(RecipeHolder::value)
                    .orElse(null);

            if (currentRecipe == null || fluidHandler.getAmountAsInt(0) > 0 && !FluidStack.isSameFluidSameComponents(FluidUtil.getStack(fluidHandler, 0), currentRecipe.getFluidResult())) {
                resetProgress(true);
                return;
            }

            // Good, we can start the process
            progress = Math.max(1, progress);
            progressRequired = currentRecipe.getEnergyComponent().ticksToProcess();
        }

        if (currentRecipe != null) {
            if (progress == progressRequired) {
                if (canAcceptOutput()) {
                    // We're done... Output the result
                    executeRecipe();
                } else {
                    // not enough space for output fluid; go inactive but keep progress
                    setActive(false);
                }
            } else if (progress < progressRequired) {
                setActive(true);
                useEnergy();
                progress++;
            }
        }
    }

    private Optional<RecipeHolder<FusingMachineRecipe>> searchForRecipe(ServerLevel serverLevel) {
        return serverLevel.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.FUSING_MACHINE_TYPE.get(), NoInventory.INSTANCE, serverLevel)
                .sorted((h1, h2) -> h2.value().getInputs().size() - h1.value().getInputs().size()) // prioritise recipes with more ingredients
                .filter(holder -> holder.value().test(itemHandler))
                .findFirst();
    }

    private int genIngredientHash() {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < itemHandler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(itemHandler, i);
            if (!stack.isEmpty()) {
                l.add(ItemStack.hashItemAndComponents(stack));
            }
        }
        return l.hashCode();
    }

    private void onItemHandlerChange() {
        if (!level.isClientSide()) {
            setChanged();
            recheckRecipe = true;
        }
    }

    private boolean canAcceptOutput() {
        return currentRecipe != null && currentRecipe.getFluidResult().amount() + fluidHandler.getAmountAsInt(0) <= fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY);
    }

    //#region BlockEntity processing

    private void executeRecipe() {
        assert currentRecipe != null;

        BitSet extractingSlots = new BitSet(itemHandler.size());  // track which slots we need to extract from

        // Determine which input slots should be extracted from
        for (var ingredient : currentRecipe.getInputs()) {
            for (int i = 0; i < itemHandler.size(); i++) {
                if (!extractingSlots.get(i) && ingredient.test(ItemUtil.getStack(itemHandler, i))) {
                    extractingSlots.set(i);
                }
            }
        }

        // Do the actual extraction and fluid production
        if (extractingSlots.cardinality() == currentRecipe.getInputs().size()) {
            try (Transaction tx = Transaction.openRoot()) {
                for (int i = 0; i < itemHandler.size(); i++) {
                    if (extractingSlots.get(i)) {
                        if (itemHandler.extract(i, itemHandler.getResource(i), 1, tx) != 1) {
                            // shouldn't happen...
                            tx.close();
                            return;
                        }
                    }
                }
                FluidStackTemplate fluidResult = currentRecipe.getFluidResult();
                if (fluidHandler.insert(FluidResource.of(fluidResult.fluid()), fluidResult.amount(), tx) == fluidResult.amount()) {
                    resetProgress(false);
                    tx.commit();
                }
            }
        } else {
            setActive(true);
        }
    }

    private void useEnergy() {
        if (currentRecipe == null) {
            return;
        }

        try (Transaction tx = Transaction.openRoot()) {
            int result = energyHandler.extract(currentRecipe.getEnergyComponent().fePerTick(), tx);
            if (result < currentRecipe.getEnergyComponent().fePerTick()) {
                resetProgress(true);
            } else {
                tx.commit();
            }
        }
    }

    private void resetProgress(boolean goInactive) {
        progress = 0;
        progressRequired = 0;
        if (goInactive) {
            setActive(false);
        }
    }

    private boolean hasEnoughEnergy() {
        return energyHandler.getAmountAsInt() > (currentRecipe == null ? 0 : currentRecipe.getEnergyComponent().fePerTick());
    }

    private boolean hasOccupiedInputSlots() {
        for (int i = 0; i < itemHandler.size(); i++) {
            if (!itemHandler.getResource(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

//#endregion

//#region BlockEntity setup and syncing

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        if (player instanceof ServerPlayer sp) {
            fluidHandler.needSync(sp);
        }
        return new FusingMachineMenu(windowId, inventory, getBlockPos());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input.childOrEmpty("input"));
        energyHandler.deserialize(input.childOrEmpty("energy"));
        fluidHandler.deserialize(input.childOrEmpty("fluid"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("input", itemHandler);
        output.putChild("energy", energyHandler);
        output.putChild("fluid", fluidHandler);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(output);
        return output.buildResult();
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        loadAdditional(input);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);

        fluidHandler.overrideFluidStack(components.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy());
        energyHandler.overrideEnergy(components.getOrDefault(ComponentsRegistry.STORED_ENERGY, 0));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        components.set(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(fluidHandler.copyStack()));
        components.set(ComponentsRegistry.STORED_ENERGY, energyHandler.getAmountAsInt());
    }

    @Override
    public void syncFluidFromServer(FluidStack fluidStack) {
        fluidHandler.overrideFluidStack(fluidStack);
    }

//#endregion

//#region Data Syncing helper methods

    @Override
    public int getEnergy() {
        return energyHandler.getAmountAsInt();
    }

    @Override
    public int getMaxEnergy() {
        return energyHandler.getCapacityAsInt();
    }

    @Override
    public FluidStack getFluid() {
        return fluidHandler.copyStack();
    }

    @Override
    public int getMaxFluid() {
        return fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY);
    }

    @Override
    public void setFluid(FluidStack fluid) {
        fluidHandler.overrideFluidStack(fluid);
    }

    @Override
    public void setEnergy(int energy) {
        energyHandler.overrideEnergy(energy);
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public int getMaxProgress() {
        return progressRequired;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public void setMaxProgress(int maxProgress) {
        this.progressRequired = maxProgress;
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    @Override
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    @Override
    public ContainerData getContainerData() {
        return containerData;
    }

    public void indexModifier(int index, ItemResource resource, int amount) {
        itemHandler.set(index, resource, amount);
    }

//#endregion

    public static class ExtractOnlyFluidTank extends EmittingFluidTank {
        public ExtractOnlyFluidTank(int capacity, Consumer<EmittingFluidTank> listener) {
            super(capacity, listener);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int insert(FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        private int insertOverride(FluidResource resource, int amount, TransactionContext transactionContext) {
            return super.insert(resource, amount, transactionContext);
        }

        public void overrideFluidStack(FluidStack stack) {
            set(0, FluidResource.of(stack), stack.amount());
        }
    }
}
