package dev.ftb.mods.ftbstuffnthings.registry;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.SimpleFallingBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.BasaltgenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.dripper.DripperBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.CreativeTemperatureSourceBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.JarAutomaterBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.JarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.BarrelBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.CrateBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.SmallCrateBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceType;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.supercooler.SuperCoolerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.tube.TubeBlock;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class BlocksRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBStuffNThings.MODID);

    // Sluices
    public static final DeferredBlock<SluiceBlock> OAK_SLUICE
            = BLOCKS.register("oak_sluice", () -> new SluiceBlock(SluiceType.OAK, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> SPRUCE_SLUICE
            = BLOCKS.register("spruce_sluice", () -> new SluiceBlock(SluiceType.SPRUCE, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> BIRCH_SLUICE
            = BLOCKS.register("birch_sluice", () -> new SluiceBlock(SluiceType.BIRCH, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> JUNGLE_SLUICE
            = BLOCKS.register("jungle_sluice", () -> new SluiceBlock(SluiceType.JUNGLE, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> ACACIA_SLUICE
            = BLOCKS.register("acacia_sluice", () -> new SluiceBlock(SluiceType.ACACIA, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> DARK_OAK_SLUICE
            = BLOCKS.register("dark_oak_sluice", () -> new SluiceBlock(SluiceType.DARK_OAK, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> MANGROVE_SLUICE
            = BLOCKS.register("mangrove_sluice", () -> new SluiceBlock(SluiceType.MANGROVE, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> CHERRY_SLUICE
            = BLOCKS.register("cherry_sluice", () -> new SluiceBlock(SluiceType.CHERRY, SoundType.CHERRY_WOOD));
    public static final DeferredBlock<SluiceBlock> PALE_OAK_SLUICE
            = BLOCKS.register("pale_oak_sluice", () -> new SluiceBlock(SluiceType.PALE_OAK, SoundType.WOOD));
    public static final DeferredBlock<SluiceBlock> CRIMSON_SLUICE
            = BLOCKS.register("crimson_sluice", () -> new SluiceBlock(SluiceType.CRIMSON, SoundType.NETHER_WOOD));
    public static final DeferredBlock<SluiceBlock> WARPED_SLUICE
            = BLOCKS.register("warped_sluice", () -> new SluiceBlock(SluiceType.WARPED, SoundType.NETHER_WOOD));
    public static final DeferredBlock<SluiceBlock> BAMBOO_SLUICE
            = BLOCKS.register("bamboo_sluice", () -> new SluiceBlock(SluiceType.BAMBOO, SoundType.BAMBOO_WOOD));
    public static final DeferredBlock<SluiceBlock> IRON_SLUICE
            = BLOCKS.register("iron_sluice", () -> new SluiceBlock(SluiceType.IRON, SoundType.METAL));
    public static final DeferredBlock<SluiceBlock> DIAMOND_SLUICE
            = BLOCKS.register("diamond_sluice", () -> new SluiceBlock(SluiceType.DIAMOND, SoundType.METAL));
    public static final DeferredBlock<SluiceBlock> NETHERITE_SLUICE
            = BLOCKS.register("netherite_sluice", () -> new SluiceBlock(SluiceType.NETHERITE, SoundType.NETHERITE_BLOCK));

    public static List<DeferredBlock<SluiceBlock>> WOODEN_SLUICE = List.of(
            OAK_SLUICE, SPRUCE_SLUICE, BIRCH_SLUICE, JUNGLE_SLUICE, ACACIA_SLUICE,
            DARK_OAK_SLUICE, MANGROVE_SLUICE, CHERRY_SLUICE, PALE_OAK_SLUICE, CRIMSON_SLUICE, WARPED_SLUICE, BAMBOO_SLUICE);
    public static List<DeferredBlock<SluiceBlock>> ALL_SLUICES = List.of(
            OAK_SLUICE, SPRUCE_SLUICE, BIRCH_SLUICE, JUNGLE_SLUICE, ACACIA_SLUICE,
            DARK_OAK_SLUICE, MANGROVE_SLUICE, CHERRY_SLUICE, PALE_OAK_SLUICE, CRIMSON_SLUICE, WARPED_SLUICE,
            BAMBOO_SLUICE, IRON_SLUICE, DIAMOND_SLUICE, NETHERITE_SLUICE);


    // Autohammers
    public static final DeferredBlock<AutoHammerBlock> IRON_AUTO_HAMMER
            = BLOCKS.register("iron_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.IRON));
    public static final DeferredBlock<AutoHammerBlock> GOLD_AUTO_HAMMER
            = BLOCKS.register("gold_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.GOLD));
    public static final DeferredBlock<AutoHammerBlock> DIAMOND_AUTO_HAMMER
            = BLOCKS.register("diamond_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.DIAMOND));
    public static final DeferredBlock<AutoHammerBlock> NETHERITE_AUTO_HAMMER
            = BLOCKS.register("netherite_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.NETHERITE));
    public static final List<DeferredBlock<AutoHammerBlock>> ALL_AUTO_HAMMERS = List.of(IRON_AUTO_HAMMER, GOLD_AUTO_HAMMER, DIAMOND_AUTO_HAMMER, NETHERITE_AUTO_HAMMER);

    // Cobble & Basalt generators
    public static final DeferredBlock<CobblegenBlock> STONE_COBBLESTONE_GENERATOR
            = BLOCKS.register("stone_cobblestone_generator", () -> new CobblegenBlock(CobblegenProperties.STONE));
    public static final DeferredBlock<CobblegenBlock> IRON_COBBLESTONE_GENERATOR
            = BLOCKS.register("iron_cobblestone_generator", () -> new CobblegenBlock(CobblegenProperties.IRON));
    public static final DeferredBlock<CobblegenBlock> GOLD_COBBLESTONE_GENERATOR
            = BLOCKS.register("gold_cobblestone_generator", () -> new CobblegenBlock(CobblegenProperties.GOLD));
    public static final DeferredBlock<CobblegenBlock> DIAMOND_COBBLESTONE_GENERATOR
            = BLOCKS.register("diamond_cobblestone_generator", () -> new CobblegenBlock(CobblegenProperties.DIAMOND));
    public static final DeferredBlock<CobblegenBlock> NETHERITE_COBBLESTONE_GENERATOR
            = BLOCKS.register("netherite_cobblestone_generator", () -> new CobblegenBlock(CobblegenProperties.NETHERITE));
    public static final DeferredBlock<CobblegenBlock> STONE_BASALT_GENERATOR
            = BLOCKS.register("stone_basalt_generator", () -> new CobblegenBlock(BasaltgenProperties.STONE));
    public static final DeferredBlock<CobblegenBlock> IRON_BASALT_GENERATOR
            = BLOCKS.register("iron_basalt_generator", () -> new CobblegenBlock(BasaltgenProperties.IRON));
    public static final DeferredBlock<CobblegenBlock> GOLD_BASALT_GENERATOR
            = BLOCKS.register("gold_basalt_generator", () -> new CobblegenBlock(BasaltgenProperties.GOLD));
    public static final DeferredBlock<CobblegenBlock> DIAMOND_BASALT_GENERATOR
            = BLOCKS.register("diamond_basalt_generator", () -> new CobblegenBlock(BasaltgenProperties.DIAMOND));
    public static final DeferredBlock<CobblegenBlock> NETHERITE_BASALT_GENERATOR
            = BLOCKS.register("netherite_basalt_generator", () -> new CobblegenBlock(BasaltgenProperties.NETHERITE));

    public static final List<DeferredBlock<CobblegenBlock>> COBBLEGENS = List.of(
            STONE_COBBLESTONE_GENERATOR, IRON_COBBLESTONE_GENERATOR, GOLD_COBBLESTONE_GENERATOR,
            DIAMOND_COBBLESTONE_GENERATOR, NETHERITE_COBBLESTONE_GENERATOR
    );
    public static final List<DeferredBlock<CobblegenBlock>> BASALTGENS = List.of(
            STONE_BASALT_GENERATOR, IRON_BASALT_GENERATOR, GOLD_BASALT_GENERATOR,
            DIAMOND_BASALT_GENERATOR, NETHERITE_BASALT_GENERATOR
    );

    // Misc machines
    public static final DeferredBlock<PumpBlock> PUMP
            = BLOCKS.register("pump", PumpBlock::new);
    public static final DeferredBlock<DripperBlock> DRIPPER
            = BLOCKS.register("dripper", DripperBlock::new);
    public static final DeferredBlock<FusingMachineBlock> FUSING_MACHINE
            = BLOCKS.register("fusing_machine", FusingMachineBlock::new);
    public static final DeferredBlock<SuperCoolerBlock> SUPER_COOLER
            = BLOCKS.register("super_cooler", SuperCoolerBlock::new);
    public static final DeferredBlock<TubeBlock> TUBE
            = BLOCKS.register("tube", TubeBlock::new);
    public static final DeferredBlock<JarBlock> JAR
            = BLOCKS.register("jar", JarBlock::new);
    public static final DeferredBlock<TemperedJarBlock> TEMPERED_JAR
            = BLOCKS.register("tempered_jar", TemperedJarBlock::new);
    public static final DeferredBlock<JarAutomaterBlock> JAR_AUTOMATER
            = BLOCKS.register("auto_processing_block", JarAutomaterBlock::new);
    public static final DeferredBlock<Block> BLUE_MAGMA_BLOCK
            = BLOCKS.register("blue_magma_block", () -> new MagmaBlock(
            Block.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.NETHER)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 3)
                    .randomTicks()
                    .strength(0.5F)
                    .isValidSpawn((state, level, pos, entity) -> entity.fireImmune())
                    .hasPostProcess((state, level, pos) -> true)
                    .emissiveRendering((state, level, pos) -> true)
    ));
    public static final DeferredBlock<Block> CREATIVE_HOT_TEMPERATURE_SOURCE
            = BLOCKS.register("creative_low_temperature_source", CreativeTemperatureSourceBlock::new);
    public static final DeferredBlock<Block> CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE
            = BLOCKS.register("creative_high_temperature_source", CreativeTemperatureSourceBlock::new);
    public static final DeferredBlock<Block> CREATIVE_CHILLED_TEMPERATURE_SOURCE
            = BLOCKS.register("creative_subzero_temperature_source", CreativeTemperatureSourceBlock::new);

    // Misc resource blocks
    public static final DeferredBlock<Block> CAST_IRON_BLOCK
            = BLOCKS.registerBlock("cast_iron_block", Block::new, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5F, 6F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> DUST_BLOCK = BLOCKS.registerBlock("dust", SimpleFallingBlock::new,
            dustBlockProperties());

    public static final DeferredBlock<Block> CRUSHED_NETHERRACK = BLOCKS.registerBlock("crushed_netherrack", SimpleFallingBlock::new,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(0.35F).sound(SoundType.NETHERRACK));
    public static final DeferredBlock<Block> CRUSHED_BASALT = BLOCKS.registerBlock("crushed_basalt", SimpleFallingBlock::new,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(DyeColor.BLACK).requiresCorrectToolForDrops().strength(0.8F, 2.75F).sound(SoundType.BASALT));
    public static final DeferredBlock<Block> CRUSHED_ENDSTONE = BLOCKS.registerBlock("crushed_endstone", SimpleFallingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F));

    // Barrels
    public static final DeferredBlock<Block> WHITE_BARREL = BLOCKS.register("white_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> GREEN_BARREL = BLOCKS.register("green_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> BLUE_BARREL = BLOCKS.register("blue_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> PURPLE_BARREL = BLOCKS.register("purple_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> RED_BARREL = BLOCKS.register("red_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> BLACK_BARREL = BLOCKS.register("black_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> GOLDEN_BARREL = BLOCKS.register("golden_barrel", BarrelBlock::new);

    public static final List<DeferredBlock<Block>> BARRELS = List.of(
            WHITE_BARREL, GREEN_BARREL, BLUE_BARREL, PURPLE_BARREL,
            RED_BARREL, BLACK_BARREL, GOLDEN_BARREL
    );

    // Crates
    public static final DeferredBlock<Block> SMALL_CRATE = BLOCKS.register("small_crate", SmallCrateBlock::new);
    public static final DeferredBlock<Block> CRATE = BLOCKS.register("crate", CrateBlock::new);
    public static final DeferredBlock<Block> PULSATING_CRATE = BLOCKS.register("pulsating_crate", CrateBlock::new);

    public static final List<DeferredBlock<Block>> CRATES = List.of(SMALL_CRATE, CRATE, PULSATING_CRATE);

    // Water Strainers
    private static final List<DeferredBlock<WaterStrainerBlock>> WATER_STRAINERS = new ArrayList<>();

    public static final DeferredBlock<WaterStrainerBlock> ACACIA_STRAINER = registerStrainer(WoodType.ACACIA);
    public static final DeferredBlock<WaterStrainerBlock> BAMBOO_STRAINER = registerStrainer(WoodType.BAMBOO);
    public static final DeferredBlock<WaterStrainerBlock> BIRCH_STRAINER = registerStrainer(WoodType.BIRCH);
    public static final DeferredBlock<WaterStrainerBlock> CHERRY_STRAINER = registerStrainer(WoodType.CHERRY);
    public static final DeferredBlock<WaterStrainerBlock> CRIMSON_STRAINER = registerStrainer(WoodType.CRIMSON);
    public static final DeferredBlock<WaterStrainerBlock> DARK_OAK_STRAINER = registerStrainer(WoodType.DARK_OAK);
    public static final DeferredBlock<WaterStrainerBlock> JUNGLE_STRAINER = registerStrainer(WoodType.JUNGLE);
    public static final DeferredBlock<WaterStrainerBlock> MANGROVE_STRAINER = registerStrainer(WoodType.MANGROVE);
    public static final DeferredBlock<WaterStrainerBlock> OAK_STRAINER = registerStrainer(WoodType.OAK);
    public static final DeferredBlock<WaterStrainerBlock> SPRUCE_STRAINER = registerStrainer(WoodType.SPRUCE);
    public static final DeferredBlock<WaterStrainerBlock> WARPED_STRAINER = registerStrainer(WoodType.WARPED);

    // Compressed blocks
    private static final List<DeferredBlock<Block>> ALL_COMPRESSED = new ArrayList<>();
    private static final Map<String, List<DeferredBlock<Block>>> COMPRESSED_BY_NAME = new HashMap<>();
    private static final Map<String, String> COMPRESSED_XLATE = new HashMap<>();

    private static final List<DeferredBlock<Block>> COMPRESSED_BASALTS
            = registerCompressed("basalt", "Basalt", BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT),
            1.25f, 3, RotatedPillarBlock::new);
    private static final List<DeferredBlock<Block>> COMPRESSED_CLAYS
            = registerCompressed("clay", "Clay", Blocks.CLAY, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_COBBLESTONES
            = registerCompressed("cobblestone", "Cobblestone", Blocks.COBBLESTONE, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_DIRTS
            = registerCompressed("dirt", "Dirt", Blocks.DIRT, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_DUSTS
            = registerCompressed("dust", "Dust", dustBlockProperties(), 0.5F, 3, SimpleFallingBlock::new);
    private static final List<DeferredBlock<Block>> COMPRESSED_END_STONES
            = registerCompressed("end_stone", "End Stone",Blocks.END_STONE, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_GRAVELS
            = registerCompressed("gravel", "Gravel", BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL),
            0.6f, 3, properties -> new ColoredFallingBlock(new ColorRGBA(0x807C7B), properties));
    private static final List<DeferredBlock<Block>> COMPRESSED_NETHERRACKS
            = registerCompressed("netherrack", "Netherrack", Blocks.NETHERRACK, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_RED_SANDS
            = registerCompressed("red_sand", "Red Sand", BlockBehaviour.Properties.ofFullCopy(Blocks.RED_SAND),
            0.5f, 3, properties -> new ColoredFallingBlock(new ColorRGBA(0xA95821), properties));
    private static final List<DeferredBlock<Block>> COMPRESSED_SANDS
            = registerCompressed("sand", "Sand", BlockBehaviour.Properties.ofFullCopy(Blocks.SAND),
            0.5f, 3, properties -> new ColoredFallingBlock(new ColorRGBA(0xDBD3A0), properties));
    private static final List<DeferredBlock<Block>> COMPRESSED_STONES
            = registerCompressed("stone", "Stone", Blocks.STONE, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_SOUL_SANDS
            = registerCompressed("soul_sand", "Soul Sand", Blocks.SOUL_SAND, 3);
    private static final List<DeferredBlock<Block>> COMPRESSED_SOUL_SOILS
            = registerCompressed("soul_soil", "Soul Soil", Blocks.SOUL_SOIL, 3);

    //----------------------------------

    public static Collection<DeferredBlock<WaterStrainerBlock>> waterStrainers() {
        return Collections.unmodifiableCollection(WATER_STRAINERS);
    }

    public static Collection<DeferredBlock<Block>> allCompressedBlocks() {
        return Collections.unmodifiableCollection(ALL_COMPRESSED);
    }

    public static Map<String,String> compressedBlockTranslations() {
        return Collections.unmodifiableMap(COMPRESSED_XLATE);
    }

    public static List<DeferredBlock<Block>> compressedBlocks(String name) {
        return COMPRESSED_BY_NAME.get(name);
    }

    private static DeferredBlock<WaterStrainerBlock> registerStrainer(WoodType type) {
        var block = BLOCKS.registerBlock(type.name() + "_water_strainer",
                props -> new WaterStrainerBlock(props, type), WaterStrainerBlock.defaultProps());
        WATER_STRAINERS.add(block);
        return block;
    }

    private static List<DeferredBlock<Block>> registerCompressed(String baseName, String label, BlockBehaviour.Properties props,
                                                                 float baseDestroyTime, int maxLevel, Function<BlockBehaviour.Properties, Block> factory) {
        Validate.isTrue(maxLevel > 0);

        ImmutableList.Builder<DeferredBlock<Block>> blocks = ImmutableList.builder();
        for (int level = 1; level <= maxLevel; level++) {
            String name = String.format("compressed_%s%s", baseName, level > 1 ? "_" + level : "");
            DeferredBlock<Block> deferredBlock = BLOCKS.registerBlock(name, factory, props.destroyTime(baseDestroyTime + level));
            ALL_COMPRESSED.add(deferredBlock);
            blocks.add(deferredBlock);
        }
        ImmutableList<DeferredBlock<Block>> result = blocks.build();
        COMPRESSED_BY_NAME.put(baseName, result);
        COMPRESSED_XLATE.put(baseName, label);
        return result;
    }

    private static List<DeferredBlock<Block>> registerCompressed(String baseName, String label, Block baseBlock, int maxLevel) {
        return registerCompressed(baseName, label, BlockBehaviour.Properties.ofFullCopy(baseBlock), baseBlock.defaultDestroyTime(),
                maxLevel, Block::new);
    }

    private static BlockBehaviour.@NotNull Properties dustBlockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).strength(0.4F).sound(SoundType.SAND);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
