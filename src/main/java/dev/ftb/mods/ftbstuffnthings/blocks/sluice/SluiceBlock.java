package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.SerializableComponentsProvider;
import dev.ftb.mods.ftbstuffnthings.items.MeshItem;
import dev.ftb.mods.ftbstuffnthings.items.MeshType;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.TextUtil;
import dev.ftb.mods.ftbstuffnthings.util.VoxelShapeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class SluiceBlock extends AbstractMachineBlock implements EntityBlock, SerializableComponentsProvider {
    public static final EnumProperty<MeshType> MESH = EnumProperty.create("mesh", MeshType.class);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final VoxelShape NORTH_BODY_SHAPE = VoxelShapeUtils.or(
            box(12.5, 0, 0, 14.5, 1, 1),
            box(1.5, 0, 13.5, 3.5, 1, 15.5),
            box(12.5, 0, 13.5, 14.5, 1, 15.5),
            box(1.5, 0, 0, 3.5, 1, 1),
            box(1, 1, 0, 15, 2, 16),
            box(14, 2, 0, 15, 8, 16),
            box(1, 2, 0, 2, 8, 16),
            box(2, 5, 0, 14, 8, 1),
            box(2, 2, 15, 14, 8, 16),
            box(2, 2, 0, 14, 2.5, 1),
            box(2, 7, 1, 14, 12, 2),
            box(2, 7, 14, 14, 12, 15),
            box(13, 7, 2, 14, 12, 14),
            box(2, 7, 2, 3, 12, 14)
    );
    private static final VoxelShape EAST_BODY_SHAPE = VoxelShapeUtils.rotateY(NORTH_BODY_SHAPE, 90);
    private static final VoxelShape SOUTH_BODY_SHAPE = VoxelShapeUtils.rotateY(NORTH_BODY_SHAPE, 180);
    private static final VoxelShape WEST_BODY_SHAPE = VoxelShapeUtils.rotateY(NORTH_BODY_SHAPE, 270);

    private static final VoxelShape NORTH_FRONT_SHAPE = VoxelShapeUtils.or(
            box(2, 1.5, 12, 14, 2.5, 13),
            box(1, 2, 0, 2, 4, 16),
            box(2, 1.5, 8, 14, 2.5, 9),
            box(2, 1.5, 4, 14, 2.5, 5),
            box(1, 1, 0, 15, 2, 16),
            box(14, 2, 0, 15, 4, 16),
            box(12.5, 0, 0.5, 14.5, 1, 2.5),
            box(1.5, 0, 0.5, 3.5, 1, 2.5),
            box(1.5, 0, 15, 3.5, 1, 16),
            box(12.5, 0, 15, 14.5, 1, 16)
    );
    private static final VoxelShape EAST_FRONT_SHAPE = VoxelShapeUtils.rotateY(NORTH_FRONT_SHAPE, 90);
    private static final VoxelShape SOUTH_FRONT_SHAPE = VoxelShapeUtils.rotateY(NORTH_FRONT_SHAPE, 180);
    private static final VoxelShape WEST_FRONT_SHAPE = VoxelShapeUtils.rotateY(NORTH_FRONT_SHAPE, 270);

    private static final Map<Direction, Pair<VoxelShape, VoxelShape>> SHAPES = new EnumMap<>(Map.of(
            Direction.NORTH, Pair.of(NORTH_BODY_SHAPE, NORTH_FRONT_SHAPE),
            Direction.EAST, Pair.of(EAST_BODY_SHAPE, EAST_FRONT_SHAPE),
            Direction.SOUTH, Pair.of(SOUTH_BODY_SHAPE, SOUTH_FRONT_SHAPE),
            Direction.WEST, Pair.of(WEST_BODY_SHAPE, WEST_FRONT_SHAPE)
    ));

    private final SluiceType sluiceType;
    private final Lazy<SluiceProperties> props;

    public SluiceBlock(Properties properties, SluiceType sluiceType, SoundType soundType) {
        super(properties.sound(soundType).strength(0.9F).forceSolidOn());
        this.sluiceType = sluiceType;

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(MESH, MeshType.EMPTY)
                .setValue(PART, Part.MAIN)
                .setValue(HORIZONTAL_FACING, Direction.NORTH));

        this.props = ModConfig.makeSluiceProperties(sluiceType);
    }

    public SluiceType getSluiceType() {
        return sluiceType;
    }

    @Override
    protected boolean hasActiveStateProperty() {
        return false;
    }

    public SluiceProperties getProps() {
        return props.get();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return state.getValue(PART) == Part.FUNNEL ? List.of() : super.getDrops(state, params);
    }

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(HORIZONTAL_FACING);
        if (!SHAPES.containsKey(direction)) {
            // HOW?!
            return Shapes.empty();
        }

        Pair<VoxelShape, VoxelShape> bodyFrontShapes = SHAPES.get(direction);
        return state.getValue(PART) == Part.MAIN
                ? bodyFrontShapes.getFirst()
                : bodyFrontShapes.getSecond();
    }

    @Override
    @Deprecated
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (state.getValue(MESH) != MeshType.EMPTY) {
                ItemStack meshStack = state.getValue(MESH).createItemStack();
                level.setBlock(pos, state.setValue(MESH, MeshType.EMPTY), Block.UPDATE_ALL);

                if (!level.isClientSide()) {
                    player.getInventory().placeItemBackInInventory(meshStack);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(PART) == Part.FUNNEL || !(level.getBlockEntity(pos) instanceof SluiceBlockEntity sluice)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (stack.is(FTBStuffTags.Items.MESHES)) {
            if (stack.getItem() instanceof MeshItem meshItem) {
                if (isMeshCompatibleWith(meshItem.mesh)) {
                    ItemStack meshStack = state.getValue(MESH).createItemStack();
                    level.setBlock(pos, state.setValue(MESH, meshItem.mesh), Block.UPDATE_ALL);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        player.getInventory().placeItemBackInInventory(meshStack);
                    }
                } else {
                    player.sendOverlayMessage(Component.translatable("ftbstuff.wrong_mesh").withStyle(ChatFormatting.GOLD));
                    return InteractionResult.FAIL;
                }
            } else {
                FTBStuffNThings.LOGGER.error("item {} wrongly added added to item tag {} (not a MeshItem)!", stack.getHoverName().getString(), FTBStuffTags.Items.MESHES);
                return InteractionResult.FAIL;
            }
        } else if (stack.getItem() instanceof BucketItem || stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forPlayerInteraction(player, hand)) != null) {
            // null direction here to get the actual fluid handler
            // a non-null direction wouldn't work for basic sluices which don't offer piping automation
            FluidUtil.interactWithFluidHandler(player, hand, pos, sluice.getFluidHandler(null));
        } else {
            // player is trying to insert an item into the sluice
            sluice.getRecipeFor(stack).ifPresent(recipe -> {
                var handler = Objects.requireNonNull(sluice.getItemHandler());
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = handler.insert(ItemResource.of(stack), 1, tx);
                    if (inserted == 1) {
                        sluice.setChanged();
                        sluice.syncItemToClients();
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        tx.commit();
                    }
                }
            });
        }

        return InteractionResult.CONSUME;
    }

    private boolean isMeshCompatibleWith(MeshType type) {
        return builtInRegistryHolder().is(FTBStuffTags.Blocks.allowedMeshes(type));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MESH, PART, HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos offsetPos = context.getClickedPos().relative(context.getHorizontalDirection().getOpposite());
        return context.getLevel().getBlockState(offsetPos).canBeReplaced(context)
                ? this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(PART, Part.MAIN)
                : null;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        Direction direction = state.getValue(HORIZONTAL_FACING);

        // If you break the funnel, reject and break the main block for the player
        if (state.getValue(PART) == Part.FUNNEL) {
            BlockPos endPos = pos.relative(direction.getOpposite());
            if (level.getBlockState(endPos).getBlock() instanceof SluiceBlock) {
                level.destroyBlock(endPos, !level.isClientSide());
                return false;
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Direction direction = state.getValue(HORIZONTAL_FACING);
        BlockPos otherPos = pos.relative(state.getValue(PART) == Part.FUNNEL
                ? direction.getOpposite()
                : direction);

        // Don't act on the funnel
        if (state.getValue(PART) != Part.FUNNEL) {
            level.removeBlock(otherPos, false);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack item) {
        super.setPlacedBy(level, pos, state, entity, item);

        if (!level.isClientSide()) {
            BlockPos otherPos = pos.relative(state.getValue(HORIZONTAL_FACING));
            level.setBlockAndUpdate(otherPos, state.setValue(PART, Part.FUNNEL));
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (!blockState.hasProperty(PART) || blockState.getValue(PART) == Part.FUNNEL) {
            return null;
        }

        return sluiceType.createBlockEntity(blockPos, blockState);
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        if (getProps().energyCost().get() > 0) {
            list.add(ComponentsRegistry.STORED_ENERGY.get());
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    public enum Part implements StringRepresentable {
        MAIN("main"),
        FUNNEL("funnel");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class SluiceBlockItem extends BlockItem {
        public SluiceBlockItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
            boolean isShift = Minecraft.getInstance().hasShiftDown();

            SluiceProperties props = ((SluiceBlock) getBlock()).getProps();

            if (isShift) {
                builder.accept(Component.translatable("ftbstuff.sluice.props.processing_time",
                                Component.literal(props.timeMod().get() + "").withStyle(TextUtil.COLOUR_HIGHLIGHT))
                        .withStyle(ChatFormatting.GRAY)
                );
                builder.accept(Component.translatable("ftbstuff.sluice.props.fluid_usage",
                                Component.literal(props.fluidMod().get() + "").withStyle(TextUtil.COLOUR_HIGHLIGHT))
                        .withStyle(ChatFormatting.GRAY)
                );
                builder.accept(Component.translatable("ftbstuff.sluice.props.tank",
                                Component.literal(props.tankCap().get() + "").withStyle(TextUtil.COLOUR_HIGHLIGHT))
                        .withStyle(ChatFormatting.GRAY)
                );
                builder.accept(Component.translatable("ftbstuff.sluice.props.auto",
                        Component.translatable("ftbstuff.sluice.props.auto.item").withStyle(TextUtil.ofBoolean(props.itemIO().get())),
                        Component.translatable("ftbstuff.sluice.props.auto.fluid").withStyle(TextUtil.ofBoolean(props.fluidIO().get()))
                ).withStyle(ChatFormatting.GRAY));
            } else {
                builder.accept(Component.translatable("ftbstuff.hold_shift").withStyle(ChatFormatting.GRAY));
            }
        }
    }

}
