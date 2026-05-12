package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.BasaltgenBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.cobblegen.CobblegenBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.dripper.DripperBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.JarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.supercooler.SuperCoolerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.tube.TubeBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.woodbasin.WoodenBasinBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockEntitiesRegistry {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBStuffNThings.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Oak>> OAK_SLUICE
            = register("oak_sluice", SluiceBlockEntity.Oak::new, BlocksRegistry.OAK_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Spruce>> SPRUCE_SLUICE
            = register("spruce_sluice", SluiceBlockEntity.Spruce::new, BlocksRegistry.SPRUCE_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Birch>> BIRCH_SLUICE
            = register("birch_sluice", SluiceBlockEntity.Birch::new, BlocksRegistry.BIRCH_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Jungle>> JUNGLE_SLUICE
            = register("jungle_sluice", SluiceBlockEntity.Jungle::new, BlocksRegistry.JUNGLE_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Acacia>> ACACIA_SLUICE
            = register("acacia_sluice", SluiceBlockEntity.Acacia::new, BlocksRegistry.ACACIA_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.DarkOak>> DARK_OAK_SLUICE
            = register("dark_oak_sluice", SluiceBlockEntity.DarkOak::new, BlocksRegistry.DARK_OAK_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Mangrove>> MANGROVE_SLUICE
            = register("mangrove_sluice", SluiceBlockEntity.Mangrove::new, BlocksRegistry.MANGROVE_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Cherry>> CHERRY_SLUICE
            = register("cherry_sluice", SluiceBlockEntity.Cherry::new, BlocksRegistry.CHERRY_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.PaleOak>> PALE_OAK_SLUICE
            = register("pale_oak_sluice", SluiceBlockEntity.PaleOak::new, BlocksRegistry.PALE_OAK_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Crimson>> CRIMSON_SLUICE
            = register("crimson_sluice", SluiceBlockEntity.Crimson::new, BlocksRegistry.CRIMSON_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Warped>> WARPED_SLUICE
            = register("warped_sluice", SluiceBlockEntity.Warped::new, BlocksRegistry.WARPED_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Bamboo>> BAMBOO_SLUICE
            = register("bamboo_sluice", SluiceBlockEntity.Bamboo::new, BlocksRegistry.BAMBOO_SLUICE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Iron>> IRON_SLUICE
            = register("iron_sluice", SluiceBlockEntity.Iron::new, BlocksRegistry.IRON_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Diamond>> DIAMOND_SLUICE
            = register("diamond_sluice", SluiceBlockEntity.Diamond::new, BlocksRegistry.DIAMOND_SLUICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity.Netherite>> NETHERITE_SLUICE
            = register("netherite_sluice", SluiceBlockEntity.Netherite::new, BlocksRegistry.NETHERITE_SLUICE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoHammerBlockEntity.Iron>> IRON_HAMMER
            = register("iron_hammer", AutoHammerBlockEntity.Iron::new, BlocksRegistry.IRON_AUTO_HAMMER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoHammerBlockEntity.Gold>> GOLD_HAMMER
            = register("gold_hammer", AutoHammerBlockEntity.Gold::new, BlocksRegistry.GOLD_AUTO_HAMMER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoHammerBlockEntity.Diamond>> DIAMOND_HAMMER
            = register("diamond_hammer", AutoHammerBlockEntity.Diamond::new, BlocksRegistry.DIAMOND_AUTO_HAMMER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoHammerBlockEntity.Netherite>> NETHERITE_HAMMER
            = register("netherite_hammer", AutoHammerBlockEntity.Netherite::new, BlocksRegistry.NETHERITE_AUTO_HAMMER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity.Stone>> STONE_COBBLEGEN
            = register("stone_cobblegen", CobblegenBlockEntity.Stone::new, BlocksRegistry.STONE_COBBLESTONE_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity.Iron>> IRON_COBBLEGEN
            = register("iron_cobblegen", CobblegenBlockEntity.Iron::new, BlocksRegistry.IRON_COBBLESTONE_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity.Gold>> GOLD_COBBLEGEN
            = register("gold_cobblegen", CobblegenBlockEntity.Gold::new, BlocksRegistry.GOLD_COBBLESTONE_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity.Diamond>> DIAMOND_COBBLEGEN
            = register("diamond_cobblegen", CobblegenBlockEntity.Diamond::new, BlocksRegistry.DIAMOND_COBBLESTONE_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity.Netherite>> NETHERITE_COBBLEGEN
            = register("netherite_cobblegen", CobblegenBlockEntity.Netherite::new, BlocksRegistry.NETHERITE_COBBLESTONE_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity.Stone>> STONE_BASALT_GENERATOR
            = register("stone_basalt_generator", BasaltgenBlockEntity.Stone::new, BlocksRegistry.STONE_BASALT_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity.Iron>> IRON_BASALT_GENERATOR
            = register("iron_basalt_generator", BasaltgenBlockEntity.Iron::new, BlocksRegistry.IRON_BASALT_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity.Gold>> GOLD_BASALT_GENERATOR
            = register("gold_basalt_generator", BasaltgenBlockEntity.Gold::new, BlocksRegistry.GOLD_BASALT_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity.Diamond>> DIAMOND_BASALT_GENERATOR
            = register("diamond_basalt_generator", BasaltgenBlockEntity.Diamond::new, BlocksRegistry.DIAMOND_BASALT_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity.Netherite>> NETHERITE_BASALT_GENERATOR
            = register("netherite_basalt_generator", BasaltgenBlockEntity.Netherite::new, BlocksRegistry.NETHERITE_BASALT_GENERATOR);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP
            = register("pump", PumpBlockEntity::new, BlocksRegistry.PUMP);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBlockEntity>> TUBE
            = register("tube", TubeBlockEntity::new, BlocksRegistry.TUBE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarBlockEntity>> JAR
            = register("jar", JarBlockEntity::new, BlocksRegistry.JAR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TemperedJarBlockEntity>> TEMPERED_JAR
            = register("tempered_jar", TemperedJarBlockEntity::new, BlocksRegistry.TEMPERED_JAR);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DripperBlockEntity>> DRIPPER
            = register("dripper", DripperBlockEntity::new, BlocksRegistry.DRIPPER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenBasinBlockEntity>> WOODEN_BASIN
            = register("wooden_basin", WoodenBasinBlockEntity::new, BlocksRegistry.WOODEN_BASIN);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FusingMachineBlockEntity>> FUSING_MACHINE
            = register("fusing_machine", FusingMachineBlockEntity::new, BlocksRegistry.FUSING_MACHINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SuperCoolerBlockEntity>> SUPER_COOLER
            = register("super_cooler", SuperCoolerBlockEntity::new, BlocksRegistry.SUPER_COOLER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterStrainerBlockEntity>> WATER_STRAINER
            = registerBlockSet("water_strainer", WaterStrainerBlockEntity::new, BlockEntitiesRegistry::strainerBlocks);

    private static Set<Block> strainerBlocks() {
        return BlocksRegistry.waterStrainers().stream().map(DeferredHolder::get).collect(Collectors.toSet());
    }

    @SafeVarargs
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... blocks) {
        //noinspection ConstantConditions
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(supplier, Arrays.stream(blocks).map(Supplier::get).collect(Collectors.toSet())));
    }

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockSet(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<Set<Block>> blocks) {
        //noinspection ConstantConditions
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(supplier, blocks.get()));
    }

    public static void init(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
