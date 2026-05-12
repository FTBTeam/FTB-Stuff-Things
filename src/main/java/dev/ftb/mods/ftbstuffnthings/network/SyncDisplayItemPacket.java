package dev.ftb.mods.ftbstuffnthings.network;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.client.ClientUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.item.ItemUtil;

public record SyncDisplayItemPacket(BlockPos pos, ItemStack stack) implements CustomPacketPayload {
    public static final Type<SyncDisplayItemPacket> TYPE = new Type<>(FTBStuffNThings.id("sync_display_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDisplayItemPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDisplayItemPacket::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, SyncDisplayItemPacket::stack,
            SyncDisplayItemPacket::new
    );

    public static SyncDisplayItemPacket forSluice(AbstractMachineBlockEntity machine) {
        return new SyncDisplayItemPacket(machine.getBlockPos(), ItemUtil.getStack(machine.getItemHandler(), 0));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncDisplayItemPacket packet, IPayloadContext context) {
        ClientUtil.getBlockEntityAt(packet.pos, AbstractMachineBlockEntity.class)
                .ifPresent(holder -> holder.syncItemFromServer(packet.stack));
    }
}
