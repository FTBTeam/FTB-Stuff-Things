package dev.ftb.mods.ftbstuffnthings.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ComponentsRegistry {
    private static final DeferredRegister.DataComponents COMPONENTS
            = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, FTBStuffNThings.MODID);

    // capsules and machines with one fluid tank
    public static final Supplier<DataComponentType<SimpleFluidContent>> STORED_FLUID
            = register("stored_fluid", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);

    // tempered jars (0 or more tanks)
    public static final Supplier<DataComponentType<List<SimpleFluidContent>>> FLUID_TANKS
            = register("fluid_tanks", SimpleFluidContent.CODEC.listOf(), SimpleFluidContent.STREAM_CODEC.apply(ByteBufCodecs.list()));

    public static final Supplier<DataComponentType<Integer>> STORED_ENERGY
            = register("energy", Codec.INT, ByteBufCodecs.INT);

    public static void init(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }

    private static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return COMPONENTS.registerComponentType(name, builder -> builder
                .persistent(codec)
                .networkSynchronized(streamCodec)
        );
    }
}
