package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.client.FTBStuffNThingsClient;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import dev.ftb.mods.ftbstuffnthings.items.FluidCapsuleItem;
import dev.ftb.mods.ftbstuffnthings.network.SyncJarContentsPacket;
import dev.ftb.mods.ftbstuffnthings.network.SyncJarRecipePacket;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import dev.ftb.mods.ftbstuffnthings.util.DirectionUtil;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class TemperedJarBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 8000;  // 3 of these tanks
    private static final Identifier NO_RECIPE = FTBStuffNThings.id("_none_");
    public static final int STOPPED = -1;

    private boolean needRecipeSearch = true;
    private final Lazy<InputResourceLocator> inputResourceLocator = Lazy.of(InputResourceLocator::new);
    private String pendingRecipeId = "";
    @Nullable private RecipeHolder<JarRecipe> currentRecipe;
    private final Lazy<Boolean> autoProcessing = Lazy.of(this::checkForAutoProcessor);
    private final Lazy<TemperatureAndEfficiency> temperature = Lazy.of(this::checkForTemperature);
    private int remainingTime;  // -1 => don't autocraft, 0 => produce output, >0 => do crafting
    private int processingTime; // 0 when there's no current recipe
    private final JarItemHandler itemHandler = new JarItemHandler();
    private final JarFluidHandler fluidHandler = new JarFluidHandler();
    private final JarContainerData containerData = new JarContainerData();
    private boolean syncNeeded;
    private long lastItemFluidSync = 0L;
    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> itemOutputs = new EnumMap<>(Direction.class);
    private final Map<Direction, BlockCapabilityCache<ResourceHandler<FluidResource>, Direction>> fluidOutputs = new EnumMap<>(Direction.class);
    private JarStatus status = JarStatus.NO_RECIPE;
    private final List<ItemStack> itemBacklog = new ArrayList<>();
    private final List<FluidStack> fluidBacklog = new ArrayList<>();

    public TemperedJarBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntitiesRegistry.TEMPERED_JAR.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild("Items", itemHandler);
        output.putChild("Tanks", fluidHandler);
        output.putInt("Remaining", remainingTime);
        if (!itemBacklog.isEmpty()) {
            output.store("ItemBacklog", ItemStack.CODEC.listOf(), itemBacklog);
        }
        if (!fluidBacklog.isEmpty()) {
            output.store("FluidBacklog", FluidStack.CODEC.listOf(), fluidBacklog);
        }
        if (currentRecipe != null) output.putString("Recipe", currentRecipe.id().toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input.childOrEmpty("Items"));
        fluidHandler.deserialize(input.childOrEmpty("Tanks"));
        remainingTime = input.getInt("Remaining").orElse(0);
        pendingRecipeId = input.getString("Recipe").orElse("");  // see onLoad() for recipe init
        itemBacklog.clear();
        itemBacklog.addAll(input.read("ItemBacklog", ItemStack.CODEC.listOf()).orElse(List.of()));
        fluidBacklog.clear();
        fluidBacklog.addAll(input.read("FluidBacklog", FluidStack.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // server-side, chunk loading
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        saveAdditional(output);
        return output.buildResult();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ftbstuff.tempered_jar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TemperedJarMenu(containerId, playerInventory, getBlockPos());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);

        List<SimpleFluidContent> list = componentInput.getOrDefault(ComponentsRegistry.FLUID_TANKS, List.of());
        for (int i = 0; i < list.size() && i < fluidHandler.size(); i++) {
            FluidStack fs = list.get(i).copy();
            fluidHandler.set(i, FluidResource.of(fs), fs.amount());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        List<SimpleFluidContent> list = new ArrayList<>();
        for (int i = 0; i < fluidHandler.size(); i++) {
            list.add(SimpleFluidContent.copyOf(net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(fluidHandler, i)));
        }

        if (!list.isEmpty()) {
            components.set(ComponentsRegistry.FLUID_TANKS, list);
        }
    }

    public JarContainerData getContainerData() {
        return containerData;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!pendingRecipeId.isEmpty() && getLevel() instanceof ServerLevel serverLevel) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.parse(pendingRecipeId));
            serverLevel.getServer().getRecipeManager().byKey(key).ifPresent(r -> {
                if (r.value() instanceof JarRecipe) {
                    //noinspection unchecked
                    currentRecipe = (RecipeHolder<JarRecipe>) r;
                }
            });
            pendingRecipeId = "";
        }
    }

    public void serverTick(ServerLevel serverLevel) {
        if (syncNeeded && serverLevel.getGameTime() - lastItemFluidSync > 10L) {
            // don't sync items & fluids more than once every 10 ticks for performance reasons
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(getBlockPos()), SyncJarContentsPacket.wholeJar(this));
            syncNeeded = false;
            lastItemFluidSync = serverLevel.getGameTime();
        }

        if (needRecipeSearch) {
            Identifier prevId = currentRecipe == null ? NO_RECIPE : currentRecipe.id().identifier();
            currentRecipe = findSuitableRecipe(serverLevel);
            setChanged();
            Identifier newId = currentRecipe == null ? NO_RECIPE : currentRecipe.id().identifier();

            processingTime = currentRecipe == null ? 0 : getTemperature().getRecipeTime(currentRecipe.value());

            if (!prevId.equals(newId)) {
                // current recipe has changed! reset any crafting progress
                setRemainingTime(hasAutoProcessing() && processingTime > 0 ? processingTime : STOPPED);

                // update any players who have the GUI open right now
                SyncJarRecipePacket packet = new SyncJarRecipePacket(getBlockPos(), getCurrentRecipeId());
                serverLevel.players().stream()
                        .filter(p -> p.containerMenu instanceof TemperedJarMenu menu && menu.getJar() == this)
                        .forEach(p -> PacketDistributor.sendToPlayer(p, packet));

                inputResourceLocator.invalidate();
            } else {
                // same recipe, but processing time might have changed if block beneath was replaced
                setRemainingTime(Math.min(remainingTime, processingTime));
            }

            needRecipeSearch = false;
        }

        if (!itemBacklog.isEmpty() || !fluidBacklog.isEmpty()) {
            status = JarStatus.OUTPUT_FULL;
            if (serverLevel.getGameTime() % 20 == 0 && tryProcessBacklog()) {
                setRemainingTime(hasAutoProcessing() && processingTime > 0 ? processingTime : STOPPED);
                status = JarStatus.READY;
            }
        } else if (currentRecipe != null) {
            if (inputResourceLocator.get().allInputsFound()) {
                if (remainingTime == STOPPED) {
                    status = JarStatus.READY;
                } else {
                    // run a cycle
                    status = JarStatus.CRAFTING;
                    runOneCycle(serverLevel, currentRecipe.value());
                }
            } else {
                status = JarStatus.NOT_ENOUGH_RESOURCES;
            }
        } else {
            status = JarStatus.NO_RECIPE;
        }

        boolean active = getBlockState().getValue(TemperedJarBlock.ACTIVE);
        if (active && status != JarStatus.CRAFTING || !active && status == JarStatus.CRAFTING) {
            serverLevel.setBlock(getBlockPos(), getBlockState().setValue(TemperedJarBlock.ACTIVE, status == JarStatus.CRAFTING), Block.UPDATE_CLIENTS);
        }
    }

    private boolean tryProcessBacklog() {
        if (!itemBacklog.isEmpty()) {
            List<ItemStack> excess = distributeOutputItems(itemBacklog);
            itemBacklog.clear();
            itemBacklog.addAll(excess);
            setChanged();
        }
        if (!fluidBacklog.isEmpty()) {
            List<FluidStack> excess = distributeOutputFluids(fluidBacklog);
            fluidBacklog.clear();
            fluidBacklog.addAll(excess);
            setChanged();
        }

        return itemBacklog.isEmpty() && fluidBacklog.isEmpty();
    }

    public void maybeClearBacklog(Level level, Direction dir) {
        boolean cleared = false;
        if (!itemBacklog.isEmpty()) {
            itemBacklog.forEach(stack -> Block.popResource(level, getBlockPos().relative(dir), stack));
            itemBacklog.clear();
            cleared = true;
        }
        if (!fluidBacklog.isEmpty()) {
            fluidBacklog.forEach(stack -> Block.popResource(level, getBlockPos().relative(dir), FluidCapsuleItem.of(stack)));
            fluidBacklog.clear();
            cleared = true;
        }
        if (cleared) {
            setRemainingTime(hasAutoProcessing() && processingTime > 0 ? processingTime : STOPPED);
        }
    }

    @Nullable
    private RecipeHolder<JarRecipe> findSuitableRecipe(ServerLevel serverLevel) {
        var recipes = RecipeCaches.TEMPERED_JAR.getCachedRecipes(serverLevel, this::searchForRecipe, this::genIngredientHash);
        if (recipes.isEmpty()) {
            return null;
        } else if (recipes.size() == 1) {
            return recipes.getFirst();
        } else {
            return recipes.stream()
                    .filter(r -> r.value().test(getTemperature().temperature(), itemHandler, fluidHandler, true))
                    .findFirst()
                    .orElse(null);
        }
    }

    private void runOneCycle(ServerLevel serverLevel, JarRecipe recipe) {
        if (!itemBacklog.isEmpty() || !fluidBacklog.isEmpty()) {
            return;
        }

        if (remainingTime > 0) {
            setRemainingTime(remainingTime - 1);
        }
        if (remainingTime <= 0) {
            // important to copy these since inputResourceLocator will become null if items extracted
            int[] itemSlots = inputResourceLocator.get().itemSlots;
            int[] fluidSlots = inputResourceLocator.get().fluidSlots;
            try (Transaction tx = Transaction.openRoot()) {
                for (int i = 0; i < recipe.getInputItems().size(); i++) {
                    MiscUtil.removeResourceFromSlot(getInputItemHandler(), itemSlots[i], recipe.getInputItems().get(i).count(), tx);
                }
                for (int i = 0; i < recipe.getInputFluids().size(); i++) {
                    MiscUtil.removeResourceFromSlot(getFluidHandler(), fluidSlots[i], recipe.getInputFluids().get(i).amount(), tx);
                }
                tx.commit();
            }
            syncNeeded = true;

            boolean outputsFull = false;
            // produce output
            List<ItemStack> excessItems = distributeOutputItems(recipe);
            if (!excessItems.isEmpty()) {
                if (hasAutoProcessing()) {
                    itemBacklog.addAll(excessItems);
                    setChanged();
                } else {
                    excessItems.forEach(itemStack -> Block.popResource(serverLevel, getBlockPos(), itemStack));
                }
                outputsFull = true;
            }
            List<FluidStack> excessFluids = distributeOutputFluids(recipe);
            if (!excessFluids.isEmpty()) {
                if (hasAutoProcessing()) {
                    fluidBacklog.addAll(excessFluids);
                    setChanged();
                } else {
                    excessFluids.forEach(fluidStack -> Block.popResource(serverLevel, getBlockPos(), FluidCapsuleItem.of(fluidStack)));
                }
                outputsFull = true;
            }

            if (!hasAutoProcessing()) {
                serverLevel.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS,
                        1f, 1.2f + serverLevel.getRandom().nextFloat() * 0.4f);
                Vec3 vec = Vec3.atBottomCenterOf(getBlockPos().above());
                serverLevel.sendParticles(ParticleTypes.WHITE_SMOKE, vec.x, vec.y + 0.2, vec.z, 5, 0, 0, 0, 0.01);
            }
            setRemainingTime(!outputsFull && hasAutoProcessing() && recipe.canRepeat() ? processingTime : STOPPED);
        }
    }

    private List<ItemStack> distributeOutputItems(JarRecipe recipe) {
        return distributeOutputItems(recipe.getOutputItems().stream().map(ItemStackTemplate::create).toList());
    }

    private List<ItemStack> distributeOutputItems(List<ItemStack> toDistribute) {
        List<ItemStack> excessList = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            if (suitableOutputBlock(dir)) {
                ResourceHandler<ItemResource> handler = itemOutputs.computeIfAbsent(dir, _ ->
                                BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) getLevel(),
                                        getBlockPos().relative(dir), dir.getOpposite()))
                        .getCapability();
                if (handler != null) {
                    try (Transaction tx = Transaction.openRoot()) {
                        for (ItemStack stack : toDistribute) {
                            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.count(), tx);
                            if (inserted < stack.count()) {
                                excessList.add(stack.copyWithCount(stack.count() - inserted));
                            }
                        }
                        tx.commit();
                    }
                    toDistribute = List.copyOf(excessList);
                    if (toDistribute.isEmpty()) {
                        break;
                    }
                    excessList.clear();
                }
            }
        }
        return List.copyOf(toDistribute);
    }

    private boolean suitableOutputBlock(Direction dir) {
        assert level != null;
        BlockState state = level.getBlockState(getBlockPos().relative(dir));
        // don't push output to a hopper which is facing us, it's obviously intended as an input hopper
        return state.getBlock() != Blocks.HOPPER || state.getValue(BlockStateProperties.FACING_HOPPER) != dir.getOpposite();
    }

    private List<FluidStack> distributeOutputFluids(JarRecipe recipe) {
        return distributeOutputFluids(recipe.getOutputFluids().stream().map(FluidStackTemplate::create).toList());
    }

    private List<FluidStack> distributeOutputFluids(List<FluidStack> toDistribute) {
        List<FluidStack> excessList = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            var handler = fluidOutputs.computeIfAbsent(dir, k ->
                            BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite()))
                    .getCapability();
            if (handler != null) {
                try (Transaction tx = Transaction.openRoot()) {
                    for (FluidStack stack : toDistribute) {
                        int inserted = ResourceHandlerUtil.insertStacking(handler, FluidResource.of(stack), stack.amount(), tx);
                        if (inserted < stack.amount()) {
                            excessList.add(stack.copyWithAmount(stack.amount() - inserted));
                        }
                    }
                    tx.commit();
                }
                toDistribute = List.copyOf(excessList);
                if (toDistribute.isEmpty()) {
                    break;
                }
                excessList.clear();
            }
        }
        return List.copyOf(toDistribute);
    }

    private List<RecipeHolder<JarRecipe>> searchForRecipe(ServerLevel serverLevel) {
        // Note: we sort recipes with most input ingredients first, because items in the jar which don't match a
        //   recipe don't necessarily make the recipe invalid. Thus, recipes with more ingredients should be checked
        //   first to resolve potential ambiguity.
        return RecipesRegistry.TEMPERED_JAR_TYPE.get().streamRecipes(serverLevel)
                .filter(r -> r.value().test(getTemperature().temperature(), itemHandler, fluidHandler, false))
                .sorted(Comparator.comparing(RecipeHolder::value))
                .toList();
    }

    private int genIngredientHash() {
        IntList itemIds = new IntArrayList();
        itemIds.add(getTemperature().temperature().ordinal());
        for (int i = 0; i < getInputItemHandler().size(); i++) {
            itemIds.add(getInputItemHandler().getResource(i).hashCode());
        }
        for (int i = 0; i < getFluidHandler().size(); i++) {
            itemIds.add(getFluidHandler().getResource(i).hashCode());
        }
        return Objects.hash(itemIds.toArray());
    }

    public TemperatureAndEfficiency getTemperature() {
        return temperature.get();
    }

    public boolean onRightClick(Player player, InteractionHand hand) {
        boolean res = false;
        if (FluidUtil.interactWithFluidHandler(player, hand, getBlockPos(), fluidHandler)) {
            syncNeeded = true;
            res = true;
        }

        if (!player.level().isClientSide()) {
            List<Component> msgs = new ArrayList<>();
            for (int i = 0; i < fluidHandler.size(); i++) {
                FluidResource resource = fluidHandler.getResource(i);
                if (!resource.isEmpty()) {
                    msgs.add(Component.translatable("ftblibrary.mb", fluidHandler.getAmountAsInt(i), resource.getHoverName()));
                }
            }
            if (msgs.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("ftblibrary.empty"));
            } else {
                player.sendOverlayMessage(msgs.stream().reduce((c1, c2) -> c1.copy().append(" / ").append(c2)).orElse(Component.empty()));
            }
        }

        return res;
    }

    public void clearCachedData() {
        temperature.invalidate();
        autoProcessing.invalidate();
        needRecipeSearch = true;
    }

    private boolean hasAutoProcessing() {
        return autoProcessing.get();
    }

    private boolean checkForAutoProcessor() {
        assert level != null;
        return !level.isOutsideBuildHeight(worldPosition.above())
                && level.getBlockState(worldPosition.above()).is(BlocksRegistry.JAR_AUTOMATER.get());
    }

    private TemperatureAndEfficiency checkForTemperature() {
        assert getLevel() != null;
        return TemperatureAndEfficiency.fromLevel(getLevel(), getBlockPos().below());
    }

    public void itemIndexModifer(int index, ItemResource resource, int amount) {
        // for use by TemperedJarMenu
        itemHandler.set(index, resource, amount);
    }

    public ResourceHandler<ItemResource> getInputItemHandler() {
        return itemHandler;
    }

    public ResourceHandler<ItemResource> getInputItemHandler(Direction ignoredSide) {
        return itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidHandler;
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction ignoredSide) {
        return fluidHandler;
    }

    public void syncFromServer(List<SyncJarContentsPacket.ResourceSlot> resources) {
        itemHandler.clear();
        fluidHandler.clear();
        resources.forEach(resource -> resource.resource()
                .ifLeft(item -> itemHandler.set(resource.slot(), item, resource.amount()))
                .ifRight(fluid -> fluidHandler.set(resource.slot(), fluid, resource.amount()))
        );
    }

    public Optional<Identifier> getCurrentRecipeId() {
        return currentRecipe == null ? Optional.empty() : Optional.of(currentRecipe.id().identifier());
    }

    public void setCurrentRecipeId(@Nullable Identifier newRecipeId) {
        // only called clientside when a SyncJarRecipePacket is received
        assert level != null && level.isClientSide();
        if (newRecipeId == null) {
            currentRecipe = null;
        } else {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, newRecipeId);
            RecipeHolder<?> holder = FTBStuffNThingsClient.getInstance().getRecipeMap().byKey(key);
            if (holder != null && holder.value() instanceof JarRecipe j) {
                currentRecipe = new RecipeHolder<>(holder.id(), j);
            }
        }
    }

    public void toggleCrafting() {
        setRemainingTime(remainingTime < 0 && currentRecipe != null ? processingTime : STOPPED);
    }

    private void setRemainingTime(int time) {
        if (time != remainingTime) {
            remainingTime = time;
            setChanged();
        }
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public JarStatus getStatus() {
        return status;
    }

    public Optional<RecipeHolder<JarRecipe>> getCurrentRecipe() {
        return Optional.ofNullable(currentRecipe);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        Containers.dropContents(getLevel(), getBlockPos(), MiscUtil.getItemsInHandler(getInputItemHandler()));
    }

    private class JarItemHandler extends ItemStacksResourceHandler {
        public JarItemHandler() {
            super(3);
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            if (level != null && !level.isClientSide()) {
                setChanged();
                syncNeeded = true;
                if (!ItemStack.isSameItemSameComponents(stacks.get(slot), previousContents)) {
                    needRecipeSearch = true;
                }
                inputResourceLocator.invalidate();
            }
        }

        public void clear() {
            for (int i = 0; i < size(); i++) {
                set(i, ItemResource.EMPTY, 0);
            }
        }
    }

    private static class JarFluidHandler extends FluidStacksResourceHandler {
        private JarFluidHandler() {
            super(3, TANK_CAPACITY);
        }

        public void clear() {
            for (int i = 0; i < size(); i++) {
                set(i, FluidResource.EMPTY, 0);
            }
        }
    }

    public class JarContainerData implements ContainerData {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> processingTime;
                case 1 -> remainingTime;
                case 2 -> status.ordinal();
                default -> throw new IllegalArgumentException("invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> processingTime = value;
                case 1 -> setRemainingTime(value);
                case 2 -> status = JarStatus.values()[value];
                default -> throw new IllegalArgumentException("invalid index: " + index);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    /**
     * Tracks the slots from which the items/fluids for the current recipe can be extracted
     */
    private class InputResourceLocator {
        private int[] itemSlots = new int[] { -1 };
        private int[] fluidSlots = new int[] { -1 };

        InputResourceLocator() {
            locateInputResources();
        }

        private void locateInputResources() {
            if (currentRecipe != null) {
                JarRecipe recipe = currentRecipe.value();

                itemSlots = Util.make(new int[recipe.getInputItems().size()], a -> Arrays.fill(a, -1));
                BitSet itemSlotsChecked = new BitSet(itemSlots.length);
                List<SizedIngredient> inputItems = recipe.getInputItems();
                for (int ingrIdx = 0; ingrIdx < inputItems.size(); ingrIdx++) {
                    SizedIngredient ingr = inputItems.get(ingrIdx);
                    for (int slotIdx = 0; slotIdx < itemHandler.size(); slotIdx++) {
                        if (!itemSlotsChecked.get(slotIdx) && ingr.test(itemHandler.getResource(slotIdx).toStack(itemHandler.getAmountAsInt(slotIdx)))) {
                            itemSlotsChecked.set(slotIdx);
                            itemSlots[ingrIdx] = slotIdx;
                        }
                    }
                }

                fluidSlots = Util.make(new int[recipe.getInputFluids().size()], a -> Arrays.fill(a, -1));
                BitSet fluidSlotsChecked = new BitSet(fluidSlots.length);
                List<SizedFluidIngredient> inputFluids = recipe.getInputFluids();
                for (int ingrIdx = 0; ingrIdx < inputFluids.size(); ingrIdx++) {
                    SizedFluidIngredient ingr = inputFluids.get(ingrIdx);
                    for (int slotIdx = 0; slotIdx < fluidHandler.size(); slotIdx++) {
                        if (!fluidSlotsChecked.get(slotIdx) && ingr.test(fluidHandler.getResource(slotIdx).toStack(fluidHandler.getAmountAsInt(slotIdx)))) {
                            fluidSlotsChecked.set(slotIdx);
                            fluidSlots[ingrIdx] = slotIdx;
                        }
                    }
                }
            } else {
                itemSlots = new int[] { -1 };
                fluidSlots = new int[] { -1 };
            }
        }

        private boolean allInputsFound() {
            return Arrays.stream(itemSlots).noneMatch(itemSlot -> itemSlot < 0)
                    && Arrays.stream(fluidSlots).noneMatch(fluidSlot -> fluidSlot < 0);
        }
    }


    public enum JarStatus {
        READY("ready", ChatFormatting.DARK_GREEN),
        CRAFTING("crafting", ChatFormatting.GREEN),
        NO_RECIPE("no_recipe", ChatFormatting.GOLD),
        NOT_ENOUGH_RESOURCES("not_enough_resources", ChatFormatting.YELLOW),
        OUTPUT_FULL("output_full", ChatFormatting.YELLOW)
        ;

        private final String id;
        private final ChatFormatting color;

        JarStatus(String id, ChatFormatting color) {
            this.id = id;
            this.color = color;
        }

        public Component displayString() {
            return Component.translatable("ftbstuff.jar_status." + id).withStyle(color);
        }
    }
}
