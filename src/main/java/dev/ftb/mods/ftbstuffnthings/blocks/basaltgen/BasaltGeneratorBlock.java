package dev.ftb.mods.ftbstuffnthings.blocks.basaltgen;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BasaltGeneratorBlock extends Block implements EntityBlock {

    private final BasaltGeneratorProperties properties;

    public BasaltGeneratorBlock(BasaltGeneratorProperties properties) {
        super(Properties.of().mapColor(MapColor.STONE).strength(1F, 1F).noOcclusion());
        this.properties = properties;

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.ENABLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.ENABLED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(BlockStateProperties.ENABLED, true)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof  BasaltGeneratorBlockEntity basaltGenerator && player.getMainHandItem().isEmpty()) {
            ItemStack itemStack = basaltGenerator.getInternalInventory().extractItem(0, 64, false);
            if (!itemStack.isEmpty()) {
                player.addItem(itemStack);
                player.level().playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 1f);
            }
            return InteractionResult.CONSUME;
        }
        return  InteractionResult.PASS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, t) -> {
          if (t instanceof BasaltGeneratorBlockEntity tickable) {
              if (!level1.isClientSide()) {
                  tickable.tickServer();
              }
          }
        };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        boolean hasSignal = !level.hasNeighborSignal(pos);
        if (hasSignal != state.getValue(BlockStateProperties.ENABLED)) {
            var newState = state.setValue(BlockStateProperties.ENABLED, hasSignal);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return properties.createBlockEntity(blockPos, blockState);
    }
}
