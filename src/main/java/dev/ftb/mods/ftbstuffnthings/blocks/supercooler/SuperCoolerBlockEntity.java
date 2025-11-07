package dev.ftb.mods.ftbstuffnthings.blocks.supercooler;

import com.google.common.collect.Sets;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProcessorContainerData;
import dev.ftb.mods.ftbstuffnthings.blocks.FluidEnergyProvider;
import dev.ftb.mods.ftbstuffnthings.blocks.ProgressProvider;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingEnergy;
import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import dev.ftb.mods.ftbstuffnthings.capabilities.IOStackHandler;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SuperCoolerRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SuperCoolerBlockEntity extends AbstractMachineBlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    private final EmittingEnergy energyHandler = new EmittingEnergy(1_000_000, 10_000, 10_000, (energy) -> setChanged());
    private final EmittingFluidTank fluidHandler = new EmittingFluidTank(10000, (tank) -> setChanged());
    private final IOStackHandler itemHandler = new IOStackHandler(3, 1, (container, ioType) -> itemHandlerChanged(ioType));

    private final FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    private int progress = 0;
    private int progressRequired = 0;
    private boolean recheckRecipe = false;
    private RecipeHolder<SuperCoolerRecipe> currentRecipe = null;
    private ResourceLocation pendingRecipeId = null;  // set when loading from NBT
    boolean tickLock = false;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.SUPER_COOLER.get(), pos, state);
    }

    @Override
    public IOStackHandler getItemHandler(@Nullable Direction side) {
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

    private SuperCoolerRecipe getCurrentRecipe() {
        return Objects.requireNonNull(currentRecipe).value();
    }

    private void itemHandlerChanged(IOStackHandler.IO ioType) {
        if (!level.isClientSide) {
            setChanged();
            if (ioType == IOStackHandler.IO.INPUT) {
                recheckRecipe = true;
            }
        }
    }

    @Override
    public void tickServer(ServerLevel serverLevel) {
        if (tickLock) {
            return;
        }

        if (!hasEnoughEnergy() || !hasAnyFluid() || !hasItemInAnySlot()) {
            setActive(false);
            progress = 0;
            return;
        }

        if (pendingRecipeId != null) {
            serverLevel.getRecipeManager().byKey(pendingRecipeId).ifPresent(r -> {
                if (r.value() instanceof SuperCoolerRecipe s) {
                    currentRecipe = new RecipeHolder<>(r.id(), s);
                }
            });
            pendingRecipeId = null;
        }

        if (recheckRecipe || progress == 0) {
            recheckRecipe = false;

            currentRecipe = RecipeCaches.SUPER_COOLER.getCachedRecipe(this::findValidRecipe, this::genIngredientHash)
                    .orElse(null);

            if (currentRecipe == null) {
                progress = 0;
                setActive(false);
                return;
            }

            progress = Math.max(1, progress);
            progressRequired = getCurrentRecipe().getEnergyComponent().ticksToProcess();
        }

        if (currentRecipe != null) {
            if (progress == progressRequired && canAcceptOutput(getCurrentRecipe())) {
                executeRecipe();
            } else if (progress < progressRequired) {
                if (getCurrentRecipe().getFluidInput().test(fluidHandler.getFluid())) {
                    // Use energy
                    setActive(true);
                    useEnergy();
                    progress++;
                } else {
                    setActive(false);
                }
            }
        }
    }

    private Optional<RecipeHolder<SuperCoolerRecipe>> findValidRecipe() {
        return level.getRecipeManager().getAllRecipesFor(RecipesRegistry.SUPER_COOLER_TYPE.get()).stream()
                .sorted((a, b) -> b.value().getInputs().size() - a.value().getInputs().size())  // prioritise recipes with more ingredients
                .filter(r -> r.value().test(itemHandler, fluidHandler.getFluid()))
                .findFirst();
    }

    private int genIngredientHash() {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                l.add(ItemStack.hashItemAndComponents(itemHandler.getStackInSlot(i)));
            }
        }
        l.add(FluidStack.hashFluidAndComponents(fluidHandler.getFluid()));
        return l.hashCode();
    }

    public void executeRecipe() {
        if (currentRecipe == null) {
            resetProgress();
            return;
        }

        // Ensure enough fluid (SizedFluidIngredient test here)
        if (!getCurrentRecipe().getFluidInput().test(fluidHandler.getFluid())) {
            resetProgress();
            return;
        }

        // Ensure the items are OK
        // First test if we can extract the items by simulating and validating the result
        Set<Ingredient> requiredItems = Sets.newIdentityHashSet();
        requiredItems.addAll(getCurrentRecipe().getInputs());

        ItemStackHandler inputHandler = itemHandler.getInput();
        BitSet extractingSlots = new BitSet(inputHandler.getSlots());  // track which slots we need to extract from

        for (var ingredient : requiredItems) {
            for (int i = 0; i < inputHandler.getSlots(); i++) {
                if (!extractingSlots.get(i) && ingredient.test(inputHandler.getStackInSlot(i))) {
                    if (inputHandler.extractItem(i, 1, true).isEmpty()) {
                        // this shouldn't happen, but let's be defensive
                        resetProgress();
                        currentRecipe = null;
                        return;
                    }
                    extractingSlots.set(i);
                }
            }
        }

        // Consume inputs, produce output
        if (extractingSlots.cardinality() == getCurrentRecipe().getInputs().size()) {
            fluidHandler.drain(getCurrentRecipe().getFluidInput().amount(), IFluidHandler.FluidAction.EXECUTE);

            for (int i = 0; i < inputHandler.getSlots(); i++) {
                if (extractingSlots.get(i)) {
                    inputHandler.extractItem(i, 1, false);
                }
            }

            itemHandler.getOutput().insertItem(0, getCurrentRecipe().getResult().copy(), false);
            resetProgress();
        }
    }

    private void useEnergy() {
        if (currentRecipe != null) {
            EnergyRequirement energy = getCurrentRecipe().getEnergyComponent();
            var result = energyHandler.extractEnergy(energy.fePerTick(), true);
            if (result >= energy.fePerTick()) {
                energyHandler.extractEnergy(energy.fePerTick(), false);
            } else {
                resetProgress();
            }
        }
    }

    /**
     * This will always force us back to the start of the recipe
     */
    private void resetProgress() {
        progress = 0;
        progressRequired = 0;
        currentRecipe = null;
        tickLock = false;
    }

    public boolean canAcceptOutput(SuperCoolerRecipe recipe) {
        var outputSlot = itemHandler.getOutput().getStackInSlot(0);

        if (outputSlot.isEmpty()) {
            return true;
        }

        int nItems = currentRecipe == null ? 0 : getCurrentRecipe().getResult().getCount();

        // Do we have room for the result?
        if (outputSlot.getCount() >= outputSlot.getMaxStackSize() - nItems) {
            return false;
        }

        // Are the items the same?
        return ItemStack.isSameItemSameComponents(outputSlot, recipe.getResult());
    }

    private boolean hasAnyFluid() {
        return !fluidHandler.isEmpty();
    }

    private boolean hasEnoughEnergy() {
        return energyHandler.getEnergyStored() > (currentRecipe == null ? 0 : getCurrentRecipe().getEnergyComponent().fePerTick());
    }

    private boolean hasItemInAnySlot() {
        var input = itemHandler.getInput();
        for (int i = 0; i < input.getSlots(); i++) {
            if (!input.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ftbstuff.super_cooler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (player instanceof ServerPlayer sp) {
            fluidHandler.needSync(sp);
        }
        return new SuperCoolerMenu(containerId, inventory, getBlockPos());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        itemHandler.getInput().deserializeNBT(provider, tag.getCompound("input"));
        itemHandler.getOutput().deserializeNBT(provider, tag.getCompound("output"));
        if (tag.contains("energy")) {
            energyHandler.deserializeNBT(provider, tag.get("energy"));
        }
        fluidHandler.readFromNBT(provider, tag.getCompound("fluid"));

        // Write the progress
        progress = tag.getInt("progress");
        progressRequired = tag.getInt("progressRequired");

        // Write the recipe id
        if (tag.contains("recipe")) {
            try {
                pendingRecipeId = ResourceLocation.parse(tag.getString("recipe"));
            } catch (ResourceLocationException e) {
                pendingRecipeId = null;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("input", itemHandler.getInput().serializeNBT(provider));
        tag.put("output", itemHandler.getOutput().serializeNBT(provider));
        tag.put("energy", energyHandler.serializeNBT(provider));
        tag.put("fluid", fluidHandler.writeToNBT(provider, new CompoundTag()));

        // Write the progress
        tag.putInt("progress", progress);
        tag.putInt("progressRequired", progressRequired);

        // Write the recipe id
        if (currentRecipe != null) {
            tag.putString("recipe", currentRecipe.id().toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return Util.make(new CompoundTag(), t -> saveAdditional(t, provider));
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
    public void setEnergy(int energy) {
        energyHandler.overrideEnergy(energy);
    }

    @Override
    public void setFluid(FluidStack fluid) {
        fluidHandler.setFluid(fluid);
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
    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    public void syncFluidFromServer(FluidStack fluidStack) {
        fluidHandler.setFluid(fluidStack);
    }
}
