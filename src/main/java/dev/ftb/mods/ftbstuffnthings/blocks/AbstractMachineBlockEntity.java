package dev.ftb.mods.ftbstuffnthings.blocks;

import dev.ftb.mods.ftbstuffnthings.capabilities.EmittingFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jspecify.annotations.Nullable;

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
    public ResourceHandler<ItemResource> getItemHandler() {
        return getItemHandler(null);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return getFluidHandler(null);
    }

    @Nullable
    public EnergyHandler getEnergyHandler() {
        return getEnergyHandler(null);
    }

    public abstract @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side);

    public abstract @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side);

    public abstract @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side);

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        dropItemContents();
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

    public void tickClient(Level clientLevel) {
        getActiveParticle().ifPresent(particle -> {
            if (getBlockState().hasProperty(AbstractMachineBlock.ACTIVE)
                    && getBlockState().getValue(AbstractMachineBlock.ACTIVE)
                    && clientLevel.getRandom().nextInt(5) == 0) {
                Vec3 vec = Vec3.upFromBottomCenterOf(getBlockPos(), 1.05);
                clientLevel.addParticle(particle, vec.x, vec.y, vec.z, 0, 0, 0);
            }
        });
    }

    protected Optional<ParticleOptions> getActiveParticle() {
        return Optional.of(ParticleTypes.SMOKE);
    }

    public abstract void tickServer(ServerLevel serverLevel);

    protected void dropItemContents() {
        var handler = getItemHandler();
        if (handler != null) {
            for (int i = 0; i < handler.size(); i++) {
                Block.popResource(level, getBlockPos(), ItemUtil.getStack(handler, i));
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
        event.registerBlockEntity(Capabilities.Item.BLOCK, machine, AbstractMachineBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, machine, AbstractMachineBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, machine, AbstractMachineBlockEntity::getEnergyHandler);
    }

    public void syncItemFromServer(ItemStack itemStack) {
        // nothing - override in subclasses as needed
    }

    public void syncFluidFromServer(FluidStack fluidStack) {
        // nothing - override in subclasses as needed
    }
}
