package dev.ftb.mods.ftbstuffnthings.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record EnergyRequirement(int fePerTick, int ticksToProcess) {
    public static final Codec<EnergyRequirement> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("fe_per_tick").forGetter(EnergyRequirement::fePerTick),
            ExtraCodecs.POSITIVE_INT.fieldOf("ticks_to_process").forGetter(EnergyRequirement::ticksToProcess)
    ).apply(builder, EnergyRequirement::new));

    public static final StreamCodec<FriendlyByteBuf, EnergyRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EnergyRequirement::fePerTick,
            ByteBufCodecs.VAR_INT, EnergyRequirement::ticksToProcess,
            EnergyRequirement::new
    );
}
