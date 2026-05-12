package dev.ftb.mods.ftbstuffnthings.blocks.tube;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

public interface ITubeConnectable {
    static boolean canConnect(Level level, BlockPos pos, Direction face) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, face) != null
                || level.getCapability(Capabilities.Fluid.BLOCK, pos, face) != null
                || level.getBlockState(pos).getBlock() instanceof ITubeConnectable c && c.isSideTubeConnectable(face)
                || level.getBlockEntity(pos) instanceof ITubeConnectable c1 && c1.isSideTubeConnectable(face);
    }

    boolean isSideTubeConnectable(Direction side);
}
