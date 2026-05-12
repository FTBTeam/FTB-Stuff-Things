package dev.ftb.mods.ftbstuffnthings.tubes;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.tube.TubeBlockEntity;
import dev.ftb.mods.ftbstuffnthings.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.*;

public enum TubeNetwork {
    INSTANCE;

    private final Map<ResourceKey<Level>, Map<BlockPos, ConnectedHandlers>> handlerMap = new HashMap<>();

    public static ConnectedHandlers getConnectedHandlers(BlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("only call this on the server!");
        }

        Map<BlockPos, ConnectedHandlers> map = TubeNetwork.INSTANCE.handlerMap
                .computeIfAbsent(blockEntity.getLevel().dimension(), k -> new HashMap<>());

        BlockPos pos = blockEntity.getBlockPos();
        return map.computeIfAbsent(pos, k -> walkTubeNetwork(serverLevel, pos.above()));
    }

    private static ConnectedHandlers walkTubeNetwork(ServerLevel level, BlockPos initialPos) {
        ConnectedHandlers conn = ConnectedHandlers.create();
        if (!isValidTubeBlock(level, initialPos, Direction.DOWN, level.getBlockState(initialPos))) {
            return conn;
        }

        Set<BlockPos> traversedPositions = new HashSet<>();
        Deque<BlockPos> pendingPositions = new ArrayDeque<>(List.of(initialPos));

        while (!pendingPositions.isEmpty()) {
            BlockPos pos = pendingPositions.pop();
            for (Direction dir : DirectionUtil.VALUES) {
                if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube && tube.isSideClosed(dir)) {
                    continue;
                }
                BlockPos pos1 = pos.relative(dir);
                if (!level.isLoaded(pos1)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos1);
                Direction opposite = dir.getOpposite();
                if (isValidTubeBlock(level, pos, opposite, state) && traversedPositions.add(pos1)) {
                    // a tube... we'll search along there next
                    pendingPositions.add(pos1);
                } else {
                    // something else... check for item and fluid handlers
                    conn.checkAndAddHandlers(level, pos1, opposite);
                }
            }
        }
        return conn;
    }

    private static boolean isValidTubeBlock(ServerLevel level, BlockPos pos, Direction dir, BlockState blockState) {
        if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube && !tube.isSideClosed(dir)) {
            return true;
        }
        // TODO auto-processing block
        return false;
//        return blockState.getBlock() == BlocksRegistry.AUTOPROCESSOR.get();
    }

    @EventBusSubscriber(modid = FTBStuffNThings.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onServerShutdown(ServerStoppingEvent ignored) {
            // important when using integrated server
            TubeNetwork.INSTANCE.handlerMap.clear();
        }
    }
}
