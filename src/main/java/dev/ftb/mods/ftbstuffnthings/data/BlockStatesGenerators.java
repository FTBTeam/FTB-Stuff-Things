package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlock;
import dev.ftb.mods.ftbstuffnthings.client.model.TubeModel;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class BlockStatesGenerators extends BlockStateProvider {
    public BlockStatesGenerators(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FTBStuffNThings.MODID, exFileHelper);
    }

    private static final List<DirRotation> HORIZONTALS = Util.make(new ArrayList<>(), l -> {
        l.add(new DirRotation(Direction.NORTH, 0));
        l.add(new DirRotation(Direction.EAST, 90));
        l.add(new DirRotation(Direction.SOUTH, 180));
        l.add(new DirRotation(Direction.WEST, 270));
    });

    @Override
    protected void registerStatesAndModels() {
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        int[] dirsRot = {0, 180, 270, 90};

        // Sluices
        for (DeferredBlock<SluiceBlock> block : BlocksRegistry.ALL_SLUICES) {
            String key = block.getKey().location().getPath();
            MultiPartBlockStateBuilder builder = this.getMultipartBuilder(block.get());

            for (int d = 0; d < 4; d++) {
                builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/" + key + "_body"))).rotationY(dirsRot[d]).addModel().condition(HORIZONTAL_FACING, dirs[d]).condition(SluiceBlock.PART, SluiceBlock.Part.MAIN);
                builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/" + key + "_front"))).rotationY(dirsRot[d]).addModel().condition(HORIZONTAL_FACING, dirs[d]).condition(SluiceBlock.PART, SluiceBlock.Part.FUNNEL);

                for (MeshType type : MeshType.NON_EMPTY_VALUES) {
                    builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/" + type.getSerializedName() + "_mesh"))).rotationY(dirsRot[d]).addModel().condition(SluiceBlock.MESH, type).condition(HORIZONTAL_FACING, dirs[d]).condition(SluiceBlock.PART, SluiceBlock.Part.MAIN);
                }
            }
        }

        // Pump
        int[] dirsRot2 = new int[]{90, 270, 0, 180};
        MultiPartBlockStateBuilder builder = this.getMultipartBuilder(BlocksRegistry.PUMP.get());
        for (int d = 0; d < 4; d++) {
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_off")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, false)
                    .condition(HORIZONTAL_FACING, dirs[d]);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_on")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d]);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_20")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d])
                    .condition(PumpBlock.PROGRESS, PumpBlock.Progress.TWENTY);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_40")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d])
                    .condition(PumpBlock.PROGRESS, PumpBlock.Progress.FORTY);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_60")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d])
                    .condition(PumpBlock.PROGRESS, PumpBlock.Progress.SIXTY);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_80")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d])
                    .condition(PumpBlock.PROGRESS, PumpBlock.Progress.EIGHTY);
            builder.part().modelFile(this.models().getExistingFile(this.modLoc("block/pump_100")))
                    .rotationY(dirsRot2[d]).addModel()
                    .condition(AbstractMachineBlock.ACTIVE, true)
                    .condition(HORIZONTAL_FACING, dirs[d])
                    .condition(PumpBlock.PROGRESS, PumpBlock.Progress.HUNDRED);
        }

        // Auto Hammers
        for (var block : List.of(BlocksRegistry.IRON_AUTO_HAMMER, BlocksRegistry.GOLD_AUTO_HAMMER, BlocksRegistry.DIAMOND_AUTO_HAMMER, BlocksRegistry.NETHERITE_AUTO_HAMMER)) {
            MultiPartBlockStateBuilder b = getMultipartBuilder(block.get());
            String path = block.getId().getPath();
            for (DirRotation d : HORIZONTALS) {
                b.part().modelFile(models().getExistingFile(modLoc("block/" + path)))
                        .rotationY(d.rotation).addModel()
                        .condition(AbstractMachineBlock.ACTIVE, false)
                        .condition(HORIZONTAL_FACING, d.direction);
                b.part().modelFile(models().getExistingFile(modLoc("block/" + path + "_active")))
                        .rotationY(d.rotation).addModel()
                        .condition(AbstractMachineBlock.ACTIVE, true)
                        .condition(HORIZONTAL_FACING, d.direction);
            }
        }

        // Cobble generators
        for (var block : List.of(BlocksRegistry.STONE_COBBLESTONE_GENERATOR, BlocksRegistry.IRON_COBBLESTONE_GENERATOR, BlocksRegistry.GOLD_COBBLESTONE_GENERATOR, BlocksRegistry.DIAMOND_COBBLESTONE_GENERATOR, BlocksRegistry.NETHERITE_COBBLESTONE_GENERATOR)) {
            MultiPartBlockStateBuilder b = getMultipartBuilder(block.get());
            String path = block.getId().getPath();
            for (DirRotation d : HORIZONTALS) {
                b.part().modelFile(models().getExistingFile(modLoc("block/" + path)))
                        .rotationY(d.rotation).addModel()
                        .condition(HORIZONTAL_FACING, d.direction);
            }
        }

        // Basalt generators
        for (var block : List.of(BlocksRegistry.STONE_BASALT_GENERATOR, BlocksRegistry.IRON_BASALT_GENERATOR, BlocksRegistry.GOLD_BASALT_GENERATOR, BlocksRegistry.DIAMOND_BASALT_GENERATOR, BlocksRegistry.NETHERITE_BASALT_GENERATOR)) {
            MultiPartBlockStateBuilder b = getMultipartBuilder(block.get());
            String path = block.getId().getPath();
            for (DirRotation d : HORIZONTALS) {
                b.part().modelFile(models().getExistingFile(modLoc("block/" + path)))
                        .rotationY(d.rotation).addModel()
                        .condition(HORIZONTAL_FACING, d.direction);
            }
        }


        // Fusing Machine & Super Cooler
        for (var block: List.of(BlocksRegistry.FUSING_MACHINE, BlocksRegistry.SUPER_COOLER)) {
            var model = machineModel(block, false);
            var activeModel = machineModel(block, true);
            VariantBlockStateBuilder.PartialBlockstate machineBuilder = getVariantBuilder(block.get()).partialState();
            for (DirRotation d : HORIZONTALS) {
                machineBuilder.with(HORIZONTAL_FACING, d.direction).with(AbstractMachineBlock.ACTIVE, false)
                        .setModels(new ConfiguredModel(model, 0, d.rotation, false));
                machineBuilder.with(HORIZONTAL_FACING, d.direction).with(AbstractMachineBlock.ACTIVE, true)
                        .setModels(new ConfiguredModel(activeModel, 0, d.rotation, false));
            }
            simpleBlockItem(block.get(), model);
        }

        // Tubes
        TubeLoaderBuilder tlb = models().getBuilder("block/tube").customLoader(TubeLoaderBuilder::new);
        simpleBlock(BlocksRegistry.TUBE.get(), tlb.end());
        models().withExistingParent("block/tube_inv", modLoc("block/tube_base"));

        // Dripper
        ModelFile dripperModel = models().withExistingParent("block/dripper", modLoc("block/dripper_base"));
        simpleBlock(BlocksRegistry.DRIPPER.get(), dripperModel);

        // Jar
        ModelFile jarModel = models().withExistingParent("block/jar", modLoc("block/jar_base"));
        simpleBlock(BlocksRegistry.JAR.get(), jarModel);

        // Tempered Jar
        getVariantBuilder(BlocksRegistry.TEMPERED_JAR.get()).forAllStates(state -> {
            Temperature temp = state.getValue(TemperedJarBlock.TEMPERATURE);
            return ConfiguredModel.builder().modelFile(
                    models().withExistingParent("tempered_jar_" + temp.getSerializedName(), modLoc("block/jar_base"))
                            .texture("cover", modLoc("block/cast_iron_jar_cover"))
                            .texture("glass_side", modLoc("block/jar_glass_side_" + temp.getSerializedName()))
                            .texture("glass_top", modLoc("block/jar_glass_tempered_top"))
                            .texture("glass_bottom", modLoc("block/jar_glass_bottom_" + temp.getSerializedName()))
            ).build();
        });

        // Jar auto processing block
        simpleBlock(BlocksRegistry.JAR_AUTOMATER.get(), models().getExistingFile(modLoc("block/auto_processing_block")));

        // Crates & Barrels
        BlocksRegistry.BARRELS.forEach((block) -> {
            var name = block.getId().getPath();
            ModelFile model = models().getExistingFile(modLoc("block/" + name));
            simpleBlock(block.get(), model);
        });

        simpleBlock(BlocksRegistry.CRATE.get(), models().getExistingFile(modLoc("block/crate")));
        simpleBlock(BlocksRegistry.PULSATING_CRATE.get(), models().getExistingFile(modLoc("block/pulsating_crate")));

        // Small crate supports rotations
        var smallCrateModel = models().getExistingFile(modLoc("block/small_crate"));
        getVariantBuilder(BlocksRegistry.SMALL_CRATE.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                        .modelFile(smallCrateModel)
                        .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360).build(),
                BlockStateProperties.WATERLOGGED);

        // Water Strainers
        BlocksRegistry.waterStrainers().forEach(this::waterStrainer);

        // Compressed Blocks
        BlocksRegistry.allCompressedBlocks().forEach(db -> {
            if (db.get() instanceof RotatedPillarBlock pillar) {
                ResourceLocation side = FTBStuffNThings.id("block/" + db.getId().getPath() + "_side");
                ResourceLocation end = FTBStuffNThings.id("block/" + db.getId().getPath() + "_top");
                axisBlock(pillar, side, end);
            } else {
                simpleBlock(db.get());
            }
        });

        // Misc simple blocks

        simpleBlock(BlocksRegistry.BLUE_MAGMA_BLOCK.get());
        simpleBlock(BlocksRegistry.CREATIVE_HOT_TEMPERATURE_SOURCE.get());
        simpleBlock(BlocksRegistry.CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE.get());
        simpleBlock(BlocksRegistry.CREATIVE_CHILLED_TEMPERATURE_SOURCE.get());

        simpleBlock(BlocksRegistry.CAST_IRON_BLOCK.get());
        simpleBlock(BlocksRegistry.DUST_BLOCK.get());
        simpleBlock(BlocksRegistry.CRUSHED_BASALT.get());
        simpleBlock(BlocksRegistry.CRUSHED_ENDSTONE.get());
        simpleBlock(BlocksRegistry.CRUSHED_NETHERRACK.get());
    }

    private ModelFile machineModel(DeferredBlock<? extends Block> block, boolean active) {
        String name = block.getId().getPath();
        String suffix = active ? "_active" : "";
        return models().withExistingParent(name + suffix, "block/orientable")
                .texture("top", modLoc("block/" + name + "_top" + suffix))
                .texture("side", modLoc("block/generic_machine_side"))
                .texture("front", modLoc("block/" + name + "_front" + suffix));
    }

    private void waterStrainer(Supplier<WaterStrainerBlock> blockSupplier) {
        WoodType type = blockSupplier.get().getWoodType();
        simpleBlock(blockSupplier.get(), models().withExistingParent(type.name() + "_water_strainer", modLoc("block/water_strainer_base"))
                .texture("0", "block/water_strainer/water_strainer_" + type.name())
                .texture("particle", "block/water_strainer/water_strainer_" + type.name())
        );
    }
    private static class TubeLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {
        public TubeLoaderBuilder(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
            super(TubeModel.Loader.ID, parent, existingFileHelper, false);
        }
    }

    private record DirRotation(Direction direction, int rotation) {
    }
}
