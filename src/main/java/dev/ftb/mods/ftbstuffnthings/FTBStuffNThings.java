package dev.ftb.mods.ftbstuffnthings;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.items.FluidCapsuleItem;
import dev.ftb.mods.ftbstuffnthings.items.WaterBowlItem;
import dev.ftb.mods.ftbstuffnthings.network.SyncLootSummaryPacket;
import dev.ftb.mods.ftbstuffnthings.registry.*;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.LootSummaryCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Mod(FTBStuffNThings.MOD_ID)
public class FTBStuffNThings {
    public static final String MOD_ID = "ftbstuff";
    public static final String MOD_NAME = "FTB Stuff & Things";

    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBStuffNThings(IEventBus modEventBus) {
        ConfigManager.getInstance().registerServerConfig(ModConfig.CONFIG, MOD_ID, false);

        BlocksRegistry.init(modEventBus);
        ItemsRegistry.init(modEventBus);
        BlockEntitiesRegistry.init(modEventBus);
        RecipesRegistry.init(modEventBus);
        ContentRegistry.init(modEventBus);
        ComponentsRegistry.init(modEventBus);
        CriterionTriggerRegistry.init(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncLootSummaries(serverPlayer);
            CriterionTriggerRegistry.FTBSTUFF_ROOT.get().trigger(serverPlayer);
        }
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        List<RecipeType<?>> types = RecipesRegistry.RECIPE_TYPES.getEntries().stream()
                .map(DeferredHolder::get)
                .collect(Collectors.toList());
        event.sendRecipes(types);
    }

    public static void syncLootSummaries(ServerPlayer serverPlayer) {
        // sent to players when they log in, and when a /reload is done on the server
        LootSummaryCollection lsc = new LootSummaryCollection();

        ModConfig.getStrainerLootTable().ifPresent(lootTableId -> BlocksRegistry.waterStrainers().forEach(b ->
                lsc.addEntry(b.getKey(), lootTableId, makeBlockParams(serverPlayer, b.get().defaultBlockState())))
        );
        BlocksRegistry.BARRELS.forEach(b ->
                lsc.addEntry(b.getKey(), blockLootTable(b), makeBlockParams(serverPlayer, b.get().defaultBlockState()))
        );
        BlocksRegistry.CRATES.forEach(b ->
                lsc.addEntry(b.getKey(), blockLootTable(b), makeBlockParams(serverPlayer, b.get().defaultBlockState()))
        );

        PacketDistributor.sendToPlayer(serverPlayer, new SyncLootSummaryPacket(lsc));
    }

    private static LootParams makeBlockParams(ServerPlayer serverPlayer, BlockState state) {
        return new LootParams.Builder(serverPlayer.level())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, serverPlayer)
                .create(LootContextParamSets.BLOCK);
    }

    private static Identifier blockLootTable(DeferredBlock<Block> db) {
        return Identifier.fromNamespaceAndPath(db.getId().getNamespace(), "blocks/" + db.getId().getPath());
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                BlockEntitiesRegistry.JAR.get(),
                (blockEntity, _) -> blockEntity.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BlockEntitiesRegistry.TEMPERED_JAR.get(),
                TemperedJarBlockEntity::getInputItemHandler
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                BlockEntitiesRegistry.TEMPERED_JAR.get(),
                TemperedJarBlockEntity::getFluidHandler
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BlockEntitiesRegistry.WATER_STRAINER.get(),
                WaterStrainerBlockEntity::getItemHandler
        );

        List.of(BlockEntitiesRegistry.FUSING_MACHINE, BlockEntitiesRegistry.SUPER_COOLER).forEach(machine ->
                AbstractMachineBlockEntity.registerCapabilities(event, machine.get()));

        List.of(BlockEntitiesRegistry.OAK_SLUICE, BlockEntitiesRegistry.SPRUCE_SLUICE,
                BlockEntitiesRegistry.BIRCH_SLUICE, BlockEntitiesRegistry.JUNGLE_SLUICE,
                BlockEntitiesRegistry.ACACIA_SLUICE, BlockEntitiesRegistry.DARK_OAK_SLUICE,
                BlockEntitiesRegistry.MANGROVE_SLUICE, BlockEntitiesRegistry.CHERRY_SLUICE,
                BlockEntitiesRegistry.PALE_OAK_SLUICE, BlockEntitiesRegistry.CRIMSON_SLUICE,
                BlockEntitiesRegistry.WARPED_SLUICE, BlockEntitiesRegistry.BAMBOO_SLUICE,
                BlockEntitiesRegistry.IRON_SLUICE, BlockEntitiesRegistry.DIAMOND_SLUICE,
                BlockEntitiesRegistry.NETHERITE_SLUICE)
                .forEach(sluice -> SluiceBlockEntity.registerCapabilities(event, sluice.get()));
        List.of(BlockEntitiesRegistry.IRON_HAMMER, BlockEntitiesRegistry.GOLD_HAMMER,
                BlockEntitiesRegistry.DIAMOND_HAMMER, BlockEntitiesRegistry.NETHERITE_HAMMER)
                .forEach(hammer -> AutoHammerBlockEntity.registerCapabilities(event, hammer.get()));

        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, itemAccess) -> new FluidCapsuleItem.FluidHandler(itemAccess),
                ItemsRegistry.FLUID_CAPSULE
        );
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, itemAccess) -> new WaterBowlItem.WaterBowlFluidHandler(itemAccess),
                ItemsRegistry.WATER_BOWL
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                BlockEntitiesRegistry.WOODEN_BASIN.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );
    }

    private void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(FTBStuffNThings.id("reload"), new CacheReloadListener());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static class CacheReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(SharedState sharedState, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
            return CompletableFuture.runAsync(RecipeCaches::clearAll, reloadExecutor).thenCompose(preparationBarrier::wait);
        }
    }
}
