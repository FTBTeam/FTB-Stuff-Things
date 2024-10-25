package dev.ftb.mods.ftbstuffnthings.capabilities;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineMenu;
import dev.ftb.mods.ftbstuffnthings.network.SyncDisplayFluidPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EmittingFluidTank extends FluidTank {
    private final Consumer<EmittingFluidTank> onChange;
    private boolean syncAllObservers;
    private final Set<ServerPlayer> toSync = Collections.newSetFromMap(new WeakHashMap<>());

    public EmittingFluidTank(int capacity, Consumer<EmittingFluidTank> onChange) {
        super(capacity);
        this.onChange = onChange;
    }

    public EmittingFluidTank(int capacity, Predicate<FluidStack> validator, Consumer<EmittingFluidTank> onChange) {
        super(capacity, validator);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        onChange.accept(this);
        needSync();
    }

    public void needSync(ServerPlayer... players) {
        if (players.length == 0) {
            syncAllObservers = true;
        } else {
            toSync.addAll(Arrays.asList(players));
        }
    }

    public void syncToContainers(BlockEntity blockEntity) {
        if (syncAllObservers) {
            blockEntity.getLevel().getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> p.containerMenu instanceof AbstractMachineMenu<?> prov && prov.getBlockEntity() == blockEntity)
                    .forEach(toSync::add);
        }
        if (!toSync.isEmpty()) {
            SyncDisplayFluidPacket syncDisplayFluidPacket = new SyncDisplayFluidPacket(blockEntity.getBlockPos(), fluid);
            toSync.forEach(p -> PacketDistributor.sendToPlayer(p, syncDisplayFluidPacket));
            toSync.clear();
        }
        syncAllObservers = false;
    }

    public void syncToTrackers(AbstractMachineBlockEntity machine) {
        if (machine.getLevel() instanceof ServerLevel sl) {
            SyncDisplayFluidPacket packet = new SyncDisplayFluidPacket(machine.getBlockPos(), fluid);
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(machine.getBlockPos()), packet);
        }
    }
}
