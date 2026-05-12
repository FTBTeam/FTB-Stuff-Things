package dev.ftb.mods.ftbstuffnthings.blocks.supercooler;

import dev.ftb.mods.ftbstuffnthings.blocks.AbstractMachineBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.SerializableComponentsProvider;
import dev.ftb.mods.ftbstuffnthings.registry.ComponentsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SuperCoolerBlock extends AbstractMachineBlock implements SerializableComponentsProvider {
    public SuperCoolerBlock() {
        super(defaultMachineProps());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperCoolerBlockEntity(pos, state);
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        list.add(ComponentsRegistry.STORED_FLUID.get());
        list.add(ComponentsRegistry.STORED_ENERGY.get());
    }
}
