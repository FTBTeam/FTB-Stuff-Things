package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;

public interface IResourceGenProps {
    String getName();

    String textureId();

    String resourceId();

    int itemsPerOperation();

    BlockEntity createBlockEntity(BlockPos pos, BlockState blockState);

    default String description() {
        return StringUtils.capitalize(getName()) + " " + StringUtils.capitalize(resourceId()) + " Generator";
    }

    default String getBlockId() {
        return getName() + "_" + resourceId() + "_generator";
    }
}
