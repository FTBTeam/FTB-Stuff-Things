package dev.ftb.mods.ftbstuffnthings.network;

import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Received on: CLIENT<br>
 * Sent by server (not more than once every 10 ticks) when items or fluids in the jar have changed.
 *
 * @param jarPos jar blockpos
 * @param resources list of resources to sync; a slot with an item or fluid stack
 */
public record SyncJarContentsPacket(BlockPos jarPos, List<ResourceSlot> resources) implements CustomPacketPayload {
    public static final Type<SyncJarContentsPacket> TYPE = new Type<>(FTBStuffNThings.id("sync_jar_fluids"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJarContentsPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncJarContentsPacket::jarPos,
            ResourceSlot.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncJarContentsPacket::resources,
            SyncJarContentsPacket::new
    );

    public static SyncJarContentsPacket wholeJar(TemperedJarBlockEntity jar) {
        List<ResourceSlot> resources = new ArrayList<>();
        for (int i = 0; i < jar.getInputItemHandler().size(); i++) {
            var resource = jar.getInputItemHandler().getResource(i);
            if (!resource.isEmpty()) {
                resources.add(new ResourceSlot(i, Either.left(resource), jar.getInputItemHandler().getAmountAsInt(i)));
            }
        }
        for (int i = 0; i < jar.getFluidHandler().size(); i++) {
            FluidResource resource = jar.getFluidHandler().getResource(i);
            if (!resource.isEmpty()) {
                resources.add(new ResourceSlot(i, Either.right(resource), jar.getFluidHandler().getAmountAsInt(i)));
            }
        }

        return new SyncJarContentsPacket(jar.getBlockPos(), resources);
    }

    public static SyncJarContentsPacket oneItem(BlockPos pos, int slot, ItemStack stack) {
        return new SyncJarContentsPacket(pos, List.of(new ResourceSlot(slot, Either.left(ItemResource.of(stack)), stack.count())));
    }

    public static SyncJarContentsPacket oneFluid(BlockPos pos, int slot, FluidStack stack) {
        return new SyncJarContentsPacket(pos, List.of(new ResourceSlot(slot, Either.right(FluidResource.of(stack)), stack.amount())));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncJarContentsPacket packet, IPayloadContext context) {
        if (context.player().level().getBlockEntity(packet.jarPos) instanceof TemperedJarBlockEntity jar) {
            jar.syncFromServer(packet.resources);
        }
    }

    public record ResourceSlot(int slot, Either<ItemResource, FluidResource> resource, int amount) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ResourceSlot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ResourceSlot::slot,
                ByteBufCodecs.either(ItemResource.STREAM_CODEC, FluidResource.STREAM_CODEC),ResourceSlot::resource,
                ByteBufCodecs.VAR_INT, ResourceSlot::amount,
                ResourceSlot::new
        );
    }
}
