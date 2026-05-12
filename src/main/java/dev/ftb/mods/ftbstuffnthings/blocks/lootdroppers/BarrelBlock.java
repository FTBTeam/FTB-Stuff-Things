package dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers;

import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BarrelBlock extends Block {
    public static final VoxelShape SHAPE = VoxelShapeUtils.or(
            box(1, 1, 1, 15, 15, 15),
            box(0, 0, 1, 16, 1, 15),
            box(0, 15, 1, 1, 16, 15),
            box(15, 15, 1, 16, 16, 15),
            box(1, 0, 15, 15, 1, 16),
            box(1, 15, 15, 15, 16, 16),
            box(1, 0, 0, 15, 1, 1),
            box(1, 15, 0, 15, 16, 1),
            box(0.5, 10, 1, 1.5, 11, 15),
            box(14.5, 10, 1, 15.5, 11, 15),
            box(0.5, 5, 1, 1.5, 6, 15),
            box(14.5, 5, 1, 15.5, 6, 15),
            box(1, 5, 0.5, 15, 6, 1.5),
            box(1, 10, 0.5, 15, 11, 1.5),
            box(1, 10, 14.5, 15, 11, 15.5),
            box(1, 5, 14.5, 15, 6, 15.5)
    );

    public BarrelBlock() {
        super(Properties.of().strength(5f, 6f).sound(SoundType.NETHERITE_BLOCK).noOcclusion());
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> arg) {
        arg.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext arg) {
        FluidState fluidState = arg.getLevel().getFluidState(arg.getClickedPos());
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }
}
