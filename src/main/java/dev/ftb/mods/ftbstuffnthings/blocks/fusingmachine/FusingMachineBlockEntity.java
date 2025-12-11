package dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProcessorContainerData;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProvider;
import dev.ftb.mods.ftbstuffnthings.blocks.ProgressProvider;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingEnergy;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingStackHandler;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.FusingMachineRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FusingMachineBlockEntity extends AbstractMachineBlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    private final EmittingEnergy energyHandler = new EmittingEnergy(1_000_000, 10_000, 10_000, (energy) -> setChanged());
    private final ExtractOnlyFluidTank fluidHandler = new ExtractOnlyFluidTank(10000, (tank) -> setChanged());
    private final EmittingStackHandler itemHandler = new EmittingStackHandler(2, (contents) -> onItemHandlerChange());

    private int progress = 0;
    private int progressRequired = 0;
    private boolean recheckRecipe = false;
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

            currentRecipe = RecipeCaches.FUSING_MACHINE.getCachedRecipe(this::searchForRecipe, this::genIngredientHash)
                    .map(RecipeHolder::value)
                    .orElse(null);

            if (currentRecipe == null || !fluidHandler.isEmpty() && !FluidStack.isSameFluidSameComponents(fluidHandler.getFluid(), currentRecipe.getFluidResult())) {
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

    private Optional<RecipeHolder<FusingMachineRecipe>> searchForRecipe() {
        return level.getRecipeManager().getAllRecipesFor(RecipesRegistry.FUSING_MACHINE_TYPE.get()).stream()
                .sorted((h1, h2) -> h2.value().getInputs().size() - h1.value().getInputs().size()) // prioritise recipes with more ingredients
                .filter(holder -> holder.value().test(itemHandler))
                .findFirst();
    }

    private int genIngredientHash() {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                l.add(ItemStack.hashItemAndComponents(itemHandler.getStackInSlot(i)));
            }
        }
        return l.hashCode();
    }

    private void onItemHandlerChange() {
        if (!level.isClientSide) {
            setChanged();
            recheckRecipe = true;
        }
    }

    private boolean canAcceptOutput() {
        return currentRecipe != null && currentRecipe.getFluidResult().getAmount() + fluidHandler.getFluidAmount() <= fluidHandler.getCapacity();
    }

    //#region BlockEntity processing

    private void executeRecipe() {
        BitSet extractingSlots = new BitSet(itemHandler.getSlots());  // track which slots we need to extract from

        // Determine which input slots should be extracted from
        for (var ingredient : currentRecipe.getInputs()) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (!extractingSlots.get(i) && ingredient.test(itemHandler.getStackInSlot(i))) {
                    if (itemHandler.extractItem(i, 1, true).isEmpty()) {
                        // this shouldn't happen, but let's be defensive
                        resetProgress(true);
                        currentRecipe = null;
                        return;
                    }
                    extractingSlots.set(i);
                }
            }
        }

        // Do the actual extraction and fluid production
        if (extractingSlots.cardinality() == currentRecipe.getInputs().size()) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (extractingSlots.get(i)) {
                    itemHandler.extractItem(i, 1, false);
                }
            }
            fluidHandler.fillInternal(currentRecipe.getFluidResult(), IFluidHandler.FluidAction.EXECUTE);
            resetProgress(false);
        } else {
            setActive(true);
        }
    }

    private void useEnergy() {
        if (currentRecipe == null) {
            return;
        }

        var result = energyHandler.extractEnergy(currentRecipe.getEnergyComponent().fePerTick(), true);
        if (result < currentRecipe.getEnergyComponent().fePerTick()) {
            resetProgress(true);
            return;
        }

        energyHandler.extractEnergy(currentRecipe.getEnergyComponent().fePerTick(), false);
    }

    private void resetProgress(boolean goInactive) {
        progress = 0;
        progressRequired = 0;
        if (goInactive) {
            setActive(false);
        }
    }

    private boolean hasEnoughEnergy() {
        return energyHandler.getEnergyStored() > (currentRecipe == null ? 0 : currentRecipe.getEnergyComponent().fePerTick());
    }

    private boolean hasOccupiedInputSlots() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        itemHandler.deserializeNBT(provider, tag.getCompound("input"));
        if (tag.contains("energy")) {
            energyHandler.deserializeNBT(provider, tag.get("energy"));
        }
        fluidHandler.readFromNBT(provider, tag.getCompound("fluid"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("input", itemHandler.serializeNBT(provider));
        tag.put("energy", energyHandler.serializeNBT(provider));
        tag.put("fluid", fluidHandler.writeToNBT(provider, new CompoundTag()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        saveAdditional(compoundTag, provider);
        return compoundTag;
    }


    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        fluidHandler.setFluid(componentInput.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy());
        energyHandler.overrideEnergy(componentInput.getOrDefault(ComponentsRegistry.STORED_ENERGY, 0));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        components.set(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(fluidHandler.getFluid()));
        components.set(ComponentsRegistry.STORED_ENERGY, energyHandler.getEnergyStored());
    }

    @Override
    public void syncFluidFromServer(FluidStack fluidStack) {
        fluidHandler.setFluid(fluidStack);
    }

//#endregion

//#region Data Syncing helper methods

    @Override
    public int getEnergy() {
        return energyHandler.getEnergyStored();
    }

    @Override
    public int getMaxEnergy() {
        return energyHandler.getMaxEnergyStored();
    }

    @Override
    public FluidStack getFluid() {
        return fluidHandler.getFluid();
    }

    @Override
    public int getMaxFluid() {
        return fluidHandler.getCapacity();
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
    public EmittingStackHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    @Override
    public ContainerData getContainerData() {
        return containerData;
    }

//#endregion

    public static class ExtractOnlyFluidTank extends EmittingFluidTank {
        public ExtractOnlyFluidTank(int capacity, Consumer<EmittingFluidTank> listener) {
            super(capacity, listener);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        public int fillInternal(FluidStack resource, FluidAction action) {
            return super.fill(resource, action);
        }

        public void overrideFluidStack(FluidStack stack) {
            fluid = stack;
        }
    }
}
