package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobbleGenProperties;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.SimpleFallingBlock;
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
import dev.ftb.mods.ftbstuffnthings.blocks.supercooler.SuperCoolerBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.tube.TubeBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class BlocksRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBStuffNThings.MODID);

    public static final DeferredBlock<SluiceBlock> OAK_SLUICE
            = BLOCKS.register("oak_sluice", () -> new SluiceBlock(SluiceType.OAK));
    public static final DeferredBlock<SluiceBlock> IRON_SLUICE
            = BLOCKS.register("iron_sluice", () -> new SluiceBlock(SluiceType.IRON));
    public static final DeferredBlock<SluiceBlock> DIAMOND_SLUICE
            = BLOCKS.register("diamond_sluice", () -> new SluiceBlock(SluiceType.DIAMOND));
    public static final DeferredBlock<SluiceBlock> NETHERITE_SLUICE
            = BLOCKS.register("netherite_sluice", () -> new SluiceBlock(SluiceType.NETHERITE));
    public static List<DeferredBlock<SluiceBlock>> ALL_SLUICES = List.of(OAK_SLUICE, IRON_SLUICE, DIAMOND_SLUICE, NETHERITE_SLUICE);

    public static final DeferredBlock<PumpBlock> PUMP
            = BLOCKS.register("pump", PumpBlock::new);

    public static final DeferredBlock<DripperBlock> DRIPPER
            = BLOCKS.register("dripper", DripperBlock::new);


    public static final DeferredBlock<AutoHammerBlock> IRON_AUTO_HAMMER
            = BLOCKS.register("iron_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.IRON));
    public static final DeferredBlock<AutoHammerBlock> GOLD_AUTO_HAMMER
            = BLOCKS.register("gold_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.GOLD));
    public static final DeferredBlock<AutoHammerBlock> DIAMOND_AUTO_HAMMER
            = BLOCKS.register("diamond_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.DIAMOND));
    public static final DeferredBlock<AutoHammerBlock> NETHERITE_AUTO_HAMMER
            = BLOCKS.register("netherite_auto_hammer", () -> new AutoHammerBlock(AutoHammerProperties.NETHERITE));
    public static final List<DeferredBlock<AutoHammerBlock>> ALL_AUTO_HAMMERS = List.of(IRON_AUTO_HAMMER, GOLD_AUTO_HAMMER, DIAMOND_AUTO_HAMMER, NETHERITE_AUTO_HAMMER);


    public static final DeferredBlock<CobblegenBlock> STONE_COBBLESTONE_GENERATOR
            = BLOCKS.register("stone_cobblestone_generator", () -> new CobblegenBlock(CobbleGenProperties.STONE));
    public static final DeferredBlock<CobblegenBlock> IRON_COBBLESTONE_GENERATOR
            = BLOCKS.register("iron_cobblestone_generator", () -> new CobblegenBlock(CobbleGenProperties.IRON));
    public static final DeferredBlock<CobblegenBlock> GOLD_COBBLESTONE_GENERATOR
            = BLOCKS.register("gold_cobblestone_generator", () -> new CobblegenBlock(CobbleGenProperties.GOLD));
    public static final DeferredBlock<CobblegenBlock> DIAMOND_COBBLESTONE_GENERATOR
            = BLOCKS.register("diamond_cobblestone_generator", () -> new CobblegenBlock(CobbleGenProperties.DIAMOND));
    public static final DeferredBlock<CobblegenBlock> NETHERITE_COBBLESTONE_GENERATOR
            = BLOCKS.register("netherite_cobblestone_generator", () -> new CobblegenBlock(CobbleGenProperties.NETHERITE));

    public static final List<DeferredBlock<CobblegenBlock>> COBBLEGENS = List.of(STONE_COBBLESTONE_GENERATOR, IRON_COBBLESTONE_GENERATOR, GOLD_COBBLESTONE_GENERATOR, DIAMOND_COBBLESTONE_GENERATOR, NETHERITE_COBBLESTONE_GENERATOR);

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

    public static final DeferredBlock<Block> CAST_IRON_BLOCK
            = BLOCKS.registerBlock("cast_iron_block", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5F, 6F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DUST_BLOCK = BLOCKS.registerBlock("dust", SimpleFallingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).strength(0.4F).sound(SoundType.SAND));
    public static final DeferredBlock<Block> CRUSHED_NETHERRACK = BLOCKS.registerBlock("crushed_netherrack", SimpleFallingBlock::new,
           BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(0.35F).sound(SoundType.NETHERRACK));
    public static final DeferredBlock<Block> CRUSHED_BASALT = BLOCKS.registerBlock("crushed_basalt", SimpleFallingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(DyeColor.BLACK).requiresCorrectToolForDrops().strength(0.8F, 2.75F).sound(SoundType.BASALT));
    public static final DeferredBlock<Block> CRUSHED_ENDSTONE = BLOCKS.registerBlock("crushed_endstone", SimpleFallingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(2.0F, 6.0F));

    public static final DeferredBlock<Block> WHITE_BARREL = BLOCKS.register("white_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> GREEN_BARREL = BLOCKS.register("green_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> BLUE_BARREL = BLOCKS.register("blue_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> PURPLE_BARREL = BLOCKS.register("purple_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> RED_BARREL = BLOCKS.register("red_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> BLACK_BARREL = BLOCKS.register("black_barrel", BarrelBlock::new);
    public static final DeferredBlock<Block> GOLDEN_BARREL = BLOCKS.register("golden_barrel", BarrelBlock::new);

    public static final List<DeferredBlock<Block>> BARRELS = List.of(WHITE_BARREL, GREEN_BARREL, BLUE_BARREL, PURPLE_BARREL, RED_BARREL, BLACK_BARREL, GOLDEN_BARREL);

    public static final DeferredBlock<Block> SMALL_CRATE = BLOCKS.register("small_crate", SmallCrateBlock::new);
    public static final DeferredBlock<Block> CRATE = BLOCKS.register("crate", CrateBlock::new);
    public static final DeferredBlock<Block> PULSATING_CRATE = BLOCKS.register("pulsating_crate", CrateBlock::new);

    public static final List<DeferredBlock<Block>> CRATES = List.of(SMALL_CRATE, CRATE, PULSATING_CRATE);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
