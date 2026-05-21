package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.BasaltgenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerType;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.JarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceType;
import dev.ftb.mods.ftbstuffnthings.items.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBStuffNThings.MOD_ID);

    public static final DeferredItem<MeshItem> CLOTH_MESH = ITEMS.registerItem("cloth_mesh", (p) -> new MeshItem(p, MeshType.CLOTH));
    public static final DeferredItem<MeshItem> IRON_MESH = ITEMS.registerItem("iron_mesh", (p) -> new MeshItem(p, MeshType.IRON));
    public static final DeferredItem<MeshItem> GOLD_MESH = ITEMS.registerItem("gold_mesh", (p) -> new MeshItem(p, MeshType.GOLD));
    public static final DeferredItem<MeshItem> DIAMOND_MESH = ITEMS.registerItem("diamond_mesh", (p) -> new MeshItem(p, MeshType.DIAMOND));
    public static final DeferredItem<MeshItem> BLAZING_MESH = ITEMS.registerItem("blazing_mesh", (p) -> new MeshItem(p, MeshType.BLAZING));
    public static final List<DeferredItem<MeshItem>> ALL_MESHES = List.of(CLOTH_MESH, IRON_MESH, GOLD_MESH, DIAMOND_MESH, BLAZING_MESH);

    public static final DeferredItem<FluidCapsuleItem> FLUID_CAPSULE = ITEMS.registerItem("fluid_capsule", FluidCapsuleItem::new);
    public static final DeferredItem<WaterBowlItem> WATER_BOWL = ITEMS.registerItem("water_bowl", WaterBowlItem::new);

    public static final DeferredItem<Item> CAST_IRON_INGOT = simpleItem("cast_iron_ingot");
    public static final DeferredItem<Item> CAST_IRON_NUGGET = simpleItem("cast_iron_nugget");
    public static final DeferredItem<Item> CAST_IRON_GEAR = simpleItem("cast_iron_gear");
    public static final DeferredItem<Item> TEMPERED_GLASS = simpleItem("tempered_glass");

    public static final DeferredItem<HammerItem> STONE_HAMMER = registerHammer("stone_hammer", ToolMaterial.STONE);
    public static final DeferredItem<HammerItem> IRON_HAMMER = registerHammer("iron_hammer", ToolMaterial.IRON);
    public static final DeferredItem<HammerItem> GOLD_HAMMER = registerHammer("gold_hammer", ToolMaterial.GOLD);
    public static final DeferredItem<HammerItem> DIAMOND_HAMMER = registerHammer("diamond_hammer", ToolMaterial.DIAMOND);
    public static final DeferredItem<HammerItem> NETHERITE_HAMMER = registerHammer("netherite_hammer", ToolMaterial.NETHERITE);
    public static final List<DeferredItem<HammerItem>> ALL_HAMMERS = List.of(STONE_HAMMER, IRON_HAMMER, GOLD_HAMMER, DIAMOND_HAMMER, NETHERITE_HAMMER);

    public static final DeferredItem<CrookItem> CROOK = ITEMS.registerItem("stone_crook", CrookItem::new);
    public static final DeferredItem<Item> STONE_ROD = simpleItem("stone_rod");

    //#region Block Items
    static {
        for (var type : SluiceType.values()) {
            blockItem(type.getSerializedName() + "_sluice", BlocksRegistry.getSluice(type), SluiceBlock.SluiceBlockItem::new);
        }
        for (var type : AutoHammerType.values()) {
            blockItem(type.getMaterialId() + "_auto_hammer", BlocksRegistry.getAutoHammer(type));
        }
        for (var type : CobblegenProperties.values()) {
            blockItem(type.getName() + "_cobblestone_generator", BlocksRegistry.getCobbleGenerator(type));
        }
        for (var type : BasaltgenProperties.values()) {
            blockItem(type.getName() + "_basalt_generator", BlocksRegistry.getBasaltGenerator(type));
        }
        BlocksRegistry.allWaterStrainers().forEach(block -> blockItem(block.getId().getPath(), block));
        BlocksRegistry.allBarrels().forEach(block -> blockItem(block.getId().getPath(), block));
    }

    public static final DeferredItem<BlockItem> PUMP = blockItem("pump", BlocksRegistry.PUMP);

    public static final DeferredItem<BlockItem> DRIPPER = blockItem("dripper", BlocksRegistry.DRIPPER);

    public static final DeferredItem<BlockItem> WOODEN_BASIN = blockItem("wooden_basin", BlocksRegistry.WOODEN_BASIN);

    public static final DeferredItem<BlockItem> FUSING_MACHINE = blockItem("fusing_machine", BlocksRegistry.FUSING_MACHINE);
    public static final DeferredItem<BlockItem> SUPER_COOLER = blockItem("super_cooler", BlocksRegistry.SUPER_COOLER);

    public static final DeferredItem<BlockItem> CAST_IRON_BLOCK = blockItem("cast_iron_block", BlocksRegistry.CAST_IRON_BLOCK);

    public static final DeferredItem<BlockItem> DUST = blockItem("dust", BlocksRegistry.DUST_BLOCK);
    public static final DeferredItem<BlockItem> CRUSHED_BASALT = blockItem("crushed_basalt", BlocksRegistry.CRUSHED_BASALT);
    public static final DeferredItem<BlockItem> CRUSHED_ENDSTONE = blockItem("crushed_endstone", BlocksRegistry.CRUSHED_ENDSTONE);
    public static final DeferredItem<BlockItem> CRUSHED_NETHERRACK = blockItem("crushed_netherrack", BlocksRegistry.CRUSHED_NETHERRACK);

    public static final DeferredItem<JarBlock.JarBlockItem> JAR
            = blockItem("jar", BlocksRegistry.JAR, JarBlock.JarBlockItem::new);
    public static final DeferredItem<TemperedJarBlock.TemperedJarBlockItem> TEMPERED_JAR
            = blockItem("tempered_jar", BlocksRegistry.TEMPERED_JAR, TemperedJarBlock.TemperedJarBlockItem::new);
    public static final DeferredItem<BlockItem> AUTO_PROCESSING_BLOCK
            = blockItem("auto_processing_block", BlocksRegistry.JAR_AUTOMATER);
    public static final DeferredItem<BlockItem> BLUE_MAGMA_BLOCK
            = blockItem("blue_magma_block", BlocksRegistry.BLUE_MAGMA_BLOCK);
    public static final DeferredItem<BlockItem> CREATIVE_HOT_TEMPERATURE_SOURCE
            = blockItem("creative_low_temperature_source", BlocksRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE);
    public static final DeferredItem<BlockItem> CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE
            = blockItem("creative_high_temperature_source", BlocksRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE);
    public static final DeferredItem<BlockItem> CREATIVE_CHILLED_TEMPERATURE_SOURCE
            = blockItem("creative_subzero_temperature_source", BlocksRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE);

    public static final DeferredItem<BlockItem> SMALL_CRATE = blockItem("small_crate", BlocksRegistry.SMALL_CRATE);
    public static final DeferredItem<BlockItem> CRATE = blockItem("crate", BlocksRegistry.CRATE);
    public static final DeferredItem<BlockItem> PULSATING_CRATE = blockItem("pulsating_crate", BlocksRegistry.PULSATING_CRATE);

    static {
        BlocksRegistry.allCompressedBlocks().forEach(ITEMS::registerSimpleBlockItem);
    }

    //#endregion

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static DeferredItem<Item> simpleItem(String id) {
        return ITEMS.registerSimpleItem(id, Item.Properties::new);
    }

    public static DeferredItem<BlockItem> blockItem(String id, Supplier<? extends Block> sup) {
        return ITEMS.registerSimpleBlockItem(id, sup);
    }

    public static <B extends Block, I extends BlockItem> DeferredItem<I> blockItem(String name, Supplier<B> block, BiFunction<B, Item.Properties, I> factory) {
        return ITEMS.registerItem(name, props -> factory.apply(block.get(), props), () -> new Item.Properties().useBlockDescriptionPrefix());
    }

    public static <B extends Block, I extends BlockItem> DeferredItem<I> blockItem(String name, Supplier<B> block, Supplier<Item.Properties> properties, BiFunction<B, Item.Properties, I> factory) {
        return ITEMS.registerItem(name, props -> factory.apply(block.get(), props), () -> properties.get().useBlockDescriptionPrefix());
    }

    private static DeferredItem<HammerItem> registerHammer(String name, ToolMaterial material) {
        return ITEMS.registerItem(name, props -> new HammerItem(props, material));
    }
}
