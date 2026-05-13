package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

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
        TemperatureAndEfficiency tempEff = context.getLevel() instanceof ServerLevel l ?
                TemperatureAndEfficiency.fromLevel(l, context.getClickedPos().below()) :
                TemperatureAndEfficiency.DEFAULT;

        return defaultBlockState().setValue(TEMPERATURE, tempEff.temperature());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (level1, _, _, be) -> {
            if (be instanceof TemperedJarBlockEntity jar && level1 instanceof ServerLevel serverLevel) {
                jar.serverTick(serverLevel);
            }
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TemperedJarBlockEntity jar) {
            if (directionToNeighbour == Direction.DOWN || directionToNeighbour == Direction.UP) {
                // heat blocks below, autoprocessing block above
                jar.clearCachedData();
            }
        }

        return directionToNeighbour == Direction.DOWN && level instanceof ServerLevel l ?
                state.setValue(TEMPERATURE, TemperatureAndEfficiency.fromLevel(l, neighbourPos).temperature()) :
                state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack item = player.getItemInHand(hand);

        if (hitResult.getDirection() == Direction.UP && item.getItem() == ItemsRegistry.TUBE.get()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TemperedJarBlockEntity jar) {
            if (!player.isShiftKeyDown()) {
                if (!jar.onRightClick(player, hand)) {
                    player.openMenu(jar, buf -> {
                        buf.writeBlockPos(pos);
                        buf.writeOptional(jar.getCurrentRecipeId(), FriendlyByteBuf::writeIdentifier);
                    });
                }
            } else {
                jar.maybeClearBacklog(level, hitResult.getDirection());
            }
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            Temperature temperature = state.getValue(TEMPERATURE);
            for (int i = 0; i < 12; i++) {
                if (level.getRandom().nextInt(4) == 0) {
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

    public static class TemperedJarBlockItem extends BlockItem {
        public TemperedJarBlockItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
            super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

            List<SimpleFluidContent> l = itemStack.getOrDefault(ComponentsRegistry.FLUID_TANKS, List.of());
            l.forEach(content -> builder.accept(MiscUtil.makeFluidStackDesc(content.copy())));
        }
    }
}
