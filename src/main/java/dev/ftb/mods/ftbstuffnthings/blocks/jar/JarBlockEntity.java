package dev.ftb.mods.ftbstuffnthings.blocks.jar;

import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class JarBlockEntity extends BlockEntity {
    private final FluidStacksResourceHandler tank = new JarFluidTank();

    public JarBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesRegistry.JAR.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Tank", tank);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        saveAdditional(output);
        return output.buildResult();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return tank;
    }

    public ResourceHandler<FluidResource> getTank() {
        return tank;
    }

    public int getComparatorSignal() {
        return tank.getAmountAsInt(0) * 15 / tank.getCapacityAsInt(0, FluidResource.EMPTY);
    }

    public void onRightClick(Player player, InteractionHand hand, ItemStack item) {
        FluidUtil.interactWithFluidHandler(player, hand, getBlockPos(), tank);

        if (!level.isClientSide()) {
            if (tank.getResource(0).isEmpty()) {
                player.sendOverlayMessage(Component.translatable("ftblibrary.empty"));
            } else {
                player.sendOverlayMessage(Component.translatable("ftblibrary.mb", tank.getAmountAsInt(0), tank.getResource(0).getHoverName()));
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);

        FluidStack fs = componentInput.getOrDefault(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.EMPTY).copy();
        tank.set(0, FluidResource.of(fs), fs.amount());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        components.set(ComponentsRegistry.STORED_FLUID, SimpleFluidContent.copyOf(FluidUtil.getStack(tank, 0)));
    }

    private class JarFluidTank extends FluidStacksResourceHandler {
        public JarFluidTank() {
            super(1, 8000);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }
}
