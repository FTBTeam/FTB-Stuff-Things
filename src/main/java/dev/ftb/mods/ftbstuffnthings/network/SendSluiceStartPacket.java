package dev.ftb.mods.ftbstuffnthings.network;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.ClientUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Received on: CLIENT<br>
 * Sent by server when processing a block starts to sync the processing time (needed by the BER)
 *
 * @param pos sluice blockpos
 * @param processingTime recipe processing time, in ticks
 */
public record SendSluiceStartPacket(BlockPos pos, int processingTime) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendSluiceStartPacket> TYPE = new CustomPacketPayload.Type<>(FTBStuffNThings.id("send_sluice_start"));

    public static final StreamCodec<FriendlyByteBuf, SendSluiceStartPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SendSluiceStartPacket::pos,
            ByteBufCodecs.VAR_INT, SendSluiceStartPacket::processingTime,
            SendSluiceStartPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SendSluiceStartPacket packet, IPayloadContext ignoredContext) {
        ClientUtil.getBlockEntityAt(packet.pos, SluiceBlockEntity.class)
                .ifPresent(holder -> holder.syncProcessingTimeFromServer(packet.processingTime));
    }
}
