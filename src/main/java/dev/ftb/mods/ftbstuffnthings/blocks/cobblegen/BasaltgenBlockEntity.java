package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftbstuffnthings.config.ServerConfig;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class BasaltgenBlockEntity extends BaseResourceGenBlockEntity {
    public BasaltgenBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntitiesRegistry.BASALT_GENERATOR.get(), pos, blockState);
    }

    @Override
    public Item generatedItem() {
        return Items.BASALT;
    }

    @Override
    protected int tickRate() {
        return ServerConfig.BASALTGEN_TICK_RATE.get();
    }
}
