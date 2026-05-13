package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class ModelGenerator extends ModelProvider {
    private static final Identifier GENERATED = Identifier.parse("item/generated");

    private static final List<DirRotation> HORIZONTALS = Util.make(new ArrayList<>(), l -> {
        l.add(new DirRotation(Direction.NORTH, 0));
        l.add(new DirRotation(Direction.EAST, 90));
        l.add(new DirRotation(Direction.SOUTH, 180));
        l.add(new DirRotation(Direction.WEST, 270));
    });

    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final int[] DIRS_ROTATION = {0, 180, 270, 90};

    private record DirRotation(Direction direction, int rotation) {
    }

    public ModelGenerator(PackOutput output) {
        super(output, FTBStuffNThings.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // BLOCKS
//        makeSluice("oak");
//        makeSluice("spruce");
//        makeSluice("birch");
//        makeSluice("jungle");
//        makeSluice("acacia");
//        makeSluice("dark_oak");
//        makeSluice("mangrove");
//        makeSluice("cherry");
//        makeSluice("pale_oak");
//        makeSluice("crimson");
//        makeSluice("warped");
//        makeSluice("bamboo");
//
//        makeSluice("iron");
//        makeSluice("diamond");
//        makeSluice("netherite");
//
//        makeGenerator("cobblestone");
//        makeGenerator("basalt");
//
//        makeHammer();
//        makeMesh();

        // ITEMS
//        String path = BlocksRegistry.PUMP.getKey().location().getPath();
//        this.getBuilder(path).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + path + "_on")));

        fromBlock(blockModels, ItemsRegistry.PUMP, "block/pump_off");

//        withExistingParent("tube", "block/tube_inv");
        fromBlock(blockModels, ItemsRegistry.JAR, "block/jar");
        fromBlock(blockModels, ItemsRegistry.TEMPERED_JAR, "block/tempered_jar_normal");
        fromBlock(blockModels, ItemsRegistry.AUTO_PROCESSING_BLOCK, "block/auto_processing_block");
        fromBlock(blockModels, ItemsRegistry.BLUE_MAGMA_BLOCK, "block/blue_magma_block");
        fromBlock(blockModels, ItemsRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE, "block/creative_low_temperature_source");
        fromBlock(blockModels, ItemsRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE, "block/creative_high_temperature_source");
        fromBlock(blockModels, ItemsRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE, "block/creative_subzero_temperature_source");
        fromBlock(blockModels, ItemsRegistry.CAST_IRON_BLOCK, "block/cast_iron_block");
        fromBlock(blockModels, ItemsRegistry.IRON_AUTO_HAMMER, "block/iron_auto_hammer");
        fromBlock(blockModels, ItemsRegistry.GOLD_AUTO_HAMMER, "block/gold_auto_hammer");
        fromBlock(blockModels, ItemsRegistry.DIAMOND_AUTO_HAMMER, "block/diamond_auto_hammer");
        fromBlock(blockModels, ItemsRegistry.NETHERITE_AUTO_HAMMER, "block/netherite_auto_hammer");
        fromBlock(blockModels, ItemsRegistry.DUST, "block/dust");
        fromBlock(blockModels, ItemsRegistry.CRUSHED_BASALT, "block/crushed_basalt");
        fromBlock(blockModels, ItemsRegistry.CRUSHED_ENDSTONE, "block/crushed_endstone");
        fromBlock(blockModels, ItemsRegistry.CRUSHED_NETHERRACK, "block/crushed_netherrack");

        fromBlock(blockModels, ItemsRegistry.WHITE_BARREL, "block/white_barrel");
        fromBlock(blockModels, ItemsRegistry.GREEN_BARREL, "block/green_barrel");
        fromBlock(blockModels, ItemsRegistry.BLUE_BARREL, "block/blue_barrel");
        fromBlock(blockModels, ItemsRegistry.PURPLE_BARREL, "block/purple_barrel");
        fromBlock(blockModels, ItemsRegistry.RED_BARREL, "block/red_barrel");
        fromBlock(blockModels, ItemsRegistry.BLACK_BARREL, "block/black_barrel");
        fromBlock(blockModels, ItemsRegistry.GOLDEN_BARREL, "block/golden_barrel");

        fromBlock(blockModels, ItemsRegistry.CRATE, "block/crate");
        fromBlock(blockModels, ItemsRegistry.SMALL_CRATE, "block/small_crate");
        fromBlock(blockModels, ItemsRegistry.PULSATING_CRATE, "block/pulsating_crate");

        fromBlock(blockModels, ItemsRegistry.STONE_COBBLESTONE_GENERATOR, "block/stone_cobblestone_generator");
        fromBlock(blockModels, ItemsRegistry.IRON_COBBLESTONE_GENERATOR, "block/iron_cobblestone_generator");
        fromBlock(blockModels, ItemsRegistry.GOLD_COBBLESTONE_GENERATOR, "block/gold_cobblestone_generator");
        fromBlock(blockModels, ItemsRegistry.DIAMOND_COBBLESTONE_GENERATOR, "block/diamond_cobblestone_generator");
        fromBlock(blockModels, ItemsRegistry.NETHERITE_COBBLESTONE_GENERATOR, "block/netherite_cobblestone_generator");
        fromBlock(blockModels, ItemsRegistry.STONE_BASALT_GENERATOR, "block/stone_basalt_generator");
        fromBlock(blockModels, ItemsRegistry.IRON_BASALT_GENERATOR, "block/iron_basalt_generator");
        fromBlock(blockModels, ItemsRegistry.GOLD_BASALT_GENERATOR, "block/gold_basalt_generator");
        fromBlock(blockModels, ItemsRegistry.DIAMOND_BASALT_GENERATOR, "block/diamond_basalt_generator");
        fromBlock(blockModels, ItemsRegistry.NETHERITE_BASALT_GENERATOR, "block/netherite_basalt_generator");

        fromBlock(blockModels, ItemsRegistry.CLOTH_MESH, "block/cloth_mesh");
        fromBlock(blockModels, ItemsRegistry.IRON_MESH, "block/iron_mesh");
        fromBlock(blockModels, ItemsRegistry.GOLD_MESH, "block/gold_mesh");
        fromBlock(blockModels, ItemsRegistry.DIAMOND_MESH, "block/diamond_mesh");
        fromBlock(blockModels, ItemsRegistry.BLAZING_MESH, "block/blazing_mesh");

        fromBlock(blockModels, ItemsRegistry.WOODEN_BASIN, "block/wooden_basin");

        fromBlock(blockModels, ItemsRegistry.ACACIA_STRAINER, "block/acacia_water_strainer");
        fromBlock(blockModels, ItemsRegistry.BAMBOO_STRAINER, "block/bamboo_water_strainer");
        fromBlock(blockModels, ItemsRegistry.BIRCH_STRAINER, "block/birch_water_strainer");
        fromBlock(blockModels, ItemsRegistry.CHERRY_STRAINER, "block/cherry_water_strainer");
        fromBlock(blockModels, ItemsRegistry.CRIMSON_STRAINER, "block/crimson_water_strainer");
        fromBlock(blockModels, ItemsRegistry.DARK_OAK_STRAINER, "block/dark_oak_water_strainer");
        fromBlock(blockModels, ItemsRegistry.JUNGLE_STRAINER, "block/jungle_water_strainer");
        fromBlock(blockModels, ItemsRegistry.MANGROVE_STRAINER, "block/mangrove_water_strainer");
        fromBlock(blockModels, ItemsRegistry.OAK_STRAINER, "block/oak_water_strainer");
        fromBlock(blockModels, ItemsRegistry.SPRUCE_STRAINER, "block/spruce_water_strainer");
        fromBlock(blockModels, ItemsRegistry.WARPED_STRAINER, "block/warped_water_strainer");

        simpleItem(itemModels, ItemsRegistry.FLUID_CAPSULE, "item/fluid_container_base", "item/fluid_container_overlay");
        simpleItem(itemModels, ItemsRegistry.DRIPPER, "item/dripper");
        simpleItem(itemModels, ItemsRegistry.WATER_BOWL, "item/water_bowl");

        simpleItem(itemModels, ItemsRegistry.CAST_IRON_GEAR, "item/cast_iron_gear");
        simpleItem(itemModels, ItemsRegistry.CAST_IRON_INGOT, "item/cast_iron_ingot");
        simpleItem(itemModels, ItemsRegistry.CAST_IRON_NUGGET, "item/cast_iron_nugget");
        simpleItem(itemModels, ItemsRegistry.TEMPERED_GLASS, "item/tempered_glass");

        simpleItem(itemModels, ItemsRegistry.STONE_HAMMER, "item/stone_hammer");
        simpleItem(itemModels, ItemsRegistry.IRON_HAMMER, "item/iron_hammer");
        simpleItem(itemModels, ItemsRegistry.GOLD_HAMMER, "item/gold_hammer");
        simpleItem(itemModels, ItemsRegistry.DIAMOND_HAMMER, "item/diamond_hammer");
        simpleItem(itemModels, ItemsRegistry.NETHERITE_HAMMER, "item/netherite_hammer");

        simpleItem(itemModels, ItemsRegistry.CROOK, "item/stone_crook");
        simpleItem(itemModels, ItemsRegistry.STONE_ROD, "item/stone_rod");

        itemModels.generateFlatItem(ItemsRegistry.OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/oak_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.SPRUCE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/spruce_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/spruce_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.BIRCH_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/birch_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/birch_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.JUNGLE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/jungle_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/jungle_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.ACACIA_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/acacia_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/acacia_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.DARK_OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/dark_oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/dark_oak_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.MANGROVE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/mangrove_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/mangrove_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.CHERRY_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/cherry_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/cherry_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.PALE_OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/pale_oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/pale_oak_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.CRIMSON_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/crimson_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/crimson_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.WARPED_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/warped_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/warped_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.BAMBOO_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/bamboo_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/bamboo_sluice"));

        itemModels.generateFlatItem(ItemsRegistry.IRON_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/iron_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/iron_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.DIAMOND_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/diamond_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/diamond_sluice"));
        itemModels.generateFlatItem(ItemsRegistry.NETHERITE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/netherite_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/netherite_sluice"));

//        BlocksRegistry.allCompressedBlocks().forEach(db -> simpleBlockItem(db.get()));
    }

    void fromBlock(BlockModelGenerators gen, DeferredItem<? extends Item> item, String parentPath) {
        gen.registerSimpleItemModel(item.get(), modLocation(parentPath));
    }

    private void simpleItem(ItemModelGenerators itemModels, DeferredItem<? extends Item> item, String... textures) {
        simpleItem(itemModels, item.get(), textures);
    }

    private void simpleItem(ItemModelGenerators itemModels, Item item, String... textures) {
        List<TextureSlot> slots = new ArrayList<>();
        for (int i = 0; i < textures.length; i++) {
            slots.add(TextureSlot.create("layer" + i));
        }

        itemModels.generateFlatItem(item, ModelTemplates.create(slots.toArray(new TextureSlot[0])));
    }

//    private void makeSluice(String type) {
//        singleTexture("block/" + type + "_sluice_body", getLoc("sluice_body"), "0", getLoc("sluice/" + type + "_sluice"));
//        singleTexture("block/" + type + "_sluice_front", getLoc("sluice_front"), "0", getLoc("sluice/" + type + "_sluice"));
//    }
//
//    private void makeGenerator(String type) {
//        withExistingParent("block/stone_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/stone")).texture("particle", getMCLoc("cobblestone"));
//        withExistingParent("block/iron_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/iron")).texture("particle", getMCLoc("iron_block"));
//        withExistingParent("block/gold_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/gold")).texture("particle", getMCLoc("gold_block"));
//        withExistingParent("block/diamond_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/diamond")).texture("particle", getMCLoc("diamond_block"));
//        withExistingParent("block/netherite_" + type + "_generator", getLoc(type + "_generator")).texture("0", getLoc("generator/netherite")).texture("particle", getMCLoc("netherite_block"));
//    }
//
//    private void makeHammer() {
//        withExistingParent("block/iron_auto_hammer", getLoc("auto_hammer")).texture("base", getLoc("auto_hammer/iron_base")).texture("hammer", getLoc("auto_hammer/iron_hammer"));
//        withExistingParent("block/gold_auto_hammer", getLoc("auto_hammer")).texture("base", getLoc("auto_hammer/gold_base")).texture("hammer", getLoc("auto_hammer/gold_hammer"));
//        withExistingParent("block/diamond_auto_hammer", getLoc("auto_hammer")).texture("base", getLoc("auto_hammer/diamond_base")).texture("hammer", getLoc("auto_hammer/diamond_hammer"));
//        withExistingParent("block/netherite_auto_hammer", getLoc("auto_hammer")).texture("base", getLoc("auto_hammer/netherite_base")).texture("hammer", getLoc("auto_hammer/netherite_hammer"));
//
//        withExistingParent("block/iron_auto_hammer_active", getLoc("auto_hammer_active")).texture("base", getLoc("auto_hammer/iron_base")).texture("hammer", getLoc("auto_hammer/iron_hammer_active"));
//        withExistingParent("block/gold_auto_hammer_active", getLoc("auto_hammer_active")).texture("base", getLoc("auto_hammer/gold_base")).texture("hammer", getLoc("auto_hammer/gold_hammer_active"));
//        withExistingParent("block/diamond_auto_hammer_active", getLoc("auto_hammer_active")).texture("base", getLoc("auto_hammer/diamond_base")).texture("hammer", getLoc("auto_hammer/diamond_hammer_active"));
//        withExistingParent("block/netherite_auto_hammer_active", getLoc("auto_hammer_active")).texture("base", getLoc("auto_hammer/netherite_base")).texture("hammer", getLoc("auto_hammer/netherite_hammer_active"));
//    }
//
//    private void makeMesh() {
//        singleTexture("block/cloth_mesh", getLoc("mesh"), "0", getLoc("mesh/cloth"));
//        singleTexture("block/iron_mesh", getLoc("mesh"), "0", getLoc("mesh/iron"));
//        singleTexture("block/gold_mesh", getLoc("mesh"), "0", getLoc("mesh/gold"));
//        singleTexture("block/diamond_mesh", getLoc("mesh"), "0", getLoc("mesh/diamond"));
//        singleTexture("block/blazing_mesh", getLoc("mesh"), "0", getLoc("mesh/blazing"));
//    }
}
