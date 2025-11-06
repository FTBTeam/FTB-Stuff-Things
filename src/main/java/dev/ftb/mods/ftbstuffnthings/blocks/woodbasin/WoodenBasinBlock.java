package dev.ftb.mods.ftbstuffnthings.blocks.woodbasin;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class WoodenBasinBlock extends Block implements EntityBlock {
    // same shape as a vanilla cauldron
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(
                    box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
                    box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
                    box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    INSIDE
            ), BooleanOp.ONLY_FIRST);

    public WoodenBasinBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2F));
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WoodenBasinBlockEntity(blockPos, blockState);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof WoodenBasinBlockEntity basin
                    && FluidUtil.interactWithFluidHandler(player, hand, basin.getFluidHandler()))
            {
                return ItemInteractionResult.CONSUME;
            }
        }
        return stack.getCapability(Capabilities.FluidHandler.ITEM) == null ?
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION :
                ItemInteractionResult.SUCCESS;
    }

    @EventBusSubscriber(modid = FTBStuffNThings.MODID)
    public static class Listener {
        @SubscribeEvent
        public static void onEntityFall(LivingFallEvent event) {
            if (!event.getEntity().level().isClientSide) {
                BlockPos pos = event.getEntity().getOnPos();
                if (event.getDistance() > 0.5 && event.getEntity().level().getBlockEntity(pos.below()) instanceof WoodenBasinBlockEntity basin) {
                    basin.trySqueezing(event.getEntity());
                }
            }
        }
    }
}
