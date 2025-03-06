package dev.ftb.mods.ftbstuffnthings;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.ClientSetup;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.items.FluidCapsuleItem;
import dev.ftb.mods.ftbstuffnthings.items.WaterBowlItem;
import dev.ftb.mods.ftbstuffnthings.network.SyncLootSummaryPacket;
import dev.ftb.mods.ftbstuffnthings.registry.*;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.LootSummaryCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(FTBStuffNThings.MODID)
public class FTBStuffNThings {
    public static final String MODID = "ftbstuff";

    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBStuffNThings(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.onModConstruction(modEventBus);
        }

        Config.init();

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
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncLootSummaries(serverPlayer);
            CriterionTriggerRegistry.FTBSTUFF_ROOT.get().trigger(serverPlayer);
        }
    }

    public static void syncLootSummaries(ServerPlayer serverPlayer) {
        // sent to players when they log in, and when a /reload is done on the server
        LootSummaryCollection lsc = new LootSummaryCollection();

        Config.getStrainerLootTable().ifPresent(lootTableId -> BlocksRegistry.waterStrainers().forEach(b -> {
            lsc.addEntry(b.getKey(), lootTableId, makeBlockParams(serverPlayer, b.get().defaultBlockState()));
        }));
        BlocksRegistry.BARRELS.forEach(b -> {
            lsc.addEntry(b.getKey(), blockLootTable(b), makeBlockParams(serverPlayer, b.get().defaultBlockState()));
        });
        BlocksRegistry.CRATES.forEach(b -> {
            lsc.addEntry(b.getKey(), blockLootTable(b), makeBlockParams(serverPlayer, b.get().defaultBlockState()));
        });

        PacketDistributor.sendToPlayer(serverPlayer, new SyncLootSummaryPacket(lsc));
    }

    private static LootParams makeBlockParams(ServerPlayer serverPlayer, BlockState state) {
        return new LootParams.Builder(serverPlayer.serverLevel())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, serverPlayer)
                .create(LootContextParamSets.BLOCK);
    }

    private static ResourceLocation blockLootTable(DeferredBlock<Block> db) {
        return ResourceLocation.fromNamespaceAndPath(db.getId().getNamespace(), "blocks/" + db.getId().getPath());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BlockEntitiesRegistry.JAR.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntitiesRegistry.TEMPERED_JAR.get(),
                TemperedJarBlockEntity::getInputItemHandler
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BlockEntitiesRegistry.TEMPERED_JAR.get(),
                TemperedJarBlockEntity::getFluidHandler
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
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
                BlockEntitiesRegistry.NETHERITE_SLUICE).forEach(sluice -> {
            SluiceBlockEntity.registerCapabilities(event, sluice.get());
        });
        List.of(BlockEntitiesRegistry.IRON_HAMMER, BlockEntitiesRegistry.GOLD_HAMMER,
                BlockEntitiesRegistry.DIAMOND_HAMMER, BlockEntitiesRegistry.NETHERITE_HAMMER).forEach(hammer -> {
            AutoHammerBlockEntity.registerCapabilities(event, hammer.get());
        });

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new FluidCapsuleItem.FluidHandler(stack),
                ItemsRegistry.FLUID_CAPSULE
        );
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new WaterBowlItem.WaterBowlFluidHandler(stack),
                ItemsRegistry.WATER_BOWL
        );
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CacheReloadListener());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static class CacheReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(RecipeCaches::clearAll, gameExecutor).thenCompose(stage::wait);
        }
    }
}
