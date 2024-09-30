package dev.ftb.mods.ftbobb;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbobb.client.ClientSetup;
import dev.ftb.mods.ftbobb.items.FluidCapsuleItem;
import dev.ftb.mods.ftbobb.recipes.RecipeCaches;
import dev.ftb.mods.ftbobb.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(FTBOBB.MODID)
public class FTBOBB {
    public static final String MODID = "ftbobb";

    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBOBB(IEventBus modEventBus, ModContainer modContainer) {
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

        modEventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BlockEntitiesRegistry.OAK_SLUICE.get(),
                (blockEntity, side) -> blockEntity.getFluidTank()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BlockEntitiesRegistry.JAR.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new FluidCapsuleItem.FluidHandler(stack),
                ItemsRegistry.FLUID_CAPSULE
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
