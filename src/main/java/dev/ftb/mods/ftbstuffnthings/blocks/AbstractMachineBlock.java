package dev.ftb.mods.ftbstuffnthings.blocks;

import dev.ftb.mods.ftbstuffnthings.client.ClientUtil;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.SubLevelMenuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class AbstractMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected static Properties defaultMachineProps() {
        return Properties.of().mapColor(MapColor.STONE).strength(1F, 1F);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (context.level() != null) {
            if (context.level().isClientSide) {
                ClientUtil.maybeAddBlockTooltip(stack, tooltipComponents);
            }
            int energy = stack.getOrDefault(ComponentsRegistry.STORED_ENERGY, 0);
            if (energy > 0) {
                tooltipComponents.add(Component.translatable("ftbstuff.tooltip.energy", energy).withStyle(ChatFormatting.YELLOW));
            }
            FluidStack fluidStack = stack.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy();
            if (!fluidStack.isEmpty()) {
                tooltipComponents.add(Component.translatable("ftbstuff.tooltip.fluid", fluidStack.getAmount(), fluidStack.getHoverName()).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMachineBlockEntity machine) {
                IFluidHandler handler = machine.getFluidHandler(hitResult.getDirection());
                if (handler != null) {
                    // handle filling/emptying with bucket (or other fluid containing item)
                    if (FluidUtil.interactWithFluidHandler(player, hand, handler)) {
                        return ItemInteractionResult.CONSUME;
                    }
                }
            }
            if (blockEntity instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider, buf -> SubLevelMenuHelper.writeLocator(buf, blockEntity));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
                machine.dropItemContents();
            }
        }

        super.onRemove(state, level, pos, newState, bl);
    }


    private static boolean doFluidInteraction(BlockEntity te, Direction face, Player player, InteractionHand hand, boolean isInserting) {
        ItemStack stack = player.getItemInHand(hand);
        return FluidUtil.getFluidHandler(stack).map(stackHandler -> {
            IFluidHandler handler = te.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, te.getBlockPos(), te.getBlockState(), te, face);
            if (handler != null) {
                if (stackHandler.getTanks() == 0) return false;
                int capacity = stackHandler.getTankCapacity(0);
                PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.getInventory());
                FluidActionResult result = isInserting ?
                        FluidUtil.tryEmptyContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true) :
                        FluidUtil.tryFillContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true);
                if (result.isSuccess()) {
                    player.setItemInHand(hand, result.getResult());
                    return true;
                }
                return false;
            }
            return false;
        }).orElse(false);
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
}
