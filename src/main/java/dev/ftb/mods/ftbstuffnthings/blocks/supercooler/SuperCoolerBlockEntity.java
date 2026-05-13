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
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SuperCoolerBlockEntity extends AbstractMachineBlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    private final EmittingEnergy energyHandler = new EmittingEnergy(1_000_000, 10_000, 10_000,
            _ -> setChanged());
    private final EmittingFluidTank fluidHandler = new EmittingFluidTank(10000,
            _ -> setChanged());
    private final IOStackHandler itemHandler = new IOStackHandler(3, 1,
            (_, ioType) -> itemHandlerChanged(ioType));

    private final FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    private int progress = 0;
    private int progressRequired = 0;
    private boolean recheckRecipe = false;
    @Nullable
    private RecipeHolder<SuperCoolerRecipe> currentRecipe = null;
    @Nullable
    private ResourceKey<Recipe<?>> pendingRecipeId = null;  // set when loading from NBT
    boolean tickLock = false;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.SUPER_COOLER.get(), pos, state);
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

    private SuperCoolerRecipe getCurrentRecipe() {
        return Objects.requireNonNull(currentRecipe).value();
    }

    private void itemHandlerChanged(IOStackHandler.IO ioType) {
        if (level != null && !level.isClientSide()) {
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
            RecipeHolder<?> holder = serverLevel.getServer().getRecipeManager().recipeMap().byKey(pendingRecipeId);
            if (holder != null && holder.value() instanceof SuperCoolerRecipe s) {
                currentRecipe = new RecipeHolder<>(holder.id(), s);
            }
            pendingRecipeId = null;
        }

        if (recheckRecipe || progress == 0) {
            recheckRecipe = false;

            currentRecipe = RecipeCaches.SUPER_COOLER.getCachedRecipe(serverLevel, this::findValidRecipe, this::genIngredientHash)
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
                if (getCurrentRecipe().getFluidInput().test(FluidUtil.getStack(fluidHandler, 0))) {
                    setActive(true);
                    useEnergy();
                    progress++;
                } else {
                    setActive(false);
                }
            }
        }
    }

    private Optional<RecipeHolder<SuperCoolerRecipe>> findValidRecipe(Level level) {
        return RecipesRegistry.SUPER_COOLER_TYPE.get().streamRecipes(level)
                .sorted((a, b) -> b.value().getInputs().size() - a.value().getInputs().size())  // prioritise recipes with more ingredients
                .filter(r -> r.value().test(itemHandler, fluidHandler.copyStack()))
                .findFirst();
    }

    private int genIngredientHash() {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < itemHandler.size(); i++) {
            l.add(itemHandler.getResource(i).hashCode());
        }
        l.add(fluidHandler.getResource(0).hashCode());
        return l.hashCode();
    }

    public void executeRecipe() {
        if (currentRecipe == null) {
            resetProgress();
            return;
        }

        // Ensure enough fluid (SizedFluidIngredient test here)
        if (!getCurrentRecipe().getFluidInput().test(FluidUtil.getStack(fluidHandler, 0))) {
            resetProgress();
            return;
        }

        // Ensure the items are OK
        // First test if we can extract the items by simulating and validating the result
        Set<Ingredient> requiredItems = Sets.newIdentityHashSet();
        requiredItems.addAll(getCurrentRecipe().getInputs());

        var inputHandler = itemHandler.getInput();
        BitSet extractingSlots = new BitSet(inputHandler.size());  // track which slots we need to extract from

        for (var ingredient : requiredItems) {
            for (int i = 0; i < inputHandler.size(); i++) {
                if (!extractingSlots.get(i) && ingredient.test(ItemUtil.getStack(inputHandler, i))) {
                    if (inputHandler.getAmountAsInt(i) < 1) {
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
        try (Transaction tx = Transaction.openRoot()) {
            var drained = MiscUtil.removeResourceFromSlot(fluidHandler, 0, getCurrentRecipe().getFluidInput().amount(), tx);
            int taken = 0;
            for (int i = 0; i < inputHandler.size(); i++) {
                if (extractingSlots.get(i)) {
                    taken += MiscUtil.removeResourceFromSlot(inputHandler, i, 1, tx);
                }
            }
            int inserted = ResourceHandlerUtil.insertStacking(itemHandler.getOutput(), ItemResource.of(getCurrentRecipe().getResult()), getCurrentRecipe().getResult().count(), tx);

            if (drained == getCurrentRecipe().getFluidInput().amount()
                    && taken == extractingSlots.cardinality()
                    && inserted == getCurrentRecipe().getResult().count()) {
                tx.commit();
            }
            resetProgress();
        }
    }

    private void useEnergy() {
        if (currentRecipe != null) {
            EnergyRequirement energy = getCurrentRecipe().getEnergyComponent();
            try (Transaction tx = Transaction.openRoot()) {
                int taken = energyHandler.extract(energy.fePerTick(), tx);
                if (taken == energy.fePerTick()) {
                    tx.commit();
                } else {
                    resetProgress();
                }
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
        var outputStack = ItemUtil.getStack(itemHandler.getOutput(), 0);
        if (outputStack.isEmpty()) {
            return true;
        }

        int nItems = currentRecipe == null ? 0 : getCurrentRecipe().getResult().count();

        // Do we have room for the result?
        if (outputStack.getCount() >= outputStack.getMaxStackSize() - nItems) {
            return false;
        }

        // Are the items the same?
        return ItemStack.isSameItemSameComponents(outputStack, recipe.getResult());
    }

    private boolean hasAnyFluid() {
        return !fluidHandler.getResource(0).isEmpty();
    }

    private boolean hasEnoughEnergy() {
        return energyHandler.getAmountAsInt() >= (currentRecipe == null ? 0 : getCurrentRecipe().getEnergyComponent().fePerTick());
    }

    private boolean hasItemInAnySlot() {
        var input = itemHandler.getInput();
        for (int i = 0; i < input.size(); i++) {
            if (!input.getResource(i).isEmpty()) {
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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.getInput().deserialize(input.childOrEmpty("input"));
        itemHandler.getOutput().deserialize(input.childOrEmpty("output"));
        energyHandler.deserialize(input.childOrEmpty("energy"));
        fluidHandler.deserialize(input.childOrEmpty("fluid"));

        progress = input.getIntOr("progress", 0);
        progressRequired = input.getIntOr("progressRequired", 0);

        pendingRecipeId = input.read("recipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("input", itemHandler.getInput());
        output.putChild("output", itemHandler.getOutput());
        output.putChild("energy", energyHandler);
        output.putChild("fluid", fluidHandler);

        // Write the progress
        output.putInt("progress", progress);
        output.putInt("progressRequired", progressRequired);

        // Write the recipe id
        if (currentRecipe != null) {
            output.store("recipe", ResourceKey.codec(Registries.RECIPE), currentRecipe.id());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(output);
        return output.buildResult();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);

        setFluid(components.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy());
        setEnergy(components.getOrDefault(ComponentsRegistry.STORED_ENERGY, 0));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        components.set(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(FluidUtil.getStack(fluidHandler, 0)));
        components.set(ComponentsRegistry.STORED_ENERGY, getEnergy());
    }

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
        return FluidUtil.getStack(fluidHandler, 0);
    }

    @Override
    public int getMaxFluid() {
        return fluidHandler.getCapacityAsInt(0, FluidResource.EMPTY);
    }

    @Override
    public void setEnergy(int energy) {
        energyHandler.overrideEnergy(energy);
    }

    @Override
    public void setFluid(FluidStack fluid) {
        fluidHandler.set(0, FluidResource.of(fluid), fluid.amount());
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
        setFluid(fluidStack);
    }
}
