package dev.ftb.mods.ftbstuffnthings.client;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.client.render.*;
import dev.ftb.mods.ftbstuffnthings.client.screens.FusingMachineScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.SuperCoolerScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.TemperedJarScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.WaterStrainerScreen;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBStuffNThings.MOD_ID, dist = Dist.CLIENT)
public class FTBStuffNThingsClient {
    private static FTBStuffNThingsClient instance;
    private RecipeMap recipeMap = RecipeMap.EMPTY;

    public FTBStuffNThingsClient(IEventBus modBus) {
        instance = this;

        modBus.addListener(this::registerModelLoaders);
        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::registerColorHandlers);
        modBus.addListener(this::registerBlockColourHandlers);

        NeoForge.EVENT_BUS.addListener(this::receiveRecipes);
        NeoForge.EVENT_BUS.addListener(this::playerDisconnect);
    }

    public static FTBStuffNThingsClient getInstance() {
        return instance;
    }

    private void receiveRecipes(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }

    private void playerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        recipeMap = RecipeMap.EMPTY;
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.OAK_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.SPRUCE_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.BIRCH_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.JUNGLE_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACACIA_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DARK_OAK_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.MANGROVE_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.CHERRY_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.PALE_OAK_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.CRIMSON_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.WARPED_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.BAMBOO_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_SLUICE.get(), SluiceBlockEntityRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GOLD_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_HAMMER.get(), AutoHammerRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.STONE_COBBLEGEN.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_COBBLEGEN.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GOLD_COBBLEGEN.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_COBBLEGEN.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_COBBLEGEN.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.STONE_BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GOLD_BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_BASALT_GENERATOR.get(), ResourcegenBlockEntityRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.JAR.get(), JarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.TEMPERED_JAR.get(), TemperedJarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.WOODEN_BASIN.get(), BasinBlockEntityRenderer::new);
    }

    private void registerModelLoaders(ModelEvent.RegisterLoaders event) {
        // TODO tubes aren't in right now but uncomment this if they're added back
//        event.register(TubeModel.Loader.ID, TubeModel.Loader.INSTANCE);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ContentRegistry.TEMPERED_JAR_MENU.get(), TemperedJarScreen::new);
        event.register(ContentRegistry.FUSING_MACHINE_MENU.get(), FusingMachineScreen::new);
        event.register(ContentRegistry.SUPER_COOLER_MENU.get(), SuperCoolerScreen::new);
        event.register(ContentRegistry.WATER_STRAINER_MENU.get(), WaterStrainerScreen::new);
    }

    private void registerColorHandlers(RegisterColorHandlersEvent.ItemTintSources event) {
//        event.register((stack, tintIndex) -> switch (tintIndex) {
//            case 0 -> 0xFFFFFFFF;
//            case 1 -> FluidCapsuleColorHandler.getColor(stack);
//            default -> 0xFF000000;
//        }, ItemsRegistry.FLUID_CAPSULE.get());
//
//        event.register(
//                (stack, index) -> {
//                    if (index != 1) {
//                        return -1;
//                    }
//
//                    Minecraft instance = Minecraft.getInstance();
//                    return instance.level != null && instance.player != null ? BiomeColors.getAverageWaterColor(instance.level, instance.player.blockPosition()) : 4159204;
//                },
//                BlocksRegistry.COBBLEGENS.stream().map(DeferredHolder::get).map(ItemStack::new).map(ItemStack::getItem).toArray(ItemLike[]::new)
//        );
    }

    public void registerBlockColourHandlers(final RegisterColorHandlersEvent.BlockTintSources event) {
//        event.register(
//                (state, env, pos, index) -> {
//                    if (index != 1) {
//                        return -1;
//                    }
//
//                    return env != null && pos != null ? BiomeColors.getAverageWaterColor(env, pos) : 4159204;
//                },
//                BlocksRegistry.COBBLEGENS.stream().map(DeferredHolder::get).toArray(Block[]::new)
//        );
    }

    public RecipeMap getRecipeMap() {
        return recipeMap;
    }
}
