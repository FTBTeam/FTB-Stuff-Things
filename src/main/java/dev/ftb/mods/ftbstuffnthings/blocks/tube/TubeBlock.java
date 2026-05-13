package dev.ftb.mods.ftbstuffnthings.blocks.tube;

import dev.ftb.mods.ftbstuffnthings.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TubeBlock extends Block implements EntityBlock {
    public static final VoxelShape CENTER_SHAPE = box(6, 6, 6, 10, 10, 10);
    public static final VoxelShape[] ARM_SHAPE = new VoxelShape[] {
            // DUNSWE order
            box(6, 0, 6, 10, 6, 10),
            box(6, 10, 6, 10, 16, 10),
            box(6, 6, 0, 10, 10, 6),
            box(6, 6, 10, 10, 10, 16),
            box(0, 6, 6, 6, 10, 10),
            box(10, 6, 6, 16, 10, 10),
    };

    private static final Map<Integer,VoxelShape> SHAPE_CACHE = new ConcurrentHashMap<>();

    public TubeBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).strength(0.7F).sound(SoundType.NETHERITE_BLOCK));

        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TubeBlockEntity(blockPos, blockState);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube) {
            return getCachedShape(tube);
        }
        return CENTER_SHAPE; // shouldn't get here
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube) {
            tube.updateConnectedSides();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

        if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube) {
            // wtf orientation
            Direction relDir = null;//DirectionUtil.getRelativeDirection(pos, neighborPos);
            if (relDir != null) {
                tube.updateSide(relDir, true);
            }
        }
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            int x = coord(hitResult.getLocation().x - pos.getX());
            int y = coord(hitResult.getLocation().y - pos.getY());
            int z = coord(hitResult.getLocation().z - pos.getZ());
            Direction dir = getDirForHitLocation(x, y, z, hitResult.getDirection());
            if (level.getBlockEntity(pos) instanceof TubeBlockEntity tube) {
                if (tube.isSideClosed(dir)) {
                    // side closed; re-open it, and rescan for neighbouring connections
                    BlockPos pos1 = pos.relative(dir);
                    BlockState state1 = level.getBlockState(pos1);
                    if (state1.getBlock() == this) {
                        tube.setSideClosed(dir, false);
                    }
                } else if (tube.isSideConnected(dir)) {
                    // side is connected; close it
                    tube.setSideClosed(dir, true);
                } else {
                    // side is not currently connected to anything; if neighbouring tube facing us is closed, re-open it
                    if (level.getBlockEntity(pos.relative(dir)) instanceof TubeBlockEntity neighbourTube) {
                        if (neighbourTube.isSideClosed(dir.getOpposite())) {
                            neighbourTube.setSideClosed(dir.getOpposite(), false);
                            neighbourTube.updateSide(dir.getOpposite(), true);
                        }
                    }
                }
                tube.updateConnectedSides();

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private static int coord(double v) {
        return v < 0.375D ? -1 : v > 0.625D ? 1 : 0;
    }

    private static Direction getDirForHitLocation(int x, int y, int z, Direction dir) {
        return Arrays.stream(DirectionUtil.VALUES)
                .filter(d -> d.getStepX() == x && d.getStepY() == y && d.getStepZ() == z)
                .findFirst()
                .orElse(dir);

    }

    private VoxelShape getCachedShape(TubeBlockEntity tube) {
        int data = tube.getShapeCacheKey();
        VoxelShape cachedShape = SHAPE_CACHE.get(data);
        if (cachedShape == null) {
            cachedShape = CENTER_SHAPE;
            for (Direction dir : Direction.values()) {
                if (tube.isSideClosed(dir) || tube.isSideConnected(dir)) {
                    cachedShape = Shapes.joinUnoptimized(cachedShape, ARM_SHAPE[dir.get3DDataValue()], BooleanOp.OR);
                }
            }
            cachedShape = cachedShape.optimize();
            SHAPE_CACHE.put(data, cachedShape);
        }
        return cachedShape;
    }
}
