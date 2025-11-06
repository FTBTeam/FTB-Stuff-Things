package dev.ftb.mods.ftbstuffnthings.blocks.dripper;

import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class DripperBlock extends Block implements EntityBlock {
	public static final VoxelShape SHAPE = VoxelShapeUtils.or(
			Block.box(7, 8, 7, 9, 9, 9),
			Block.box(0, 13, 0, 16, 16, 16),
			Block.box(1, 12, 1, 15, 13, 15),
			Block.box(3, 11, 3, 13, 12, 13),
			Block.box(5, 10, 5, 11, 11, 11),
			Block.box(6, 9, 6, 10, 10, 10)
	);

	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public DripperBlock() {
		super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2F).randomTicks());
		registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DripperBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DripperBlockEntity dripper) {
			FluidTank tank = dripper.getTank();
			FluidUtil.interactWithFluidHandler(player, hand, tank);

			if (tank.getFluidAmount() == 0) {
				player.displayClientMessage(Component.translatable("ftblibrary.empty"), true);
			} else {
				player.displayClientMessage(Component.translatable("ftblibrary.mb",
						tank.getFluidAmount(), tank.getFluid().getHoverName()), true);
			}
		}

		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (state.getValue(ACTIVE)) {
			BlockEntity entity = level.getBlockEntity(pos);
			boolean foundParticle = false;

			if (entity instanceof DripperBlockEntity dripper && !dripper.getTank().isEmpty()) {
				FluidStack fluid = dripper.getTank().getFluid();

				if (!fluid.isEmpty()) {
					BlockState dripState = fluid.getFluid().defaultFluidState().createLegacyBlock();
					if (dripState.getBlock() != Blocks.AIR) {
						level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, dripState), pos.getX() + 0.5D, pos.getY() + 0.475D, pos.getZ() + 0.5D, 0D, -1D, 0D);
						foundParticle = true;
					}
				}
			}

			if (!foundParticle) {
				level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return (level1, blockPos, blockState, be) -> {
			if (be instanceof DripperBlockEntity dripper && level1 instanceof ServerLevel serverLevel) {
				dripper.serverTick(serverLevel);
			}
		};
	}
}
