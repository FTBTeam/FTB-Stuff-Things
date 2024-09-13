package dev.ftb.mods.ftbobb.blocks;

import dev.ftb.mods.ftbobb.registry.TilesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PumpBlockEntity extends BlockEntity {
    public int timeLeft = 0;

    private final int checkInterval = 50;
    private int checkTimeout = 0;
    private boolean foundValidBlocks = false;
    private int lastTick = 0;
    boolean creative = false;
    Fluid creativeFluid = Fluids.WATER;
    Item creativeItem = null;

    private Set<BlockPos> targetBlocks = new HashSet<>();

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(TilesRegistry.PUMP.get(), pos, state);
    }

//    @Override
//    public static void tick() {
//        if (this.level == null || (this.timeLeft <= 0 && !this.creative)) {
//            return;
//        }
//
//        FluidState fluidState = level.getBlockState(this.getBlockPos().below()).getFluidState();
//
//        // No valid fluid source
//        if (!this.creative && fluidState.isEmpty() || !fluidState.isSource() || !fluidState.is(FluidTags.WATER)) {
//            return;
//        }
//
//        // Try and find a valid block;
//        if (!foundValidBlocks) {
//            // Time to check
//            if (checkTimeout > checkInterval) {
//                for (Direction direction : Direction.values()) {
//                    BlockEntity blockEntity = level.getBlockEntity(this.getBlockPos().relative(direction));
//                    if (blockEntity instanceof SluiceBlockEntity) {
//                        this.targetBlocks.add(this.getBlockPos().relative(direction));
//                        this.foundValidBlocks = true;
//                    }
//                }
//
//                checkTimeout = 0;
//            }
//
//            checkTimeout ++;
//            return;
//        }
//
//        // Just do it
//        if (this.creative) {
//            for (BlockPos pos : this.getTargetBlocks()) {
//                BlockEntity blockEntity = level.getBlockEntity(pos);
//                if (blockEntity == null) {
//                    return;
//                }
//
//                this.provideFluidToSluice(blockEntity);
//                if (this.creativeItem != null && blockEntity instanceof SluiceBlockEntity) {
//                    ItemsHandler inventory = ((SluiceBlockEntity) blockEntity).inventory;
//                    if (inventory.getStackInSlot(0).isEmpty()) {
//                        inventory.internalInsert(0, new ItemStack(this.creativeItem, 1), false);
//                    }
//                }
//            }
//
//            // Don't do anything else, creative means creative
//            return;
//        }
//
//        this.lastTick ++;
//        if (this.lastTick < 20) {
//            return;
//        }
//
//        this.lastTick = 0;
//
//        boolean wasUsed = false;
//        for (BlockPos pos : this.getTargetBlocks()) {
//            BlockEntity blockEntity = level.getBlockEntity(pos);
//            if (blockEntity == null) {
//                return;
//            }
//
//            boolean filled = this.provideFluidToSluice(blockEntity);
//            if (!wasUsed && filled) {
//                wasUsed = true;
//            }
//        }
//
//        if (!wasUsed) {
//            return;
//        }
//
//        this.timeLeft -= 20;
//        PumpBlock.computeStateForProgress(this.getBlockState(), this.getBlockPos(), this.level, this.timeLeft);
//        if (this.timeLeft < 0) {
//            this.timeLeft = 0;
//
//            level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PumpBlock.ON_OFF, false).setValue(PumpBlock.PROGRESS, PumpBlock.Progress.ZERO), 3);
//        }
//    }

    private boolean provideFluidToSluice(BlockEntity blockEntity) {
        boolean didFill = false;

        // It's gone! Poof
        if (!(blockEntity instanceof SluiceBlockEntity)) {
            this.foundValidBlocks = false; // flag to update cache
            this.targetBlocks.clear();
            return false;
        }

        // Give it water!
//        FluidCap tank = ((SluiceBlockEntity) blockEntity).tank;
//        if (tank.getFluidAmount() < tank.getCapacity()) {
//            int i = tank.internalFill(new FluidStack(this.creative ? this.creativeFluid : Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
//            if (i > 0) {
//                didFill = true;
//            }
//            blockEntity.setChanged();
//        }

        return didFill;
    }

    public Set<BlockPos> getTargetBlocks() {
        return this.targetBlocks;
    }

//    @Override
//    public CompoundTag save(CompoundTag compound) {
//        compound.putInt("time_left", this.timeLeft);
//        compound.putBoolean("is_creative", this.creative);
//
//        if (this.creativeFluid != Fluids.WATER) {
//            compound.putString("creative_fluid", Objects.requireNonNull(this.creativeFluid.getRegistryName()).toString());
//        }
//
//        if (this.creativeItem != null) {
//            compound.putString("creative_item", Objects.requireNonNull(this.creativeItem.getRegistryName()).toString());
//        }
//
//        return super.save(compound);
//    }
//
//    @Override
//    public void load(BlockState state, CompoundTag compound) {
//        this.timeLeft = compound.getInt("time_left");
//        this.creative = compound.getBoolean("is_creative");
//
//        if (compound.contains("creative_fluid")) {
//            Fluid creativeFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(compound.getString("creative_fluid")));
//            this.creativeFluid = creativeFluid == null ? Fluids.WATER : creativeFluid;
//        }
//
//        if (compound.contains("creative_item")) {
//            this.creativeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("creative_item")));
//        }
//
//        super.load(state, compound);
//    }

//    @Override
//    public AABB getRenderBoundingBox() {
//        return new AABB(this.getBlockPos()).inflate(1);
//    }

//    @Override
//    public CompoundTag getUpdateTag() {
//        return this.save(new CompoundTag());
//    }

//    @Override
//    public void handleUpdateTag(BlockState state, CompoundTag tag) {
//        this.load(state, tag);
//    }

//    @Nullable
//    @Override
//    public ClientboundBlockEntityDataPacket getUpdatePacket() {
//        return new ClientboundBlockEntityDataPacket(this.getBlockPos(), 0, this.save(new CompoundTag()));
//    }

//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
//        this.load(this.getBlockState(), pkt.getTag());
//    }
}
