package dev.ftb.mods.ftbstuffnthings.integration.sable;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SableMenuCompat {
    private SableMenuCompat() {
    }

    @Nullable
    public static UUID subLevelUuid(BlockEntity blockEntity) {
        SubLevel subLevel = Sable.HELPER.getContaining(blockEntity);
        return subLevel == null ? null : subLevel.getUniqueId();
    }

    @Nullable
    public static BlockEntity resolve(Level parentLevel, UUID subLevelUuid, BlockPos pos) {
        SubLevelContainer container = SubLevelContainer.getContainer(parentLevel);
        if (container == null) {
            return null;
        }
        SubLevel subLevel = container.getSubLevel(subLevelUuid);
        if (subLevel == null) {
            return null;
        }
        return subLevel.getLevel().getBlockEntity(pos);
    }
}
