package dev.ftb.mods.ftbstuffnthings.blocks.hammer;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AutoHammerBlock extends Block implements EntityBlock {
    private static final VoxelShape EAST_WEST = VoxelShapeUtils.or(
            box(1, 4, 1, 15, 14, 15),
            box(0, 4, 0, 2, 14, 2),
            box(0, 4, 14, 2, 14, 16),
            box(14, 4, 0, 16, 14, 2),
            box(14, 4, 14, 16, 14, 16),
            box(0, 0, 0, 16, 4, 16),
            box(0, 14, 0, 16, 16, 16),
            box(4, 4, 0, 12, 12, 2),
            box(4, 4, 14, 12, 12, 16)
    );
    public static final VoxelShape NORTH_SOUTH = VoxelShapeUtils.rotateY(EAST_WEST, 90);

    private final AutoHammerProperties props;

    public AutoHammerBlock(AutoHammerProperties props) {
        super(Properties.of().mapColor(MapColor.STONE).strength(1F, 1F));

        this.props = props;

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(AbstractMachineBlock.ACTIVE, false)
                .setValue(BlockStateProperties.ENABLED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, AbstractMachineBlock.ACTIVE, BlockStateProperties.ENABLED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection())
                .setValue(BlockStateProperties.ENABLED, true);
    }

    @Override
    public VoxelShape getVisualShape(BlockState arg, BlockGetter arg2, BlockPos arg3, CollisionContext arg4) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            return NORTH_SOUTH;
        } else {
            return EAST_WEST;
        }
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof AutoHammerBlockEntity autoHammer) {
                autoHammer.dropInventoryOnBreak(world);
                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof AutoHammerBlockEntity tickable) {
                if (level1 instanceof ServerLevel serverLevel) {
                    tickable.tickServer(serverLevel);
                } else {
                    tickable.tickClient(level1);
                }
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return props.createBlockEntity(pos, blockState);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        if (level.getBlockEntity(pos) instanceof AutoHammerBlockEntity autoHammer) {
            autoHammer.invalidateCapabilities();  // what other things know about our caps
            autoHammer.clearCapabilityCaches();   // what we know about other things' caps
        }
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, direction.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!oldState.is(state.getBlock())) {
            this.checkPoweredState(level, pos, state);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        checkPoweredState(level, pos, state);
    }

    private void checkPoweredState(Level level, BlockPos pos, BlockState state) {
        boolean hasSignal = !level.hasNeighborSignal(pos);
        if (hasSignal != state.getValue(BlockStateProperties.ENABLED)) {
            var newState = state.setValue(BlockStateProperties.ENABLED, hasSignal);

            // If the block is no longer enabled but is active, toggle the active state
            if (!hasSignal && state.getValue(AbstractMachineBlock.ACTIVE)) {
                newState = newState.setValue(AbstractMachineBlock.ACTIVE, false);
            }

            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }
}
