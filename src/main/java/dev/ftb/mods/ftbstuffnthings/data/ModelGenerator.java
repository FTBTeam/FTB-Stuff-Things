package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlock;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.client.data.models.BlockModelGenerators.variant;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ModelGenerator extends ModelProvider {
    private static final List<DirRotation> HORIZONTALS = List.of(
            new DirRotation(Direction.NORTH, 0, BlockModelGenerators.NOP),
            new DirRotation(Direction.EAST, 90, BlockModelGenerators.Y_ROT_90),
            new DirRotation(Direction.SOUTH, 180, BlockModelGenerators.Y_ROT_180),
            new DirRotation(Direction.WEST, 270, BlockModelGenerators.Y_ROT_270)
    );
    private static final List<DirRotation> PUMP_HORIZONTALS = List.of(
            // pump models are rotatated 90 degrees relative to every other model, yay!
            new DirRotation(Direction.NORTH, 0, BlockModelGenerators.Y_ROT_90),
            new DirRotation(Direction.EAST, 90, BlockModelGenerators.Y_ROT_180),
            new DirRotation(Direction.SOUTH, 180, BlockModelGenerators.Y_ROT_270),
            new DirRotation(Direction.WEST, 270, BlockModelGenerators.NOP)
    );

    private static final TextureMapping EMPTY_MAPPING = new TextureMapping();

    // Template slots
    private static final TextureSlot SLOT_0 = TextureSlot.create("0");
    private static final TextureSlot SLOT_COVER = TextureSlot.create("cover");
    private static final TextureSlot SLOT_GLASS_BOTTOM = TextureSlot.create("glass_bottom");
    private static final TextureSlot SLOT_GLASS_SIDE = TextureSlot.create("glass_side");
    private static final TextureSlot SLOT_GLASS_TOP = TextureSlot.create("glass_top");

    // Model templates
    private static final ModelTemplate SLUICE_BODY_TEMPLATE = simpleBlockTemplate("sluice_body", SLOT_0);
    private static final ModelTemplate SLUICE_FRONT_TEMPLATE = simpleBlockTemplate("sluice_front", SLOT_0);
    private static final ModelTemplate MESH_TEMPLATE = simpleBlockTemplate("mesh", SLOT_0);
    private static final ModelTemplate STRAINER_TEMPLATE = simpleBlockTemplate("water_strainer_base", SLOT_0, TextureSlot.PARTICLE);
    private static final ModelTemplate GENERATOR_TEMPLATE_STONE = simpleBlockTemplate("cobblestone_generator", SLOT_0, TextureSlot.PARTICLE);
    private static final ModelTemplate GENERATOR_TEMPLATE_BASALT = simpleBlockTemplate("basalt_generator", SLOT_0, TextureSlot.PARTICLE);

    public ModelGenerator(PackOutput output) {
        super(output, FTBStuffNThings.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Misc simple (cube) blocks
        simpleBlockWithItem(blockModels, BlocksRegistry.BLUE_MAGMA_BLOCK);
        simpleBlockWithItem(blockModels, BlocksRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE);
        simpleBlockWithItem(blockModels, BlocksRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE);
        simpleBlockWithItem(blockModels, BlocksRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE);
        simpleBlockWithItem(blockModels, BlocksRegistry.CAST_IRON_BLOCK);
        simpleBlockWithItem(blockModels, BlocksRegistry.DUST_BLOCK);
        simpleBlockWithItem(blockModels, BlocksRegistry.CRUSHED_BASALT);
        simpleBlockWithItem(blockModels, BlocksRegistry.CRUSHED_ENDSTONE);
        simpleBlockWithItem(blockModels, BlocksRegistry.CRUSHED_NETHERRACK);

        // Simple blocks with pre-created (Blockbench etc.) block models
        createModelParentedBlock(blockModels, BlocksRegistry.DRIPPER, "dripper_base");
        createModelParentedBlock(blockModels, BlocksRegistry.JAR, "jar_base");
        createModelWithExistingParent(blockModels, BlocksRegistry.WOODEN_BASIN);
        createModelWithExistingParent(blockModels, BlocksRegistry.JAR_AUTOMATER);

        // Complex blocks
        registerSluices(blockModels, itemModels);
        registerMeshes(blockModels, itemModels);
        registerPump(blockModels);
        registerGenerators(blockModels, itemModels);
        registerAutoHammers(blockModels, itemModels);
        registerWaterStrainers(blockModels, itemModels);
        registerCratesAndBarrels(blockModels);
        registerProcessingMachines(blockModels, itemModels);
        registerTemperedJar(blockModels, itemModels);
        registerCompressedBlocks(blockModels);

        // Simple items
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

        fromBlock(blockModels, ItemsRegistry.JAR, "block/jar");
        fromBlock(blockModels, ItemsRegistry.WOODEN_BASIN, "block/wooden_basin");
        fromBlock(blockModels, ItemsRegistry.AUTO_PROCESSING_BLOCK, "block/auto_processing_block");
    }

    private void registerCompressedBlocks(BlockModelGenerators blockModels) {
        BlocksRegistry.allCompressedBlocks().forEach(db -> {
            if (db.get() instanceof RotatedPillarBlock) {
                blockModels.createAxisAlignedPillarBlock(db.get(), TexturedModel.COLUMN);
            } else {
                blockModels.createTrivialCube(db.get());
            }
            blockModels.registerSimpleItemModel(db.get(), blockId(db.getId().getPath()));
        });
    }

    private void registerCratesAndBarrels(BlockModelGenerators blockModels) {
        // Crates & Barrels
        BlocksRegistry.BARRELS.forEach((block) -> {
            var name = block.getId().getPath();
            createModelWithExistingParent(blockModels, block);
            blockModels.registerSimpleItemModel(block.asItem(), blockId(name));
        });
        createModelWithExistingParent(blockModels, BlocksRegistry.CRATE);
        blockModels.registerSimpleItemModel(BlocksRegistry.CRATE.asItem(), blockId("crate"));
        createModelWithExistingParent(blockModels, BlocksRegistry.PULSATING_CRATE);
        blockModels.registerSimpleItemModel(BlocksRegistry.PULSATING_CRATE.asItem(), blockId("pulsating_crate"));

        // small crates are rotatable
        var dispatch = PropertyDispatch.initial(HORIZONTAL_FACING);
        for (DirRotation horizontal : HORIZONTALS) {
            dispatch.select(horizontal.direction, multiVariant("block/small_crate", horizontal.mutator));
        }
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(BlocksRegistry.SMALL_CRATE.get()).with(dispatch)
        );
        blockModels.registerSimpleItemModel(BlocksRegistry.SMALL_CRATE.asItem(), blockId("small_crate"));
    }

    private void registerTemperedJar(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var dispatch = PropertyDispatch.initial(TemperedJarBlock.TEMPERATURE, TemperedJarBlock.ACTIVE);
        for (var temp : Temperature.values()) {
            MultiVariant variant = variant(new Variant(blockId("tempered_jar_" + temp.getSerializedName())));
            dispatch.select(temp, true, variant);
            dispatch.select(temp, false, variant);
        }
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(BlocksRegistry.TEMPERED_JAR.get()).with(dispatch)
        );

        for (var temp : Temperature.values()) {
            String tempName = temp.getSerializedName();
            ModelTemplate template = simpleBlockTemplate("jar_base",
                    SLOT_COVER, SLOT_GLASS_BOTTOM, SLOT_GLASS_SIDE, SLOT_GLASS_TOP
            );
            TextureMapping mapping = new TextureMapping()
                    .put(SLOT_COVER, blockMaterial("cast_iron_jar_cover"))
                    .put(SLOT_GLASS_TOP, blockMaterial("jar_glass_tempered_top"))
                    .put(SLOT_GLASS_BOTTOM, blockMaterial("jar_glass_bottom_" + tempName))
                    .put(SLOT_GLASS_SIDE, blockMaterial("jar_glass_side_" + tempName));
            applyBlockTemplate(template, "tempered_jar_" + tempName, mapping, blockModels);
        }

        itemModels.itemModelOutput.accept(ItemsRegistry.TEMPERED_JAR.get(), ItemModelUtils.plainModel(blockId("tempered_jar_normal")));
    }

    private void registerAutoHammers(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var baseSlot = TextureSlot.create("base");
        var hammerSlot = TextureSlot.create("hammer");

        ModelTemplate blockTemplate = simpleBlockTemplate("auto_hammer", baseSlot, hammerSlot);
        ModelTemplate activeBlockTemplate = simpleBlockTemplate("auto_hammer_active", baseSlot, hammerSlot);

        // Block & Item models
        BlocksRegistry.ALL_AUTO_HAMMERS.forEach(db -> {
            String material = db.get().getMaterial();
            applyBlockTemplate(blockTemplate, material + "_auto_hammer", new TextureMapping()
                    .put(baseSlot, blockMaterial("auto_hammer/" + material + "_base"))
                    .put(hammerSlot, blockMaterial("auto_hammer/" + material + "_hammer")), blockModels);

            applyBlockTemplate(activeBlockTemplate, material + "_auto_hammer_active", new TextureMapping()
                    .put(baseSlot, blockMaterial("auto_hammer/" + material + "_base"))
                    .put(hammerSlot, blockMaterial("auto_hammer/" + material + "_hammer")), blockModels);

            itemModels.itemModelOutput.accept(db.asItem(), ItemModelUtils.plainModel(blockId(material + "_auto_hammer")));
        });

        // Blockstates
        BlocksRegistry.ALL_AUTO_HAMMERS.forEach(block -> {
            MultiPartGenerator gen = MultiPartGenerator.multiPart(block.get());
            String path = "block/" + block.getId().getPath();

            for (DirRotation horizontal : HORIZONTALS) {
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, false).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path, horizontal.mutator));
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path + "_active", horizontal.mutator));
            }

            blockModels.blockStateOutput.accept(gen);
        });
    }

    private void registerProcessingMachines(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var block : List.of(BlocksRegistry.FUSING_MACHINE, BlocksRegistry.SUPER_COOLER)) {
            String blockName = block.getId().getPath();
            var mapping = new TextureMapping()
                    .put(TextureSlot.TOP, blockMaterial(blockName + "_top"))
                    .put(TextureSlot.FRONT, blockMaterial(blockName + "_front"))
                    .put(TextureSlot.SIDE, blockMaterial("generic_machine_side"));
            var activeMapping = new TextureMapping()
                    .put(TextureSlot.TOP, blockMaterial(blockName + "_top_active"))
                    .put(TextureSlot.FRONT, blockMaterial(blockName + "_front_active"))
                    .put(TextureSlot.SIDE, blockMaterial("generic_machine_side"));

            applyBlockTemplate(ModelTemplates.CUBE_ORIENTABLE, blockName, mapping, blockModels);
            applyBlockTemplate(ModelTemplates.CUBE_ORIENTABLE, blockName + "_active", activeMapping, blockModels);

            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(blockId(blockName)));

            // blockstates
            MultiPartGenerator gen = MultiPartGenerator.multiPart(block.get());
            String path = "block/" + blockName;
            for (DirRotation horizontal : HORIZONTALS) {
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, false).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path, horizontal.mutator));
                gen.with(new ConditionBuilder().term(AbstractMachineBlock.ACTIVE, true).term(HORIZONTAL_FACING, horizontal.direction()),
                        multiVariant(path + "_active", horizontal.mutator));
            }

            blockModels.blockStateOutput.accept(gen);
        }
    }

    private void registerGenerators(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var blocks = Stream.concat(BlocksRegistry.COBBLEGENS.stream(), BlocksRegistry.BASALTGENS.stream()).toList();

        blocks.forEach(block -> {
            String textureType = block.get().getGeneratorProps().textureId();
            String genType = block.get().getGeneratorProps().resourceId();

            ModelTemplate template = genType.equals("basalt") ? GENERATOR_TEMPLATE_BASALT : GENERATOR_TEMPLATE_STONE;

            var vanillaMaterial = new Material(Identifier.withDefaultNamespace("block/" + textureType));

            var ourNaming = textureType.replace("cobble", "").replace("_block", "");
            var textures = new TextureMapping()
                    .put(SLOT_0, blockMaterial("generator/" + ourNaming))
                    .put(TextureSlot.PARTICLE, vanillaMaterial);

            String generatorName = ourNaming + "_" + genType + "_generator";
            applyBlockTemplate(template, generatorName, textures, blockModels);

            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(blockId(ourNaming + "_" + genType +  "_generator")));
        });

        // Now we need state configs for all variants but we can reuse the same models.
        blocks.forEach(block -> {
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

    void registerSluices(BlockModelGenerators generators, ItemModelGenerators itemModels) {
        BlocksRegistry.ALL_SLUICES.forEach(block -> {
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
        });
    }

    private void registerWaterStrainers(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        BlocksRegistry.waterStrainers().forEach(block -> {
            WoodType type = block.get().getWoodType();
            Identifier strainerBlockModel = blockId(type.name() + "_water_strainer");
            Material material = new Material(blockId("water_strainer/water_strainer_" + type.name()));

            TextureMapping mapping = new TextureMapping()
                    .put(SLOT_0, material)
                    .put(TextureSlot.PARTICLE, material);
            applyBlockTemplate(STRAINER_TEMPLATE, type.name() + "_water_strainer", mapping, blockModels);
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block.get(),
                    BlockModelGenerators.plainVariant(strainerBlockModel)));
            itemModels.itemModelOutput.accept(block.get().asItem(), ItemModelUtils.plainModel(strainerBlockModel));
        });
    }

    private void registerMeshes(BlockModelGenerators generators, ItemModelGenerators itemModels) {
        for (MeshType meshType : MeshType.NON_EMPTY_VALUES) {
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

        for (DirRotation horizontal : PUMP_HORIZONTALS) {
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

    //#region Helpers

    private void fromBlock(BlockModelGenerators gen, DeferredItem<? extends Item> item, String parentPath) {
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

    private void createModelParentedBlock(BlockModelGenerators blockModels, DeferredBlock<?> block, String modelLoc) {
        var template = simpleBlockTemplate(modelLoc);
        var modelId = applyBlockTemplate(template, block.getId().getPath(), blockModels);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block.get(), multiVariant(modelId)));
    }

    private void createModelWithExistingParent(BlockModelGenerators blockModels, DeferredBlock<?> block) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block.get(),
                BlockModelGenerators.plainVariant(blockId(block.getId().getPath()))));
    }

    private void simpleBlockWithItem(BlockModelGenerators blockModels, DeferredBlock<?> block) {
        blockModels.createTrivialCube(block.get());
        blockModels.registerSimpleItemModel(block.asItem(), blockId(block.getId().getPath()));
    }

    private MultiVariant multiVariant(Identifier id) {
        return new MultiVariant(WeightedList.of(new Variant(id)));
    }

    private MultiVariant multiVariant(Identifier id, VariantMutator mutator) {
        return new MultiVariant(WeightedList.of(new Variant(id).with(mutator)));
    }

    private MultiVariant multiVariant(String id, VariantMutator mutator) {
        return new MultiVariant(WeightedList.of(new Variant(FTBStuffNThings.id(id)).with(mutator)));
    }

    private static ModelTemplate simpleBlockTemplate(String path, TextureSlot... slots) {
        return new ModelTemplate(
                Optional.of(blockId(path)),
                Optional.empty(),
                slots
        );
    }

    private static ModelTemplate simpleBlockTemplate(String path) {
        return simpleBlockTemplate(path, new TextureSlot[]{});
    }

    private static ModelTemplate simpleBlockTemplateAllTexture(String path) {
        return simpleBlockTemplate(path, TextureSlot.ALL);
    }

    private static ModelTemplate simpleItemTemplate(String path, TextureSlot... slots) {
        return new ModelTemplate(
                Optional.of(itemId(path)),
                Optional.empty(),
                slots
        );
    }

    private Identifier applyBlockTemplate(ModelTemplate template, String id, BlockModelGenerators generators) {
        return template.create(blockId(id), EMPTY_MAPPING, generators.modelOutput);
    }

    private Identifier applyBlockTemplate(ModelTemplate template, String id, TextureMapping mapping, BlockModelGenerators generators) {
        return template.create(blockId(id), mapping, generators.modelOutput);
    }

    private Identifier applyBlockTemplate(ModelTemplate template, String id, TextureSlot slot, Material texture, BlockModelGenerators generators) {
        return template.create(blockId(id), new TextureMapping().put(slot, texture), generators.modelOutput);
    }

    private Identifier applyItemTemplate(ModelTemplate template, String id, TextureMapping mapping, ItemModelGenerators generators) {
        return template.create(itemId(id), mapping, generators.modelOutput);
    }

    private Material blockMaterial(String path) {
        return new Material(blockId(path));
    }

    private static Identifier blockId(String path) {
        return Identifier.fromNamespaceAndPath(FTBStuffNThings.MOD_ID, "block/" + path);
    }

    private static Identifier itemId(String path) {
        return Identifier.fromNamespaceAndPath(FTBStuffNThings.MOD_ID, "item/" + path);
    }

    //endregion

    private record DirRotation(Direction direction, int rotation, VariantMutator mutator) {
    }
}
