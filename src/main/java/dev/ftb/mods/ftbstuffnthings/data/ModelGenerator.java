package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlock;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ModelGenerator extends ModelProvider {
    private static final Identifier GENERATED = Identifier.parse("item/generated");

    private static final List<DirRotation> HORIZONTALS = List.of(
        new DirRotation(Direction.NORTH, 0, BlockModelGenerators.NOP),
        new DirRotation(Direction.EAST, 90, BlockModelGenerators.Y_ROT_90),
        new DirRotation(Direction.SOUTH, 180, BlockModelGenerators.Y_ROT_180),
        new DirRotation(Direction.WEST, 270, BlockModelGenerators.Y_ROT_270)
    );

    public ModelGenerator(PackOutput output) {
        super(output, FTBStuffNThings.MOD_ID);
    }

    private static final TextureMapping EMPTY_MAPPING = new TextureMapping();

    // Template slots
    private static final TextureSlot SLOT_0 = TextureSlot.create("0");

    // Model templates
    private static final ModelTemplate SLUICE_BODY_TEMPLATE = simpleBlockTemplate("sluice_body", SLOT_0);
    private static final ModelTemplate SLUICE_FRONT_TEMPLATE = simpleBlockTemplate("sluice_front", SLOT_0);

    private static final ModelTemplate MESH_TEMPLATE = simpleBlockTemplate("mesh", SLOT_0);

    private static final ModelTemplate GENERATOR_TEMPLATE_STONE = simpleBlockTemplate("block/cobblestone_generator", SLOT_0, TextureSlot.PARTICLE);
    private static final ModelTemplate GENERATOR_TEMPLATE_BASALT = simpleBlockTemplate("block/basalt_generator", SLOT_0, TextureSlot.PARTICLE);

    // TODO: REMOVE LATER
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(
                Stream.of(
                        BlocksRegistry.PUMP,
                        BlocksRegistry.DRIPPER
                ), // hacks.
                BlocksRegistry.COBBLEGENS.stream(),
                BlocksRegistry.BASALTGENS.stream(),
                BlocksRegistry.ALL_SLUICES.stream()
        ).reduce(Stream.empty(), Stream::concat);
    }

    // TODO: REMOVE LATER
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
            ItemsRegistry.DRIPPER
        );
    }


    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        BlocksRegistry.ALL_SLUICES.forEach(e -> this.registerSluice(blockModels, itemModels, e));

        createModelParentedBlock(blockModels, BlocksRegistry.DRIPPER, "dripper_base");

        // Complex types
        registerMeshes(blockModels, itemModels);
        registerPump(blockModels);
        registerGenerators(blockModels);
        registerAutoHammer(blockModels, itemModels);


        //#region TODO: Remove later
        if (true) {
            return;
        }

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

//        itemModels.generateFlatItem(ItemsRegistry.OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/oak_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.SPRUCE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/spruce_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/spruce_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.BIRCH_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/birch_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/birch_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.JUNGLE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/jungle_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/jungle_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.ACACIA_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/acacia_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/acacia_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.DARK_OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/dark_oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/dark_oak_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.MANGROVE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/mangrove_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/mangrove_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.CHERRY_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/cherry_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/cherry_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.PALE_OAK_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/pale_oak_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/pale_oak_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.CRIMSON_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/crimson_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/crimson_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.WARPED_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/warped_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/warped_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.BAMBOO_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/bamboo_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/bamboo_sluice"));
//
//        itemModels.generateFlatItem(ItemsRegistry.IRON_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/iron_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/iron_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.DIAMOND_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/diamond_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/diamond_sluice"));
//        itemModels.generateFlatItem(ItemsRegistry.NETHERITE_SLUICE.get(), ModelTemplates.FLAT_ITEM);//"item/netherite_sluice", modLoc("item/sluice"), "0", modLoc("block/sluice/netherite_sluice"));

