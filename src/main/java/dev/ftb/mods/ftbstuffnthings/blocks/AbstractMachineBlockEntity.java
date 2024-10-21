package dev.ftb.mods.ftbstuffnthings.blocks;

import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {
    public AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // TODO there's a lot more we could move in here, e.g. progress ticking etc.

    /**
     * {@return data to be sync'd to open GUI's via the container, or null if not applicable}
     */
    @Nullable
    public ContainerData getContainerData() {
        return null;
    }

    @Nullable
    public IItemHandler getItemHandler() {
        return getItemHandler(null);
    }

    @Nullable
    public IFluidHandler getFluidHandler() {
        return getFluidHandler(null);
    }

    @Nullable
    public IEnergyStorage getEnergyHandler() {
        return getEnergyHandler(null);
    }

    @Nullable
    public abstract IItemHandler getItemHandler(@Nullable Direction side);

    @Nullable
    public abstract IFluidHandler getFluidHandler(@Nullable Direction side);

    @Nullable
    public abstract IEnergyStorage getEnergyHandler(@Nullable Direction side);

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }

    public void syncFluidTank(boolean toGui) {
        if (getFluidHandler() instanceof EmittingFluidTank tank) {
            if (toGui) {
                tank.syncToContainers(this);
            } else {
                tank.syncToTrackers(this);
            }
        }
    }

    public void tickClient() {
        getActiveParticle().ifPresent(particle -> {
            if (getBlockState().hasProperty(AbstractMachineBlock.ACTIVE)
                    && getBlockState().getValue(AbstractMachineBlock.ACTIVE)
                    && level.random.nextInt(5) == 0) {
                Vec3 vec = Vec3.upFromBottomCenterOf(getBlockPos(), 1.05);
                level.addParticle(particle, vec.x, vec.y, vec.z, 0, 0, 0);
            }
        });
    }

    protected Optional<ParticleOptions> getActiveParticle() {
        return Optional.of(ParticleTypes.SMOKE);
    }

    public abstract void tickServer();

    public void dropItemContents() {
        IItemHandler handler = getItemHandler();
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                Block.popResource(level, getBlockPos(), handler.getStackInSlot(i));
            }
        }
    }

    protected final void setActive(boolean active) {
        if (getBlockState().hasProperty(AbstractMachineBlock.ACTIVE)) {
            boolean curActive = getBlockState().getValue(AbstractMachineBlock.ACTIVE);
            if (active != curActive) {
                level.setBlock(getBlockPos(), getBlockState().setValue(AbstractMachineBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
            }
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends AbstractMachineBlockEntity> machine) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, machine, AbstractMachineBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, machine, AbstractMachineBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, machine, AbstractMachineBlockEntity::getEnergyHandler);
    }

    public void syncItemFromServer(ItemStack itemStack) {
        // nothing - override in subclasses as needed
    }

    public void syncFluidFromServer(FluidStack fluidStack) {
        // nothing - override in subclasses as needed
    }
}
