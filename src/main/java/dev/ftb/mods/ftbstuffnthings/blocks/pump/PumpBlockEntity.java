package dev.ftb.mods.ftbstuffnthings.blocks.pump;

import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceBlockEntity;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PumpBlockEntity extends AbstractMachineBlockEntity {
    private static final int TICK_RATE = 20;

    // all but down
    private static final List<Direction> OUTPUT_DIRS = List.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    private int timeLeft = 0;
    private int tickCounter = 0;

    public boolean creative = false;
    public Fluid creativeFluid = Fluids.WATER;
    public Item creativeItem = null;

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<FluidResource>, Direction>> capabilityCacheMap = new EnumMap<>(Direction.class);

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.PUMP.get(), pos, state);
    }

    @Override
    public void tickServer(ServerLevel serverLevel) {
        if (timeLeft <= 0 && !creative) {
            return;
        }

        FluidState fluidState = serverLevel.getBlockState(getBlockPos().below()).getFluidState();

        // No valid fluid source
        if (!creative && fluidState.isEmpty() || !fluidState.isSource() || !fluidState.is(FluidTags.WATER)) {
            return;
        }

        if (++tickCounter >= TICK_RATE) {
            runOneCycle(serverLevel);
        }
    }

    @Override
    protected Optional<ParticleOptions> getActiveParticle() {
        return Optional.of(ParticleTypes.SPLASH);
    }

    private void runOneCycle(ServerLevel serverLevel) {
        tickCounter = 0;

        int totalFilled = 0;
        for (Direction dir : OUTPUT_DIRS) {
            var fluidCache = capabilityCacheMap.computeIfAbsent(dir, _ ->
                    BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, serverLevel, getBlockPos().relative(dir), dir.getOpposite())
            );
            var handler = fluidCache.getCapability();
            if (handler != null) {
                try (Transaction tx = Transaction.openRoot()) {
                    totalFilled += handler.insert(FluidResource.of(Fluids.WATER), ModConfig.PUMP_FLUID_TRANSFER.get(), tx);
                    handleCreateItemInsertion(handler, tx);
                    tx.commit();
                }
            }
        }

        if (totalFilled > 0) {
            // one unit of pump charge transfers 50mB of water
            if (!creative) {
                timeLeft = Math.max(0, timeLeft - totalFilled / 50);
            }
            updatePumpProgress();
            if (timeLeft == 0) {
                level.setBlock(getBlockPos(), getBlockState()
                        .setValue(AbstractMachineBlock.ACTIVE, false)
                        .setValue(PumpBlock.PROGRESS, PumpBlock.Progress.ZERO), AbstractMachineBlock.UPDATE_ALL);
            }
        }
    }

    private void handleCreateItemInsertion(ResourceHandler<FluidResource> handler, Transaction tx) {
        if (creative && creativeItem != null && handler instanceof SluiceBlockEntity.SluiceFluidTank sluiceFluidTank) {
            // null side bypasses item IO ability checking
            var itemHandler = sluiceFluidTank.getOwner().getItemHandler(null);
            if (itemHandler != null) {
                itemHandler.insert(ItemResource.of(creativeItem), 1, tx);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("time_left", this.timeLeft);
        output.putBoolean("is_creative", this.creative);
        if (creativeFluid != Fluids.WATER) {
            output.store("creative_fluid", FluidStack.FLUID_HOLDER_CODEC, BuiltInRegistries.FLUID.wrapAsHolder(creativeFluid));
        }
        if (creativeItem != null) {
            output.store("creative_item", Item.CODEC, BuiltInRegistries.ITEM.wrapAsHolder(creativeItem));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        timeLeft = input.getIntOr("time_left", 0);
        creative = input.getBooleanOr("is_creative", false);
        creativeFluid = input.read("creative_fluid", FluidStack.FLUID_HOLDER_CODEC)
                .map(Holder::value).orElse(Fluids.WATER);
        creativeItem = input.read("creative_item", Item.CODEC)
                .map(Holder::value).orElse(null);

    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
//        var data = new CompoundTag();
//        this.saveAdditional(data, registries);
//        return data;
        return new CompoundTag();
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
//        super.handleUpdateTag(input);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
//        loadAdditional(valueInput);
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return null;
    }

    @Override
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return null;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public boolean windUp() {
        int maxCharge = ModConfig.PUMP_MAX_CHARGE.get();

        if (timeLeft >= maxCharge) {
            return false;
        }
        timeLeft = Math.min(maxCharge, timeLeft + ModConfig.PUMP_CHARGEUP_AMOUNT.get());

        updatePumpProgress();
        setChanged();

        return true;
    }

    private void updatePumpProgress() {
        if (!getBlockState().getValue(AbstractMachineBlock.ACTIVE) && timeLeft > 0) {
            setPumpProgress(PumpBlock.Progress.ZERO);
        } else {
            PumpBlock.Progress value = getBlockState().getValue(PumpBlock.PROGRESS);
            if (timeLeft < 1200 && value != PumpBlock.Progress.TWENTY) {
                setPumpProgress(PumpBlock.Progress.TWENTY);
            } else if (timeLeft >= 1200 && timeLeft < 2400 && value != PumpBlock.Progress.FORTY) {
                setPumpProgress(PumpBlock.Progress.FORTY);
            } else if (timeLeft >= 2400 && timeLeft < 3600 && value != PumpBlock.Progress.SIXTY) {
                setPumpProgress(PumpBlock.Progress.SIXTY);
            } else if (timeLeft >= 3600 && timeLeft < 4800 && value != PumpBlock.Progress.EIGHTY) {
                setPumpProgress(PumpBlock.Progress.EIGHTY);
            } else if (timeLeft >= 4800 && timeLeft < 5500 && value != PumpBlock.Progress.HUNDRED) {
                setPumpProgress(PumpBlock.Progress.HUNDRED);
            }
        }
    }

    private void setPumpProgress(PumpBlock.Progress progress) {
        level.setBlock(getBlockPos(), getBlockState()
                        .setValue(AbstractMachineBlock.ACTIVE, true)
                        .setValue(PumpBlock.PROGRESS, progress),
                Block.UPDATE_ALL);
    }
}
