package dev.ftb.mods.ftbstuffnthings.registry;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.SimpleFallingBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.BasaltgenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.IResourceGenProps;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.ResourceGeneratorBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.dripper.DripperBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerType;
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
import dev.ftb.mods.ftbstuffnthings.blocks.woodbasin.WoodenBasinBlock;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Function;

public class BlocksRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBStuffNThings.MOD_ID);

    // Sluices
    private static final List<DeferredBlock<SluiceBlock>> WOODEN_SLUICES = new ArrayList<>();
    private static final EnumMap<SluiceType, DeferredBlock<SluiceBlock>> SLUICES
            = Util.make(new EnumMap<>(SluiceType.class), map -> {
                for (var type : SluiceType.values()) {
                    map.put(type, BLOCKS.registerBlock(type.getSerializedName() + "_sluice",
                            p -> new SluiceBlock(p, type)));
                    if (type.isWood) {
                        WOODEN_SLUICES.add(map.get(type));
                    }
                }
            }
    );

    // Autohammers
    private static final EnumMap<AutoHammerType, DeferredBlock<AutoHammerBlock>> AUTO_HAMMERS
            = Util.make(new EnumMap<>(AutoHammerType.class), map -> {
                for (var type : AutoHammerType.values()) {
                    map.put(type, BLOCKS.registerBlock(type.getMaterialId() + "_auto_hammer",
                            p -> new AutoHammerBlock(p, type)));
                }
            }
    );

    // Cobble & Basalt generators
    private static final EnumMap<CobblegenProperties, DeferredBlock<ResourceGeneratorBlock>> COBBLE_GENS
            = registerGenerators(CobblegenProperties.class);
    private static final EnumMap<BasaltgenProperties, DeferredBlock<ResourceGeneratorBlock>> BASALT_GENS
            = registerGenerators(BasaltgenProperties.class);

    // Misc machines
    public static final DeferredBlock<PumpBlock> PUMP
            = BLOCKS.registerBlock("pump", PumpBlock::new);
    public static final DeferredBlock<DripperBlock> DRIPPER
            = BLOCKS.registerBlock("dripper", DripperBlock::new);
    public static final DeferredBlock<WoodenBasinBlock> WOODEN_BASIN
            = BLOCKS.registerBlock("wooden_basin", WoodenBasinBlock::new);
    public static final DeferredBlock<FusingMachineBlock> FUSING_MACHINE
            = BLOCKS.registerBlock("fusing_machine", FusingMachineBlock::new);
    public static final DeferredBlock<SuperCoolerBlock> SUPER_COOLER
            = BLOCKS.registerBlock("super_cooler", SuperCoolerBlock::new);
    public static final DeferredBlock<JarBlock> JAR
            = BLOCKS.registerBlock("jar", JarBlock::new);
    public static final DeferredBlock<TemperedJarBlock> TEMPERED_JAR
            = BLOCKS.registerBlock("tempered_jar", TemperedJarBlock::new);
    public static final DeferredBlock<JarAutomaterBlock> JAR_AUTOMATER
            = BLOCKS.registerBlock("auto_processing_block", JarAutomaterBlock::new);
    public static final DeferredBlock<Block> BLUE_MAGMA_BLOCK
            = BLOCKS.registerBlock("blue_magma_block", MagmaBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.NETHER)
                    .requiresCorrectToolForDrops()
                    .lightLevel(_ -> 3)
                    .randomTicks()
                    .strength(0.5F)
                    .isValidSpawn((_, _, _, entity) -> entity.fireImmune())
                    .postProcess((_, _, pos) -> pos.above())
                    .emissiveRendering((_, _, _) -> true)
    );

    public static final DeferredBlock<Block> CREATIVE_HOT_TEMPERATURE_SOURCE
            = BLOCKS.registerBlock("creative_low_temperature_source", CreativeTemperatureSourceBlock::new);
    public static final DeferredBlock<Block> CREATIVE_SUPERHEATED_TEMPERATURE_SOURCE
            = BLOCKS.registerBlock("creative_high_temperature_source", CreativeTemperatureSourceBlock::new);
    public static final DeferredBlock<Block> CREATIVE_CHILLED_TEMPERATURE_SOURCE
            = BLOCKS.registerBlock("creative_subzero_temperature_source", CreativeTemperatureSourceBlock::new);

    // Misc resource blocks
    public static final DeferredBlock<Block> CAST_IRON_BLOCK
            = BLOCKS.registerBlock("cast_iron_block", Block::new, () -> BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5F, 6F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> DUST_BLOCK
            = BLOCKS.registerBlock("dust", SimpleFallingBlock::new, BlocksRegistry::dustBlockProperties);

    public static final DeferredBlock<Block> CRUSHED_NETHERRACK
            = BLOCKS.registerBlock("crushed_netherrack", SimpleFallingBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(0.35F).sound(SoundType.NETHERRACK));
    public static final DeferredBlock<Block> CRUSHED_BASALT
            = BLOCKS.registerBlock("crushed_basalt", SimpleFallingBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(DyeColor.BLACK).requiresCorrectToolForDrops().strength(0.8F, 2.75F).sound(SoundType.BASALT));
    public static final DeferredBlock<Block> CRUSHED_ENDSTONE
            = BLOCKS.registerBlock("crushed_endstone", SimpleFallingBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F));

    // Barrels
    private static final List<DeferredBlock<BarrelBlock>> BARRELS = new ArrayList<>();
    static {
        List.of("white", "green", "blue", "purple", "red", "black", "golden")
                .forEach(color -> BARRELS.add(BLOCKS.registerBlock(color + "_barrel", BarrelBlock::new)));
    }

    // Crates
    public static final DeferredBlock<Block> SMALL_CRATE = BLOCKS.registerBlock("small_crate", SmallCrateBlock::new);
    public static final DeferredBlock<Block> CRATE = BLOCKS.registerBlock("crate", CrateBlock::new);
    public static final DeferredBlock<Block> PULSATING_CRATE = BLOCKS.registerBlock("pulsating_crate", CrateBlock::new);

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

    public static Collection<DeferredBlock<SluiceBlock>> allSluices() {
        return Collections.unmodifiableCollection(SLUICES.values());
    }

    public static Collection<DeferredBlock<SluiceBlock>> woodenSluices() {
        return Collections.unmodifiableCollection(WOODEN_SLUICES);
    }

    public static DeferredBlock<SluiceBlock> getSluice(SluiceType type) {
        return SLUICES.get(type);
    }

    public static Collection<DeferredBlock<WaterStrainerBlock>> allWaterStrainers() {
        return Collections.unmodifiableCollection(WATER_STRAINERS);
    }

    public static Collection<DeferredBlock<AutoHammerBlock>> allAutoHammers() {
        return Collections.unmodifiableCollection(AUTO_HAMMERS.values());
    }

    public static DeferredBlock<AutoHammerBlock> getAutoHammer(AutoHammerType autoHammerType) {
        return AUTO_HAMMERS.get(autoHammerType);
    }

    public static Collection<DeferredBlock<ResourceGeneratorBlock>> allCobbleGenerators() {
        return Collections.unmodifiableCollection(COBBLE_GENS.values());
    }

    public static DeferredBlock<ResourceGeneratorBlock> getCobbleGenerator(CobblegenProperties type) {
        return COBBLE_GENS.get(type);
    }

    public static Collection<DeferredBlock<ResourceGeneratorBlock>> allBasaltGenerators() {
        return Collections.unmodifiableCollection(BASALT_GENS.values());
    }

    public static DeferredBlock<ResourceGeneratorBlock> getBasaltGenerator(BasaltgenProperties type) {
        return BASALT_GENS.get(type);
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

    public static Collection<DeferredBlock<BarrelBlock>> allBarrels() {
        return Collections.unmodifiableList(BARRELS);
    }

    //----------------------------------

    private static DeferredBlock<WaterStrainerBlock> registerStrainer(WoodType type) {
        var block = BLOCKS.registerBlock(type.name() + "_water_strainer",
                props -> new WaterStrainerBlock(props, type), WaterStrainerBlock::defaultProps);
        WATER_STRAINERS.add(block);
        return block;
    }

    private static <T extends Enum<T> & IResourceGenProps> EnumMap<T, DeferredBlock<ResourceGeneratorBlock>> registerGenerators(Class<T> cls) {
        EnumMap<T, DeferredBlock<ResourceGeneratorBlock>> map = new EnumMap<>(cls);
        for (var type : cls.getEnumConstants()) {
            map.put(type, BLOCKS.registerBlock(type.getBlockId(), p -> new ResourceGeneratorBlock(p, type)));
        }
        return map;
    }

    private static List<DeferredBlock<Block>> registerCompressed(String baseName, String label, BlockBehaviour.Properties props,
                                                                 float baseDestroyTime, int maxLevel, Function<BlockBehaviour.Properties, Block> factory) {
        Validate.isTrue(maxLevel > 0);

        ImmutableList.Builder<DeferredBlock<Block>> blocks = ImmutableList.builder();
        for (int level = 1; level <= maxLevel; level++) {
            String name = String.format("compressed_%s%s", baseName, level > 1 ? "_" + level : "");
            final float destroyTime = baseDestroyTime + level;
            DeferredBlock<Block> deferredBlock = BLOCKS.registerBlock(name, factory, () -> props.destroyTime(destroyTime));
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

    private static BlockBehaviour.Properties dustBlockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).strength(0.4F).sound(SoundType.SAND);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
