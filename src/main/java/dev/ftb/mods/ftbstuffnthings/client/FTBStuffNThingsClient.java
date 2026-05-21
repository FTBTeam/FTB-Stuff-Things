package dev.ftb.mods.ftbstuffnthings.client;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.client.render.*;
import dev.ftb.mods.ftbstuffnthings.client.screens.FusingMachineScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.SuperCoolerScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.TemperedJarScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.WaterStrainerScreen;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Mod(value = FTBStuffNThings.MOD_ID, dist = Dist.CLIENT)
public class FTBStuffNThingsClient {
    @Nullable
    private static FTBStuffNThingsClient instance;
    private RecipeMap recipeMap = RecipeMap.EMPTY;

    public FTBStuffNThingsClient(IEventBus modBus) {
        instance = this;

        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::registerItemTintSources);
        modBus.addListener(this::registerBlockTintSources);

        NeoForge.EVENT_BUS.addListener(this::receiveRecipes);
        NeoForge.EVENT_BUS.addListener(this::playerDisconnect);
    }

    public static FTBStuffNThingsClient getInstance() {
        return Objects.requireNonNull(instance);
    }

    private void receiveRecipes(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }

    private void playerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        recipeMap = RecipeMap.EMPTY;
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.AUTO_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.COBBLE_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.JAR.get(), JarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.TEMPERED_JAR.get(), TemperedJarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.WOODEN_BASIN.get(), BasinBlockEntityRenderer::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ContentRegistry.TEMPERED_JAR_MENU.get(), TemperedJarScreen::new);
        event.register(ContentRegistry.FUSING_MACHINE_MENU.get(), FusingMachineScreen::new);
        event.register(ContentRegistry.SUPER_COOLER_MENU.get(), SuperCoolerScreen::new);
        event.register(ContentRegistry.WATER_STRAINER_MENU.get(), WaterStrainerScreen::new);
    }

    private void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(FTBStuffNThings.id("fluid_capsule"), FluidCapsuleTintSource.CODEC);
    }

    public void registerBlockTintSources(final RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.water()), BlocksRegistry.allCobbleGenerators().stream().map(DeferredHolder::get).toArray(Block[]::new));
    }

    public RecipeMap getRecipeMap() {
        return recipeMap;
    }

}
