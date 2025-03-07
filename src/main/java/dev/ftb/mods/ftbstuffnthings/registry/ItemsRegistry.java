package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.items.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBStuffNThings.MODID);

    public static final DeferredItem<MeshItem> CLOTH_MESH = ITEMS.register("cloth_mesh", () -> new MeshItem(MeshType.CLOTH));
    public static final DeferredItem<MeshItem> IRON_MESH = ITEMS.register("iron_mesh", () -> new MeshItem(MeshType.IRON));
    public static final DeferredItem<MeshItem> GOLD_MESH = ITEMS.register("gold_mesh", () -> new MeshItem(MeshType.GOLD));
    public static final DeferredItem<MeshItem> DIAMOND_MESH = ITEMS.register("diamond_mesh", () -> new MeshItem(MeshType.DIAMOND));
    public static final DeferredItem<MeshItem> BLAZING_MESH = ITEMS.register("blazing_mesh", () -> new MeshItem(MeshType.BLAZING));
    public static final List<DeferredItem<MeshItem>> ALL_MESHES = List.of(CLOTH_MESH, IRON_MESH, GOLD_MESH, DIAMOND_MESH, BLAZING_MESH);

    public static final DeferredItem<FluidCapsuleItem> FLUID_CAPSULE = ITEMS.register("fluid_capsule", FluidCapsuleItem::new);
    public static final DeferredItem<WaterBowlItem> WATER_BOWL = ITEMS.register("water_bowl", WaterBowlItem::new);

    public static final DeferredItem<Item> CAST_IRON_INGOT = simpleItem("cast_iron_ingot");
    public static final DeferredItem<Item> CAST_IRON_NUGGET = simpleItem("cast_iron_nugget");
    public static final DeferredItem<Item> CAST_IRON_GEAR = simpleItem("cast_iron_gear");
    public static final DeferredItem<Item> TEMPERED_GLASS = simpleItem("tempered_glass");

    public static final DeferredItem<HammerItem> STONE_HAMMER = registerHammer("stone_hammer", Tiers.STONE);
    public static final DeferredItem<HammerItem> IRON_HAMMER = registerHammer("iron_hammer", Tiers.IRON);
    public static final DeferredItem<HammerItem> GOLD_HAMMER = registerHammer("gold_hammer", Tiers.GOLD);
    public static final DeferredItem<HammerItem> DIAMOND_HAMMER = registerHammer("diamond_hammer", Tiers.DIAMOND);
    public static final DeferredItem<HammerItem> NETHERITE_HAMMER = registerHammer("netherite_hammer", Tiers.NETHERITE);
    public static final List<DeferredItem<HammerItem>> ALL_HAMMERS = List.of(STONE_HAMMER, IRON_HAMMER, GOLD_HAMMER, DIAMOND_HAMMER, NETHERITE_HAMMER);

    public static final DeferredItem<CrookItem> CROOK = ITEMS.register("stone_crook",
            () -> new CrookItem(Tiers.STONE, new Item.Properties().attributes(
                    DiggerItem.createAttributes(Tiers.STONE, 2, -2.8F)
            ))
    );
    public static final DeferredItem<Item> STONE_ROD = simpleItem("stone_rod");

    //#region Block Items
    public static final DeferredItem<BlockItem> OAK_SLUICE = blockItem("oak_sluice", BlocksRegistry.OAK_SLUICE);
    public static final DeferredItem<BlockItem> SPRUCE_SLUICE = blockItem("spruce_sluice", BlocksRegistry.SPRUCE_SLUICE);
    public static final DeferredItem<BlockItem> BIRCH_SLUICE = blockItem("birch_sluice", BlocksRegistry.BIRCH_SLUICE);
    public static final DeferredItem<BlockItem> JUNGLE_SLUICE = blockItem("jungle_sluice", BlocksRegistry.JUNGLE_SLUICE);
    public static final DeferredItem<BlockItem> ACACIA_SLUICE = blockItem("acacia_sluice", BlocksRegistry.ACACIA_SLUICE);
    public static final DeferredItem<BlockItem> DARK_OAK_SLUICE = blockItem("dark_oak_sluice", BlocksRegistry.DARK_OAK_SLUICE);
    public static final DeferredItem<BlockItem> MANGROVE_SLUICE = blockItem("mangrove_sluice", BlocksRegistry.MANGROVE_SLUICE);
    public static final DeferredItem<BlockItem> CHERRY_SLUICE = blockItem("cherry_sluice", BlocksRegistry.CHERRY_SLUICE);
    public static final DeferredItem<BlockItem> PALE_OAK_SLUICE = blockItem("pale_oak_sluice", BlocksRegistry.PALE_OAK_SLUICE);
    public static final DeferredItem<BlockItem> CRIMSON_SLUICE = blockItem("crimson_sluice", BlocksRegistry.CRIMSON_SLUICE);
    public static final DeferredItem<BlockItem> WARPED_SLUICE = blockItem("warped_sluice", BlocksRegistry.WARPED_SLUICE);
    public static final DeferredItem<BlockItem> BAMBOO_SLUICE = blockItem("bamboo_sluice", BlocksRegistry.BAMBOO_SLUICE);

    public static final DeferredItem<BlockItem> IRON_SLUICE = blockItem("iron_sluice", BlocksRegistry.IRON_SLUICE);
    public static final DeferredItem<BlockItem> DIAMOND_SLUICE = blockItem("diamond_sluice", BlocksRegistry.DIAMOND_SLUICE);
    public static final DeferredItem<BlockItem> NETHERITE_SLUICE = blockItem("netherite_sluice", BlocksRegistry.NETHERITE_SLUICE);

    public static final DeferredItem<BlockItem> IRON_AUTO_HAMMER = blockItem("iron_auto_hammer", BlocksRegistry.IRON_AUTO_HAMMER);
    public static final DeferredItem<BlockItem> GOLD_AUTO_HAMMER = blockItem("gold_auto_hammer", BlocksRegistry.GOLD_AUTO_HAMMER);
    public static final DeferredItem<BlockItem> DIAMOND_AUTO_HAMMER = blockItem("diamond_auto_hammer", BlocksRegistry.DIAMOND_AUTO_HAMMER);
    public static final DeferredItem<BlockItem> NETHERITE_AUTO_HAMMER = blockItem("netherite_auto_hammer", BlocksRegistry.NETHERITE_AUTO_HAMMER);

    public static final DeferredItem<BlockItem> STONE_COBBLESTONE_GENERATOR = blockItem("stone_cobblestone_generator", BlocksRegistry.STONE_COBBLESTONE_GENERATOR);
    public static final DeferredItem<BlockItem> IRON_COBBLESTONE_GENERATOR = blockItem("iron_cobblestone_generator", BlocksRegistry.IRON_COBBLESTONE_GENERATOR);
    public static final DeferredItem<BlockItem> GOLD_COBBLESTONE_GENERATOR = blockItem("gold_cobblestone_generator", BlocksRegistry.GOLD_COBBLESTONE_GENERATOR);
    public static final DeferredItem<BlockItem> DIAMOND_COBBLESTONE_GENERATOR = blockItem("diamond_cobblestone_generator", BlocksRegistry.DIAMOND_COBBLESTONE_GENERATOR);
    public static final DeferredItem<BlockItem> NETHERITE_COBBLESTONE_GENERATOR = blockItem("netherite_cobblestone_generator", BlocksRegistry.NETHERITE_COBBLESTONE_GENERATOR);
    public static final DeferredItem<BlockItem> STONE_BASALT_GENERATOR = blockItem("stone_basalt_generator", BlocksRegistry.STONE_BASALT_GENERATOR);
    public static final DeferredItem<BlockItem> IRON_BASALT_GENERATOR = blockItem("iron_basalt_generator", BlocksRegistry.IRON_BASALT_GENERATOR);
    public static final DeferredItem<BlockItem> GOLD_BASALT_GENERATOR = blockItem("gold_basalt_generator", BlocksRegistry.GOLD_BASALT_GENERATOR);
    public static final DeferredItem<BlockItem> DIAMOND_BASALT_GENERATOR = blockItem("diamond_basalt_generator", BlocksRegistry.DIAMOND_BASALT_GENERATOR);
    public static final DeferredItem<BlockItem> NETHERITE_BASALT_GENERATOR = blockItem("netherite_basalt_generator", BlocksRegistry.NETHERITE_BASALT_GENERATOR);

    public static final DeferredItem<BlockItem> PUMP = blockItem("pump", BlocksRegistry.PUMP);

    public static final DeferredItem<BlockItem> DRIPPER = blockItem("dripper", BlocksRegistry.DRIPPER);

    public static final DeferredItem<BlockItem> FUSING_MACHINE = blockItem("fusing_machine", BlocksRegistry.FUSING_MACHINE);
    public static final DeferredItem<BlockItem> SUPER_COOLER = blockItem("super_cooler", BlocksRegistry.SUPER_COOLER);

    public static final DeferredItem<BlockItem> CAST_IRON_BLOCK = blockItem("cast_iron_block", BlocksRegistry.CAST_IRON_BLOCK);

    public static final DeferredItem<BlockItem> DUST = blockItem("dust", BlocksRegistry.DUST_BLOCK);
    public static final DeferredItem<BlockItem> CRUSHED_BASALT = blockItem("crushed_basalt", BlocksRegistry.CRUSHED_BASALT);
    public static final DeferredItem<BlockItem> CRUSHED_ENDSTONE = blockItem("crushed_endstone", BlocksRegistry.CRUSHED_ENDSTONE);
    public static final DeferredItem<BlockItem> CRUSHED_NETHERRACK = blockItem("crushed_netherrack", BlocksRegistry.CRUSHED_NETHERRACK);

    public static final DeferredItem<BlockItem> TUBE = blockItem("tube", BlocksRegistry.TUBE);
    public static final DeferredItem<BlockItem> JAR = blockItem("jar", BlocksRegistry.JAR);
    public static final DeferredItem<BlockItem> TEMPERED_JAR = blockItem("tempered_jar", BlocksRegistry.TEMPERED_JAR);
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

    public static final DeferredItem<BlockItem> WHITE_BARREL = blockItem("white_barrel", BlocksRegistry.WHITE_BARREL);
    public static final DeferredItem<BlockItem> GREEN_BARREL = blockItem("green_barrel", BlocksRegistry.GREEN_BARREL);
    public static final DeferredItem<BlockItem> BLUE_BARREL = blockItem("blue_barrel", BlocksRegistry.BLUE_BARREL);
    public static final DeferredItem<BlockItem> PURPLE_BARREL = blockItem("purple_barrel", BlocksRegistry.PURPLE_BARREL);
    public static final DeferredItem<BlockItem> RED_BARREL = blockItem("red_barrel", BlocksRegistry.RED_BARREL);
    public static final DeferredItem<BlockItem> BLACK_BARREL = blockItem("black_barrel", BlocksRegistry.BLACK_BARREL);
    public static final DeferredItem<BlockItem> GOLDEN_BARREL = blockItem("golden_barrel", BlocksRegistry.GOLDEN_BARREL);

    public static final DeferredItem<BlockItem> SMALL_CRATE = blockItem("small_crate", BlocksRegistry.SMALL_CRATE);
    public static final DeferredItem<BlockItem> CRATE = blockItem("crate", BlocksRegistry.CRATE);
    public static final DeferredItem<BlockItem> PULSATING_CRATE = blockItem("pulsating_crate", BlocksRegistry.PULSATING_CRATE);

    public static final DeferredItem<BlockItem> ACACIA_STRAINER = blockItem("acacia_water_strainer", BlocksRegistry.ACACIA_STRAINER);
    public static final DeferredItem<BlockItem> BAMBOO_STRAINER = blockItem("bamboo_water_strainer", BlocksRegistry.BAMBOO_STRAINER);
    public static final DeferredItem<BlockItem> BIRCH_STRAINER = blockItem("birch_water_strainer", BlocksRegistry.BIRCH_STRAINER);
    public static final DeferredItem<BlockItem> CHERRY_STRAINER = blockItem("cherry_water_strainer", BlocksRegistry.CHERRY_STRAINER);
    public static final DeferredItem<BlockItem> CRIMSON_STRAINER = blockItem("crimson_water_strainer", BlocksRegistry.CRIMSON_STRAINER);
    public static final DeferredItem<BlockItem> DARK_OAK_STRAINER = blockItem("dark_oak_water_strainer", BlocksRegistry.DARK_OAK_STRAINER);
    public static final DeferredItem<BlockItem> JUNGLE_STRAINER = blockItem("jungle_water_strainer", BlocksRegistry.JUNGLE_STRAINER);
    public static final DeferredItem<BlockItem> MANGROVE_STRAINER = blockItem("mangrove_water_strainer", BlocksRegistry.MANGROVE_STRAINER);
    public static final DeferredItem<BlockItem> OAK_STRAINER = blockItem("oak_water_strainer", BlocksRegistry.OAK_STRAINER);
    public static final DeferredItem<BlockItem> SPRUCE_STRAINER = blockItem("spruce_water_strainer", BlocksRegistry.SPRUCE_STRAINER);
    public static final DeferredItem<BlockItem> WARPED_STRAINER = blockItem("warped_water_strainer", BlocksRegistry.WARPED_STRAINER);

    static {
        BlocksRegistry.allCompressedBlocks().forEach(ITEMS::registerSimpleBlockItem);
    }

    //#endregion

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static DeferredItem<Item> simpleItem(String id) {
        return ITEMS.registerSimpleItem(id, new Item.Properties());
    }

    public static DeferredItem<BlockItem> blockItem(String id, Supplier<? extends Block> sup) {
        return ITEMS.registerSimpleBlockItem(id, sup);
    }

    private static DeferredItem<HammerItem> registerHammer(String name, Tiers tier) {
        return ITEMS.registerItem(name, props -> new HammerItem(tier,
                new Item.Properties().attributes(DiggerItem.createAttributes(tier, 1.0F, -2.8F))
        ));
    }
}
