package dev.ftb.mods.ftbstuffnthings.network;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.ClientUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDisplayFluidPacket(BlockPos pos, FluidStack fluidStack) implements CustomPacketPayload {
    public static final Type<SyncDisplayFluidPacket> TYPE = new Type<>(FTBStuffNThings.id("display_fluid_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDisplayFluidPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDisplayFluidPacket::pos,
            FluidStack.OPTIONAL_STREAM_CODEC, SyncDisplayFluidPacket::fluidStack,
            SyncDisplayFluidPacket::new
    );

    public static void handleData(SyncDisplayFluidPacket packet, IPayloadContext ignoredCtx) {
        ClientUtil.getBlockEntityAt(packet.pos, AbstractMachineBlockEntity.class)
                .ifPresent(holder -> holder.syncFluidFromServer(packet.fluidStack));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
