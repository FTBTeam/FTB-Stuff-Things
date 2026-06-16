package dev.ftb.mods.ftbstuffnthings.util;

import dev.ftb.mods.ftbstuffnthings.integration.sable.SableMenuCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SubLevelMenuHelper {
    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    private SubLevelMenuHelper() {
    }

    public static void writeLocator(FriendlyByteBuf buf, BlockEntity blockEntity) {
        buf.writeBlockPos(blockEntity.getBlockPos());
        UUID subLevelUuid = SABLE_LOADED ? SableMenuCompat.subLevelUuid(blockEntity) : null;
        buf.writeBoolean(subLevelUuid != null);
        if (subLevelUuid != null) {
            buf.writeUUID(subLevelUuid);
        }
    }

    @Nullable
    public static BlockEntity readAndResolve(Player player, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        UUID subLevelUuid = buf.readBoolean() ? buf.readUUID() : null;
        if (subLevelUuid != null && SABLE_LOADED) {
            BlockEntity onSubLevel = SableMenuCompat.resolve(player.level(), subLevelUuid, pos);
            if (onSubLevel != null) {
                return onSubLevel;
            }
        }
        return player.level().getBlockEntity(pos);
    }
}
