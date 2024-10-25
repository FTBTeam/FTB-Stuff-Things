package dev.ftb.mods.ftbstuffnthings.client;

import dev.ftb.mods.ftbstuffnthings.client.model.TubeModel;
import dev.ftb.mods.ftbstuffnthings.client.renders.AutoHammerRenderer;
import dev.ftb.mods.ftbstuffnthings.client.renders.JarBlockEntityRenderer;
import dev.ftb.mods.ftbstuffnthings.client.renders.SluiceBlockEntityRenderer;
import dev.ftb.mods.ftbstuffnthings.client.renders.TemperedJarBlockEntityRenderer;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ContentRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.client.screens.FusingMachineScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.SuperCoolerScreen;
import dev.ftb.mods.ftbstuffnthings.client.screens.TemperedJarScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ClientSetup {
    public static void onModConstruction(IEventBus modBus) {
        // called from mod ctor
        modBus.addListener(ClientSetup::registerModelLoaders);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerScreens);
        modBus.addListener(ClientSetup::registerColorHandlers);
        modBus.addListener(ClientSetup::registerBlockColourHandlers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.OAK_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_SLUICE.get(), SluiceBlockEntityRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GOLD_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_HAMMER.get(), AutoHammerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_HAMMER.get(), AutoHammerRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.STONE_COBBLEGEN.get(), CobblegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.IRON_COBBLEGEN.get(), CobblegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GOLD_COBBLEGEN.get(), CobblegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DIAMOND_COBBLEGEN.get(), CobblegenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.NETHERITE_COBBLEGEN.get(), CobblegenBlockEntityRenderer::new);

        event.registerBlockEntityRenderer(BlockEntitiesRegistry.JAR.get(), JarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.TEMPERED_JAR.get(), TemperedJarBlockEntityRenderer::new);
    }

    private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(TubeModel.Loader.ID, TubeModel.Loader.INSTANCE);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ContentRegistry.TEMPERED_JAR_MENU.get(), TemperedJarScreen::new);
        event.register(ContentRegistry.FUSING_MACHINE_MENU.get(), FusingMachineScreen::new);
        event.register(ContentRegistry.SUPER_COOLER_MENU.get(), SuperCoolerScreen::new);
    }

    private static void registerColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> 0xFFFFFFFF;
            case 1 -> FluidCapsuleColorHandler.getColor(stack);
            default -> 0xFF000000;
        }, ItemsRegistry.FLUID_CAPSULE.get());

        event.register(
                (stack, index) -> {
                    if (index != 1) {
                        return -1;
                    }

                    Minecraft instance = Minecraft.getInstance();
                    return instance.level != null && instance.player != null ? BiomeColors.getAverageWaterColor(instance.level, instance.player.blockPosition()) : 4159204;
                },
                BlocksRegistry.COBBLEGENS.stream().map(DeferredHolder::get).map(ItemStack::new).map(ItemStack::getItem).toArray(ItemLike[]::new)
        );
    }

    public static void registerBlockColourHandlers(final RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, env, pos, index) -> {
                    if (index != 1) {
                        return -1;
                    }

                    return env != null && pos != null ? BiomeColors.getAverageWaterColor(env, pos) : 4159204;
                },
                BlocksRegistry.COBBLEGENS.stream().map(DeferredHolder::get).toArray(Block[]::new)
        );
    }
}
