package dev.ftb.mods.ftbstuffnthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class ClientUtil {
    public static <T> Optional<T> getBlockEntityAt(BlockPos pos, Class<T> cls) {
        Level level = Minecraft.getInstance().level;
        if (level != null && pos != null && level.isLoaded(pos)) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te != null && cls.isAssignableFrom(te.getClass())) {
                //noinspection unchecked
                return Optional.of((T) te);
            }
        }
        return Optional.empty();
    }
}
