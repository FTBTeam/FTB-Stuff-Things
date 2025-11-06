package dev.ftb.mods.ftbstuffnthings.blocks.pump;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.registry.CriterionTriggerRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ModDamageSources;
import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class PumpBlock extends AbstractMachineBlock implements EntityBlock {
    public enum Progress implements StringRepresentable {
        ZERO,
        TWENTY,
        FORTY,
        SIXTY,
        EIGHTY,
        HUNDRED;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public static final int PUMP_DAMAGE_AMOUNT = 2;

    private static final VoxelShape NORTH = VoxelShapeUtils.or(
            box(0, 0, 0, 16, 10, 16),
            box(3, 10, 6, 13, 13, 9),
            box(6, 10, 0, 10, 12, 2),
            box(0, 15, 8, 16, 16, 16),
            box(1, 10, 9, 15, 15, 15),
            box(0, 10, 15, 1, 15, 16),
            box(0, 10, 8, 1, 15, 9),
            box(15, 10, 8, 16, 15, 9),
            box(15, 10, 15, 16, 15, 16),
            box(2.5, 4, 3, 13.5, 15, 5),
            box(7, 9, 2, 9, 11, 6)
    );
    private static final VoxelShape EAST = VoxelShapeUtils.rotateY(NORTH, 90);
    private static final VoxelShape SOUTH = VoxelShapeUtils.rotateY(EAST, 90);
    private static final VoxelShape WEST = VoxelShapeUtils.rotateY(SOUTH, 90);
    private static final Map<Direction,VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST
    ));

    public static final EnumProperty<Progress> PROGRESS = EnumProperty.create("progress", Progress.class);

    public PumpBlock() {
        super(Properties.of().sound(SoundType.STONE).strength(1f, 1f));

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PROGRESS, Progress.ZERO)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(PROGRESS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof FakePlayer) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PumpBlockEntity pump)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && !pump.windUp()) {
            // overwound, oops!
            player.hurt(level.damageSources().source(ModDamageSources.STATIC_ELECTRIC, player), PUMP_DAMAGE_AMOUNT);
            if (player instanceof ServerPlayer sp) CriterionTriggerRegistry.SUPERCHARGED.get().trigger(sp);
            if (player.getHealth() - PUMP_DAMAGE_AMOUNT < 0) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(player.blockPosition()));
                    lightning.setVisualOnly(true);
                    level.addFreshEntity(lightning);
                }
            } else if (level instanceof ServerLevel serverLevel) {
                Vec3 vec = Vec3.atCenterOf(pos.above());
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, vec.x, vec.y, vec.z,
                        15, 0.2, 0.2, 0.2, 0.5);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PumpBlockEntity pump)) {
            return ItemInteractionResult.FAIL;
        }

        if (pump.creative) {
            ItemStack itemInHand = player.getItemInHand(hand);

            ItemInteractionResult result = ItemInteractionResult.FAIL;
            // Try a normal bucket
            if (!itemInHand.isEmpty()) {
                if (itemInHand.getItem() instanceof BucketItem bucketItem) {
                    pump.creativeFluid = bucketItem.content;
                    sendTileUpdate(level, pos, state, pump);
                    result = ItemInteractionResult.SUCCESS;
                } else {
                    IFluidHandlerItem capability = itemInHand.getCapability(Capabilities.FluidHandler.ITEM);
                    if (capability != null) {
                        // Take the first one
                        pump.creativeFluid = capability.getFluidInTank(0).getFluid();
                        sendTileUpdate(level, pos, state, pump);
                        result = ItemInteractionResult.SUCCESS;
                    }
                }
            }

            if (result.consumesAction()) {
                return result;
            }

            pump.creativeItem = itemInHand.getItem();
            if (!level.isClientSide) {
                sendTileUpdate(level, pos, state, pump);
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void sendTileUpdate(Level level, BlockPos pos, BlockState state, PumpBlockEntity tile) {
        tile.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PumpBlockEntity(blockPos, blockState);
    }

    public VoxelShape getVisualShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return SHAPES.getOrDefault(dir, NORTH);
    }
}
