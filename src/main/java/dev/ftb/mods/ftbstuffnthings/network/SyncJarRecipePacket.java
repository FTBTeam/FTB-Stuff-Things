package dev.ftb.mods.ftbstuffnthings.network;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * Received on: CLIENT<br>
 * Sent by server to update the GUI when the current recipe changes.
 *
 * @param pos jar blockpos
 * @param recipeId the new recipe ID
 */
public record SyncJarRecipePacket(BlockPos pos, Optional<Identifier> recipeId) implements CustomPacketPayload {
    public static final Type<SyncJarRecipePacket> TYPE = new Type<>(FTBStuffNThings.id("sync_jar_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncJarRecipePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncJarRecipePacket::pos,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), SyncJarRecipePacket::recipeId,
            SyncJarRecipePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(SyncJarRecipePacket packet, IPayloadContext context) {
        if (context.flow().isServerbound() && !(context.player().containerMenu instanceof TemperedJarMenu menu && menu.getJar().getBlockPos().equals(packet.pos))) {
            // security check, ensure player has this jar open right now
            return;
        }

        if (context.player().level().getBlockEntity(packet.pos) instanceof TemperedJarBlockEntity jar) {
            jar.setCurrentRecipeId(packet.recipeId.orElse(null));
        }
    }
}
