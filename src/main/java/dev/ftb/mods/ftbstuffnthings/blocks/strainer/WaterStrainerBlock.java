package dev.ftb.mods.ftbstuffnthings.blocks.strainer;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WaterStrainerBlock extends AbstractMachineBlock {
    private static final VoxelShape SHAPE0 = VoxelShapeUtils.or(
        box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        box(0.5, 2.0, 0.5, 15.5, 14.0, 15.5),
        box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
        box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0),
        box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
        box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0)
    );
    private static final VoxelShape SHAPE1 = box(1.5, 2.0, 1.5, 14.5, 14.0, 14.5);
    private static final VoxelShape SHAPE = Shapes.join(SHAPE0, SHAPE1, BooleanOp.ONLY_FIRST);

    private final WoodType type;

    public WaterStrainerBlock(BlockBehaviour.Properties props, WoodType type) {
        super(props);

        this.type = type;
    }

    @Override
    protected boolean hasActiveStateProperty() {
        return false;
    }

    public static Properties defaultProps() {
        return Properties.of().sound(SoundType.WOOD).strength(0.6F).noOcclusion();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext arg) {
        FluidState fluidState = arg.getLevel().getFluidState(arg.getClickedPos());
        BlockState state = super.getStateForPlacement(arg);
        return state == null ? null : state.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
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
        return SHAPE;
    }

    public WoodType getWoodType() {
        return type;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterStrainerBlockEntity(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos, BlockEntitiesRegistry.WATER_STRAINER.get())
                .map(WaterStrainerBlockEntity::getComparatorLevel)
                .orElse(0);
    }
}
