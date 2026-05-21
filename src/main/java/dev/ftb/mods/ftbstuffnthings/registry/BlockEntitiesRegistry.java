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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBlockEntity>> SLUICE
            = registerBlockSet("sluice", SluiceBlockEntity::new,
            () -> BlocksRegistry.allSluices().stream().map(DeferredHolder::get).collect(Collectors.toSet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoHammerBlockEntity>> AUTO_HAMMER
            = registerBlockSet("auto_hammer", AutoHammerBlockEntity::new,
            () -> BlocksRegistry.allAutoHammers().stream().map(DeferredHolder::get).collect(Collectors.toSet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblegenBlockEntity>> COBBLE_GENERATOR
            = registerBlockSet("cobblestone_generator", CobblegenBlockEntity::new,
            () -> BlocksRegistry.allCobbleGenerators().stream().map(DeferredHolder::get).collect(Collectors.toSet()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasaltgenBlockEntity>> BASALT_GENERATOR
            = registerBlockSet("basalt_generator", BasaltgenBlockEntity::new,
            () -> BlocksRegistry.allBasaltGenerators().stream().map(DeferredHolder::get).collect(Collectors.toSet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP
            = register("pump", PumpBlockEntity::new, BlocksRegistry.PUMP);

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
            = registerBlockSet("water_strainer", WaterStrainerBlockEntity::new,
            () -> BlocksRegistry.allWaterStrainers().stream().map(DeferredHolder::get).collect(Collectors.toSet()));

    //-------------------------------

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
