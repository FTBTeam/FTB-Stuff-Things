package dev.ftb.mods.ftbstuffnthings.blocks.tube;

import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class TubeBlockEntity extends BlockEntity implements ITubeConnectable {
    public static final ModelProperty<Integer> CONNECTION_PROPERTY = new ModelProperty<>();

    private int sidesClosed = 0;    // toggleable by player clicking with empty hand
    private int sidesConnected = 0; // updated when tube placed or neighbour updates

    public TubeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesRegistry.TUBE.get(), blockPos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        sidesClosed = input.getIntOr("sides_closed", 0);
        sidesConnected = input.getIntOr("sides_connected", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (sidesClosed != 0) output.putInt("sides_closed", sidesClosed);
        if (sidesConnected != 0) output.putInt("sides_connected", sidesConnected);
    }

    public int getShapeCacheKey() {
        return Objects.hash(sidesClosed, sidesConnected);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // server side, chunk sending
        TagValueOutput t = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        saveAdditional(t);
        return t.buildResult();
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);

        requestModelDataUpdate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // server side, block update
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        super.onDataPacket(net, valueInput);

        requestModelDataUpdate();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(CONNECTION_PROPERTY, sidesConnected)
                .build();
    }

    public boolean isSideClosed(Direction dir) {
        return DirectionUtil.getDirectionBit(sidesClosed, dir);
    }

    public void setSideClosed(Direction dir, boolean closed) {
        if (!level.isClientSide()) {
            int prevSidesClosed = sidesClosed;
            sidesClosed = DirectionUtil.setDirectionBit(sidesClosed, dir, closed);
            if (sidesClosed != prevSidesClosed) {
                setChanged();
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public boolean isSideConnected(Direction dir) {
        return DirectionUtil.getDirectionBit(sidesConnected, dir);
    }

    public boolean updateSide(Direction dir, boolean updateNow) {
        int prevSidesConnected = sidesConnected;
        BlockPos pos1 = getBlockPos().relative(dir);
        Direction dir1 = dir.getOpposite();
        boolean connectable = !isSideClosed(dir) && ITubeConnectable.canConnect(level, pos1, dir1);
        sidesConnected = DirectionUtil.setDirectionBit(sidesConnected, dir, connectable);

        if (sidesConnected != prevSidesConnected) {
            if (updateNow) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
            setChanged();
            return true;
        }
        return false;
    }

    public void updateConnectedSides() {
        boolean changed = false;
        for (Direction dir : DirectionUtil.VALUES) {
            if (updateSide(dir, false)) {
                changed = true;
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isSideTubeConnectable(Direction side) {
        return !isSideClosed(side);
    }
}
