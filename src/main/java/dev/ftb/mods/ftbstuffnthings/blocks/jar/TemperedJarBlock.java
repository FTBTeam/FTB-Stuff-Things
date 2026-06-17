package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import dev.ftb.mods.ftbstuffnthings.util.SubLevelMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TemperedJarBlock extends JarBlock {
    public static final EnumProperty<Temperature> TEMPERATURE = EnumProperty.create("temperature", Temperature.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public TemperedJarBlock() {
        super();

        registerDefaultState(getStateDefinition().any().setValue(TEMPERATURE, Temperature.NORMAL).setValue(ACTIVE, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TemperedJarBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(TEMPERATURE, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        TemperatureAndEfficiency tempEff = TemperatureAndEfficiency.fromLevel(context.getLevel(), context.getClickedPos().below());
        return defaultBlockState().setValue(TEMPERATURE, tempEff.temperature());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, be) -> {
            if (be instanceof TemperedJarBlockEntity jar && level1 instanceof ServerLevel serverLevel) {
                jar.serverTick(serverLevel);
            }
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level.getBlockEntity(pos) instanceof TemperedJarBlockEntity jar) {
            if (direction == Direction.DOWN || direction == Direction.UP) {
                // heat blocks below, autoprocessing block above
                jar.clearCachedData();
            }
        }

        return direction == Direction.DOWN && level instanceof Level l ?
                state.setValue(TEMPERATURE, TemperatureAndEfficiency.fromLevel(l, neighborPos).temperature()) :
                state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack item = player.getItemInHand(hand);

        if (hitResult.getDirection() == Direction.UP && item.getItem() == ItemsRegistry.TUBE.get()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TemperedJarBlockEntity jar) {
            if (!player.isShiftKeyDown()) {
                if (!jar.onRightClick(player, hand)) {
                    player.openMenu(jar, buf -> {
                        SubLevelMenuHelper.writeLocator(buf, jar);
                        buf.writeOptional(jar.getCurrentRecipeId(), FriendlyByteBuf::writeResourceLocation);
                    });
                }
            } else {
                jar.maybeClearBacklog(hitResult.getDirection());
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            Temperature temperature = state.getValue(TEMPERATURE);
            for (int i = 0; i < 12; i++) {
                if (level.random.nextInt(4) == 0) {
                    float angle = (i / 12F) * Mth.TWO_PI;
                    double x = pos.getX() + 0.5D + Mth.cos(angle) * 0.25D;
                    double y = pos.getY() + temperature.getParticleYOffset();
                    double z = pos.getZ() + 0.5D + Mth.sin(angle) * 0.25D;
                    level.addParticle(temperature.getParticleOptions(), x, y, z, 0D, 0D, 0D);
                }
            }
        }
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        list.add(ComponentsRegistry.FLUID_TANKS.get());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof TemperedJarBlockEntity jar) {
                jar.dropContentsOnBreak();
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        List<SimpleFluidContent> l = stack.getOrDefault(ComponentsRegistry.FLUID_TANKS, List.of());
        l.forEach(content -> tooltipComponents.add(MiscUtil.makeFluidStackDesc(content.copy())));
    }
}
