package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
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
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class TemperedJarBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TANK_CAPACITY = 8000;  // 3 of these tanks
    private static final ResourceLocation NO_RECIPE = FTBStuffNThings.id("_none_");
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
    private final Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> itemOutputs = new EnumMap<>(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IFluidHandler, Direction>> fluidOutputs = new EnumMap<>(Direction.class);
    private JarStatus status = JarStatus.NO_RECIPE;
    private final List<ItemStack> itemBacklog = new ArrayList<>();
    private final List<FluidStack> fluidBacklog = new ArrayList<>();

    public TemperedJarBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntitiesRegistry.TEMPERED_JAR.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("Items", itemHandler.serializeNBT(registries));
        tag.put("Tanks", fluidHandler.serializeNBT(registries));
        tag.putInt("Remaining", remainingTime);
        if (!itemBacklog.isEmpty()) {
            tag.put("ItemBacklog", Util.make(new ListTag(), l -> itemBacklog.forEach(stack -> l.add(stack.save(registries)))));
        }
        if (!fluidBacklog.isEmpty()) {
            tag.put("FluidBacklog", Util.make(new ListTag(), l -> fluidBacklog.forEach(stack -> l.add(stack.save(registries)))));
        }
        if (currentRecipe != null) tag.putString("Recipe", currentRecipe.id().toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("Items"));
        fluidHandler.deserializeNBT(registries, tag.getCompound("Tanks"));
        remainingTime = tag.getInt("Remaining");
        pendingRecipeId = tag.getString("Recipe");  // see onLoad() for recipe init

        if (tag.contains("ItemBacklog", Tag.TAG_LIST)) {
            itemBacklog.clear();
            tag.getList("ItemBacklog", Tag.TAG_COMPOUND)
                    .forEach(t -> ItemStack.parse(registries, t).ifPresent(itemBacklog::add));
        }
        if (tag.contains("FluidBacklog", Tag.TAG_LIST)) {
            fluidBacklog.clear();
            tag.getList("FluidBacklog", Tag.TAG_COMPOUND)
                    .forEach(t -> FluidStack.parse(registries, t).ifPresent(fluidBacklog::add));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // server-side, chunk loading
        return Util.make(new CompoundTag(), tag -> saveAdditional(tag, registries));
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
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        List<SimpleFluidContent> list = componentInput.getOrDefault(ComponentsRegistry.FLUID_TANKS, List.of());
        for (int i = 0; i < list.size() && i < fluidHandler.tanks.size(); i++) {
            fluidHandler.tanks.get(i).setFluid(list.get(i).copy());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        List<SimpleFluidContent> list = fluidHandler.tanks.stream()
                .filter(tank -> !tank.isEmpty())
                .map(tank -> SimpleFluidContent.copyOf(tank.getFluid()))
                .toList();
        if (!list.isEmpty()) components.set(ComponentsRegistry.FLUID_TANKS, list);
    }

    public JarContainerData getContainerData() {
        return containerData;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!pendingRecipeId.isEmpty()) {
            getLevel().getRecipeManager().byKey(ResourceLocation.parse(pendingRecipeId)).ifPresent(r -> {
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
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()), SyncJarContentsPacket.wholeJar(this));
            syncNeeded = false;
            lastItemFluidSync = serverLevel.getGameTime();
        }

        if (needRecipeSearch) {
            ResourceLocation prevId = currentRecipe == null ? NO_RECIPE : currentRecipe.id();
            currentRecipe = findSuitableRecipe();
            setChanged();
            ResourceLocation newId = currentRecipe == null ? NO_RECIPE : currentRecipe.id();

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

    public void maybeClearBacklog(Direction dir) {
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
    private RecipeHolder<JarRecipe> findSuitableRecipe() {
        var recipes = RecipeCaches.TEMPERED_JAR.getCachedRecipes(this::searchForRecipe, this::genIngredientHash);
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
            for (int i = 0; i < recipe.getInputItems().size(); i++) {
                getInputItemHandler().extractItem(itemSlots[i], recipe.getInputItems().get(i).count(), false);
            }
            for (int i = 0; i < recipe.getInputFluids().size(); i++) {
                fluidHandler.tanks.get(fluidSlots[i]).drain(recipe.getInputFluids().get(i).amount(), FluidAction.EXECUTE);
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
                level.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1f, 1.2f + level.random.nextFloat() * 0.4f);
                Vec3 vec = Vec3.atBottomCenterOf(getBlockPos().above());
                ((ServerLevel) level).sendParticles(ParticleTypes.WHITE_SMOKE, vec.x, vec.y + 0.2, vec.z, 5, 0, 0, 0, 0.01);
            }
            setRemainingTime(!outputsFull && hasAutoProcessing() && recipe.canRepeat() ? processingTime : STOPPED);
        }
    }

    private List<ItemStack> distributeOutputItems(JarRecipe recipe) {
        return distributeOutputItems(recipe.getOutputItems().stream().map(ItemStack::copy).toList());
    }

    private List<ItemStack> distributeOutputItems(List<ItemStack> toDistribute) {
        List<ItemStack> excessList = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            if (suitableOutputBlock(dir)) {
                IItemHandler handler = itemOutputs.computeIfAbsent(dir, k ->
                                BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) getLevel(),
                                        getBlockPos().relative(dir), dir.getOpposite()))
                        .getCapability();
                if (handler != null) {
                    for (ItemStack stack : toDistribute) {
                        ItemStack excess = ItemHandlerHelper.insertItem(handler, stack, false);
                        if (!excess.isEmpty()) {
                            excessList.add(excess);
                        }
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
        BlockState state = level.getBlockState(getBlockPos().relative(dir));
        // don't push output to a hopper which is facing us, it's obviously intended as an input hopper
        return state.getBlock() != Blocks.HOPPER || state.getValue(BlockStateProperties.FACING_HOPPER) != dir.getOpposite();
    }

    private List<FluidStack> distributeOutputFluids(JarRecipe recipe) {
        return distributeOutputFluids(recipe.getOutputFluids().stream().map(FluidStack::copy).toList());
    }

    private List<FluidStack> distributeOutputFluids(List<FluidStack> toDistribute) {
        List<FluidStack> excessList = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            IFluidHandler dest = fluidOutputs.computeIfAbsent(dir, k ->
                            BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, (ServerLevel) getLevel(), getBlockPos().relative(dir), dir.getOpposite()))
                    .getCapability();
            if (dest != null) {
                for (FluidStack stack : toDistribute) {
                    int filled = dest.fill(stack, FluidAction.EXECUTE);
                    if (filled < stack.getAmount()) {
                        excessList.add(stack.copyWithAmount(stack.getAmount() - filled));
                    }
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

    private List<RecipeHolder<JarRecipe>> searchForRecipe() {
        // Note: we sort recipes with most input ingredients first, because items in the jar which don't match a
        //   recipe don't necessarily make the recipe invalid. Thus, recipes with more ingredients should be checked
        //   first to resolve potential ambiguity.
        return getLevel().getRecipeManager().getAllRecipesFor(RecipesRegistry.TEMPERED_JAR_TYPE.get()).stream()
                .filter(r -> r.value().test(getTemperature().temperature(), itemHandler, fluidHandler, false))
                .sorted(Comparator.comparing(RecipeHolder::value))
                .toList();
    }

    private int genIngredientHash() {
        IntList itemIds = new IntArrayList();
        itemIds.add(getTemperature().temperature().ordinal());
        for (int i = 0; i < getInputItemHandler().getSlots(); i++) {
            itemIds.add(ItemStack.hashItemAndComponents(getInputItemHandler().getStackInSlot(i)));
        }
        for (int i = 0; i < getFluidHandler().getTanks(); i++) {
            itemIds.add(FluidStack.hashFluidAndComponents(getFluidHandler().getFluidInTank(i)));
        }
        return Objects.hash(itemIds.toArray());
    }

    public TemperatureAndEfficiency getTemperature() {
        return temperature.get();
    }

    public boolean onRightClick(Player player, InteractionHand hand) {
        boolean res = false;
        if (FluidUtil.interactWithFluidHandler(player, hand, fluidHandler)) {
            syncNeeded = true;
            res = true;
        }

        if (!player.level().isClientSide()) {
            List<Component> msgs = new ArrayList<>();
            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack stack = fluidHandler.getFluidInTank(i);
                if (!stack.isEmpty()) {
                    msgs.add(Component.translatable("ftblibrary.mb", stack.getAmount(), stack.getHoverName()));
                }
            }
            if (msgs.isEmpty()) {
                player.displayClientMessage(Component.translatable("ftblibrary.empty"), true);
            } else {
                player.displayClientMessage(msgs.stream().reduce((c1, c2) -> c1.copy().append(" / ").append(c2)).orElse(Component.empty()), true);
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
        return !level.isOutsideBuildHeight(worldPosition.above())
                && level.getBlockState(worldPosition.above()).is(BlocksRegistry.JAR_AUTOMATER.get());
    }

    private TemperatureAndEfficiency checkForTemperature() {
        return TemperatureAndEfficiency.fromLevel(getLevel(), getBlockPos().below());
    }

    public IItemHandler getInputItemHandler() {
        return itemHandler;
    }

    public IItemHandler getInputItemHandler(Direction ignoredSide) {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public IFluidHandler getFluidHandler(Direction ignoredSide) {
        return fluidHandler;
    }

    public void syncFromServer(List<SyncJarContentsPacket.ResourceSlot> resources) {
        itemHandler.clear();
        fluidHandler.clear();
        resources.forEach(resource -> resource.resource()
                .ifLeft(item -> itemHandler.setStackInSlot(resource.slot(), item))
                .ifRight(fluid -> fluidHandler.tanks.get(resource.slot()).setFluid(fluid))
        );
    }

    public Optional<ResourceLocation> getCurrentRecipeId() {
        return currentRecipe == null ? Optional.empty() : Optional.of(currentRecipe.id());
    }

    public void setCurrentRecipeId(@Nullable ResourceLocation newRecipeId) {
        // only called clientside when a SyncJarRecipePacket is received
        if (level.isClientSide) {
            //noinspection unchecked
            currentRecipe = newRecipeId == null ?
                    null :
                    (RecipeHolder<JarRecipe>) level.getRecipeManager().byKey(newRecipeId).filter(r -> r.value() instanceof JarRecipe).orElse(null);
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

    public void dropContentsOnBreak() {
        Containers.dropContents(getLevel(), getBlockPos(), MiscUtil.getItemsInHandler(getInputItemHandler()));
    }

    private class JarItemHandler extends ItemStackHandler {
        private final ItemStack[] prevStack = new ItemStack[3];

        public JarItemHandler() {
            super(3);

            Arrays.fill(prevStack, ItemStack.EMPTY);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!level.isClientSide) {
                setChanged();
                syncNeeded = true;
                if (!ItemStack.isSameItemSameComponents(prevStack[slot], getStackInSlot(slot))) {
                    needRecipeSearch = true;
                }
                inputResourceLocator.invalidate();
                prevStack[slot] = getStackInSlot(slot).copy();
            }
        }

        @Override
        protected void onLoad() {
            for (int i = 0; i < getSlots(); i++) {
                prevStack[i] = getStackInSlot(i).copy();
            }
        }

        public void clear() {
            setSize(getSlots()); // clears slots
        }
    }

    private class JarFluidHandler implements IFluidHandler {
        private final List<JarTank> tanks;

        private JarFluidHandler() {
            tanks = List.of(new JarTank(), new JarTank(), new JarTank());
        }

        @Override
        public int getTanks() {
            return tanks.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks.get(tank).getFluid().copy();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks.get(tank).getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return getFluidInTank(tank).isEmpty() || FluidStack.isSameFluidSameComponents(getFluidInTank(tank), stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int firstEmpty = -1;
            int filled = 0;
            for (int i = 0; i < getTanks(); i++) {
                FluidStack current = getFluidInTank(i);
                if (FluidStack.isSameFluidSameComponents(current, resource)) {
                    filled = tanks.get(i).fill(resource, action);
                    break;
                } else if (firstEmpty < 0 && current.isEmpty()) {
                    firstEmpty = i;
                }
            }
            if (firstEmpty >= 0) {
                filled = tanks.get(firstEmpty).fill(resource, action);
            }
            if (filled > 0 && action.execute()) {
                setChanged();
                syncNeeded = true;
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return doDrain(t -> FluidStack.isSameFluidSameComponents(t.getFluid(), resource), resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return doDrain(t -> !t.isEmpty(), maxDrain, action);
        }

        private FluidStack doDrain(Predicate<FluidTank> tankPredicate, int amount, FluidAction action) {
            return tanks.stream()
                    .filter(tankPredicate)
                    .findFirst()
                    .map(tank -> tank.drain(amount, action))
                    .orElse(FluidStack.EMPTY);
        }

        public Tag serializeNBT(HolderLookup.Provider provider) {
            RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
            return Util.make(new CompoundTag(), tag -> {
                for (int i = 0; i < getTanks(); i++) {
                    if (!getFluidInTank(i).isEmpty()) {
                        tag.put("Tank" + i, FluidStack.CODEC.encodeStart(ops, getFluidInTank(i)).result().orElseThrow());
                    }
                }
            });
        }

        private void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
            for (int i = 0; i < tanks.size(); i++) {
                if (tag.contains("Tank" + i)) {
                    FluidStack stack = FluidStack.CODEC.parse(ops, tag.get("Tank" + i)).getOrThrow();
                    tanks.get(i).setFluid(stack);
                } else {
                    tanks.get(i).setFluid(FluidStack.EMPTY);
                }
            }
        }

        public void clear() {
            tanks.forEach(tank -> tank.setFluid(FluidStack.EMPTY));
        }
    }

    private class JarTank extends FluidTank {
        FluidStack prevFluid = FluidStack.EMPTY;

        public JarTank() {
            super(TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged() {
            if (!level.isClientSide) {
                setChanged();
                syncNeeded = true;
                if (!FluidStack.isSameFluidSameComponents(prevFluid, getFluid())) {
                    needRecipeSearch = true;
                }
                inputResourceLocator.invalidate();
                prevFluid = getFluid().copy();
            }
        }

        @Override
        public void setFluid(FluidStack stack) {
            prevFluid = getFluid().copy();
            super.setFluid(stack);
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

        public InputResourceLocator() {
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
                    for (int slotIdx = 0; slotIdx < itemHandler.getSlots(); slotIdx++) {
                        if (!itemSlotsChecked.get(slotIdx) && ingr.test(itemHandler.getStackInSlot(slotIdx))) {
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
                    for (int slotIdx = 0; slotIdx < fluidHandler.getTanks(); slotIdx++) {
                        if (!fluidSlotsChecked.get(slotIdx) && ingr.test(fluidHandler.getFluidInTank(slotIdx))) {
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
