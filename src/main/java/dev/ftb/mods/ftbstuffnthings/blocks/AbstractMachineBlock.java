package dev.ftb.mods.ftbstuffnthings.blocks;

import dev.ftb.mods.ftbstuffnthings.client.ClientUtil;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class AbstractMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected static Properties applyDefaultMachineProps(Properties properties) {
        return properties.mapColor(MapColor.STONE).strength(1F, 1F);
    }

    public AbstractMachineBlock(Properties props) {
        super(props);

        BlockState state = getStateDefinition().any();
        if (isDirectional()) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        }
        if (state.hasProperty(ACTIVE)) {
            state = state.setValue(ACTIVE, false);
        }
        if (defaultBlockState().hasProperty(WATERLOGGED)) {
            state = state.setValue(WATERLOGGED, false);
        }
        registerDefaultState(state);
    }

    protected boolean hasActiveStateProperty() {
        return true;
    }

    protected boolean isDirectional() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (isDirectional()) {
            builder.add(BlockStateProperties.HORIZONTAL_FACING);
        }
        if (hasActiveStateProperty()) {
            builder.add(ACTIVE);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        if (isDirectional()) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
        }
        if (state.hasProperty(WATERLOGGED)) {
            FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
            state = state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        }
        return state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMachineBlockEntity
                    && FluidUtil.interactWithFluidHandler(player, hand, player.level(), pos, hitResult.getDirection()))
            {
                return InteractionResult.CONSUME;
            }
            if (blockEntity instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider, pos);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState arg2, BlockEntityType<T> arg3) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof AbstractMachineBlockEntity tickable) {
                if (level1 instanceof ServerLevel serverLevel) {
                    tickable.tickServer(serverLevel);
                } else {
                    tickable.tickClient(level1);
                }
            }
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return isDirectional() ?
                state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))) :
                state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return isDirectional() ?
                state.setValue(BlockStateProperties.HORIZONTAL_FACING, mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING))) :
                state;
    }

    public static class MachineBlockItem extends BlockItem {
        public MachineBlockItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
            super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

            if (context.level() != null) {
                if (context.level().isClientSide()) {
                    ClientUtil.maybeAddBlockTooltip(itemStack, builder);
                }
                int energy = itemStack.getOrDefault(ComponentsRegistry.STORED_ENERGY, 0);
                if (energy > 0) {
                    builder.accept(Component.translatable("ftbstuff.tooltip.energy", energy).withStyle(ChatFormatting.YELLOW));
                }
                FluidStack fluidStack = itemStack.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy();
                if (!fluidStack.isEmpty()) {
                    builder.accept(Component.translatable("ftbstuff.tooltip.fluid", fluidStack.getAmount(), fluidStack.getHoverName()).withStyle(ChatFormatting.YELLOW));
                }
            }
        }
    }
}
