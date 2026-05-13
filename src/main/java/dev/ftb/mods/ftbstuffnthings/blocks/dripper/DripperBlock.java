package dev.ftb.mods.ftbstuffnthings.blocks.dripper;

import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

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
	public static final int WATER_BOTTLE_AMOUNT = 250;

	public DripperBlock(Properties properties) {
		super(properties.mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2F).randomTicks());
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
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DripperBlockEntity dripper) {
            var tank = dripper.getTank();

			InteractionResult bottleRes = tryUseWaterBottle(stack, level, pos, player, tank);
			if (bottleRes != null) {
				return bottleRes;
			}

			FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection());

			if (tank.getAmountAsInt(0) == 0) {
				player.sendOverlayMessage(Component.translatable("ftblibrary.empty"));
			} else {
				player.sendOverlayMessage(Component.translatable("ftblibrary.mb",
						tank.getAmountAsInt(0), tank.getResource(0).getHoverName()));
			}
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Nullable
	private static InteractionResult tryUseWaterBottle(ItemStack stack, Level level, BlockPos pos, Player player, ResourceHandler<FluidResource> tank) {
		if (!stack.is(Items.POTION)) return null;

		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		if (contents == null || !contents.is(Potions.WATER)) return null;

		try (Transaction tx = Transaction.openRoot()) {
			int filled = tank.insert(FluidResource.of(Fluids.WATER), WATER_BOTTLE_AMOUNT, tx);
			if (filled < WATER_BOTTLE_AMOUNT) {
				return InteractionResult.FAIL;
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1F, 1F);
			tx.commit();
		}

		if (!player.isCreative()) {
			stack.shrink(1);
			player.addItem(new ItemStack(Items.GLASS_BOTTLE));
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (state.getValue(ACTIVE)) {
			BlockEntity entity = level.getBlockEntity(pos);
			boolean foundParticle = false;

			if (entity instanceof DripperBlockEntity dripper && dripper.getTank().getAmountAsInt(0) > 0) {
				FluidResource fluid = dripper.getTank().getResource(0);

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