//        BlocksRegistry.allCompressedBlocks().forEach(db -> simpleBlockItem(db.get()));
        //#endregion
    }

    private void registerAutoHammer(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var baseSlot = TextureSlot.create("base");
        var hammerSlot = TextureSlot.create("hammer");

        ModelTemplate blockTemplate = simpleBlockTemplate("auto_hammer", baseSlot, hammerSlot);
        ModelTemplate activeBlockTemplate = simpleBlockTemplate("auto_hammer_active", baseSlot, hammerSlot);

        // Create the models
        String[] materials = new String[] {"iron", "gold", "diamond", "netherite"};
        for (String material : materials) {
            applyBlockTemplate(blockTemplate, material + "_auto_hammer", new TextureMapping()
                    .put(baseSlot, blockMaterial("auto_hammer_base"))
                    .put(hammerSlot, blockMaterial("auto_hammer/" + material)), blockModels);

            applyBlockTemplate(activeBlockTemplate, material + "_auto_hammer_active", new TextureMapping()
                    .put(baseSlot, blockMaterial("auto_hammer_base"))
                    .put(hammerSlot, blockMaterial("auto_hammer/" + material + "_active")), blockModels);
        }

        // States
        Stream.of(BlocksRegistry.IRON_AUTO_HAMMER, BlocksRegistry.GOLD_AUTO_HAMMER, BlocksRegistry.DIAMOND_AUTO_HAMMER, BlocksRegistry.NETHERITE_AUTO_HAMMER).forEach(block -> {
            MultiPartGenerator gen = MultiPartGenerator.multiPart(block.get());
            String path = block.getId().getPath();

            for (DirRotation horizontal : HORIZONTALS) {
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, false).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path, horizontal.mutator));
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path + "_active", horizontal.mutator));
            }

            blockModels.blockStateOutput.accept(gen);
        });
    }

    private void registerGenerators(BlockModelGenerators blockModels) {
        // Create the base model for all the variants to use
        String[] textureTypes = new String[] {"cobblestone", "iron_block", "gold_block", "diamond_block", "netherite_block"};

        Stream.of("cobblestone", "basalt").forEach(genType -> {
            ModelTemplate template = genType.equals("basalt") ? GENERATOR_TEMPLATE_BASALT : GENERATOR_TEMPLATE_STONE;

            for (String textureType : textureTypes) {
                var vanillaMaterial = blockMaterial(textureType);

                var ourNaming = textureType.replace("cobble", "").replace("_block", "");
                var textures = new TextureMapping()
                        .put(SLOT_0, blockMaterial("/generator/" + ourNaming))
                        .put(TextureSlot.PARTICLE, vanillaMaterial);

                String generatorName = ourNaming + "_" + genType + "_generator";
                applyBlockTemplate(template, generatorName, textures, blockModels);
            }
        });

        // Now we need state configs for all variants but we can reuse the same models.
        Stream.concat(BlocksRegistry.COBBLEGENS.stream(), BlocksRegistry.BASALTGENS.stream()).forEach(block -> {
            MultiPartGenerator generator = MultiPartGenerator.multiPart(block.get());
            for (DirRotation horizontal : HORIZONTALS) {
                generator.with(
                        new ConditionBuilder().term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(blockId(block.getId().getPath()), horizontal.mutator())
                );
            }
            blockModels.blockStateOutput.accept(generator);
        });
    }

    void registerSluice(BlockModelGenerators generators, ItemModelGenerators itemModels, DeferredBlock<SluiceBlock> block) {
        String type = block.get().getSluiceType().getSerializedName();
        Material texture = blockMaterial("sluice/" + type + "_sluice");

        applyBlockTemplate(SLUICE_BODY_TEMPLATE, type + "_sluice_body", SLOT_0, texture, generators);
        applyBlockTemplate(SLUICE_FRONT_TEMPLATE, type + "_sluice_front", SLOT_0, texture, generators);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block.get());

        for (DirRotation horizontal : HORIZONTALS) {
            ConditionBuilder mainCondition = new ConditionBuilder()
                    .term(BlockStateProperties.HORIZONTAL_FACING, horizontal.direction())
                    .term(SluiceBlock.PART, SluiceBlock.Part.MAIN);

            ConditionBuilder funnelCondition = new ConditionBuilder()
                    .term(BlockStateProperties.HORIZONTAL_FACING, horizontal.direction())
                    .term(SluiceBlock.PART, SluiceBlock.Part.FUNNEL);

            generator.with(mainCondition, multiVariant(blockId(type + "_sluice_body"), horizontal.mutator()));
            generator.with(funnelCondition, multiVariant(blockId(type + "_sluice_front"), horizontal.mutator()));

            for (MeshType meshType : MeshType.NON_EMPTY_VALUES) {
                Identifier meshId = blockId(meshType.getSerializedName() + "_mesh");
                ConditionBuilder meshCondition = new ConditionBuilder()
                        .term(SluiceBlock.MESH, meshType)
                        .term(HORIZONTAL_FACING, horizontal.direction())
                        .term(SluiceBlock.PART, SluiceBlock.Part.MAIN);

                generator.with(meshCondition, multiVariant(meshId, horizontal.mutator()));
            }
        }

        generators.blockStateOutput.accept(generator);

        // Item model
        var template = simpleItemTemplate("sluice", SLOT_0);
        applyItemTemplate(template, type + "_sluice", new TextureMapping().put(SLOT_0, texture), itemModels);

        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(itemId(type + "_sluice")));
    }

    private void registerMeshes(BlockModelGenerators generators, ItemModelGenerators itemModels) {
        for (MeshType meshType : MeshType.values()) {
            var typeName = meshType.getSerializedName();

            generators.itemModelOutput.accept(meshType.asItem(), ItemModelUtils.plainModel(itemId(typeName + "_mesh")));
            applyBlockTemplate(MESH_TEMPLATE, typeName + "_mesh", SLOT_0, blockMaterial("mesh/" + typeName), generators);

            // Item model
            applyItemTemplate(MESH_TEMPLATE, typeName + "_mesh", new TextureMapping().put(SLOT_0, blockMaterial("mesh/" + typeName)), itemModels);
        }
    }

    private void registerPump(BlockModelGenerators generators) {
        fromBlock(generators, ItemsRegistry.PUMP, "block/pump_off");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(BlocksRegistry.PUMP.get());

        for (DirRotation horizontal : HORIZONTALS) {
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, false).term(HORIZONTAL_FACING, horizontal.direction()),
                    multiVariant(blockId("pump_off"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()),
                    multiVariant(blockId("pump_on"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()).term(PumpBlock.PROGRESS, PumpBlock.Progress.TWENTY),
                    multiVariant(blockId("pump_20"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()).term(PumpBlock.PROGRESS, PumpBlock.Progress.FORTY),
                    multiVariant(blockId("pump_40"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()).term(PumpBlock.PROGRESS, PumpBlock.Progress.SIXTY),
                    multiVariant(blockId("pump_60"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()).term(PumpBlock.PROGRESS, PumpBlock.Progress.EIGHTY),
                    multiVariant(blockId("pump_80"), horizontal.mutator()));
            generator.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()).term(PumpBlock.PROGRESS, PumpBlock.Progress.HUNDRED),
                    multiVariant(blockId("pump_100"), horizontal.mutator()));
        }

        generators.blockStateOutput.accept(generator);
    }

    void fromBlock(BlockModelGenerators gen, DeferredItem<? extends Item> item, String parentPath) {
        gen.registerSimpleItemModel(item.get(), modLocation(parentPath));
    }

    private void simpleItem(ItemModelGenerators itemModels, DeferredItem<? extends Item> item, String... textures) {
        simpleItem(itemModels, item.get(), textures);
    }

    private void simpleItem(ItemModelGenerators itemModels, Item item, String... textures) {
        Identifier modelId = ModelLocationUtils.getModelLocation(item);

        TextureMapping mapping = new TextureMapping();
        TextureSlot[] layers = new TextureSlot[textures.length];
        for (int i = 0; i < textures.length; i++) {
            layers[i] = TextureSlot.create("layer" + i);
            mapping.put(layers[i], new Material(FTBStuffNThings.id(textures[i])));
        }

        new ModelTemplate(Optional.of(Identifier.parse("item/generated")), Optional.empty(), layers)
                .create(modelId, mapping, itemModels.modelOutput);

        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelId));
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

    //#region Helpers
    void createModelParentedBlock(BlockModelGenerators blockModels, DeferredBlock<?> block, String modelLoc) {
        var template = simpleBlockTemplate(modelLoc);
        var modelId = applyBlockTemplate(template, block.getId().getPath(), blockModels);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block.get(), multiVariant(modelId)));
    }

    MultiVariant multiVariant(Identifier id) {
        return new MultiVariant(WeightedList.of(new Variant(id)));
    }

    MultiVariant multiVariant(Identifier id, VariantMutator mutator) {
        return new MultiVariant(WeightedList.of(new Variant(id).with(mutator)));
    }

    MultiVariant multiVariant(String id, VariantMutator mutator) {
        return new MultiVariant(WeightedList.of(new Variant(FTBStuffNThings.id(id)).with(mutator)));
    }

    static ModelTemplate simpleBlockTemplate(String path, TextureSlot... slots) {
        return new ModelTemplate(
                Optional.of(blockId(path)),
                Optional.empty(),
                slots
        );
    }

    static ModelTemplate simpleBlockTemplate(String path) {
        return simpleBlockTemplate(path, new TextureSlot[]{});
    }

    static ModelTemplate simpleBlockTemplateAllTexture(String path) {
        return simpleBlockTemplate(path, TextureSlot.ALL);
    }

    static ModelTemplate simpleItemTemplate(String path, TextureSlot... slots) {
        return new ModelTemplate(
                Optional.of(itemId(path)),
                Optional.empty(),
                slots
        );
    }

    Identifier applyBlockTemplate(ModelTemplate template, String id, BlockModelGenerators generators) {
        return template.create(blockId(id), EMPTY_MAPPING, generators.modelOutput);
    }

    Identifier applyBlockTemplate(ModelTemplate template, String id, TextureMapping mapping, BlockModelGenerators generators) {
        return template.create(blockId(id), mapping, generators.modelOutput);
    }

    Identifier applyBlockTemplate(ModelTemplate template, String id, TextureSlot slot, Material texture, BlockModelGenerators generators) {
        return template.create(blockId(id), new TextureMapping().put(slot, texture), generators.modelOutput);
    }

    Identifier applyItemTemplate(ModelTemplate template, String id, TextureMapping mapping, ItemModelGenerators generators) {
        return template.create(itemId(id), mapping, generators.modelOutput);
    }

    Identifier applyItemTemplate(ModelTemplate template, String id, TextureSlot slot, Material texture, ItemModelGenerators generators) {
        return template.create(itemId(id), new TextureMapping().put(slot, texture), generators.modelOutput);
    }

    Material blockMaterial(String path) {
        return new Material(blockId(path));
    }

    static Identifier blockId(String path) {
        return Identifier.fromNamespaceAndPath(FTBStuffNThings.MOD_ID, "block/" + path);
    }

    static Identifier itemId(String path) {
        return Identifier.fromNamespaceAndPath(FTBStuffNThings.MOD_ID, "item/" + path);
    }

    //endregion

    private record DirRotation(Direction direction, int rotation, VariantMutator mutator) {
    }
}
