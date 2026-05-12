package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CobblegenBlockEntity extends BaseResourceGenBlockEntity {
    protected CobblegenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, IResourceGenProps props) {
        super(type, pos, blockState, props);
    }

    @Override
    public Item generatedItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected int tickRate() {
        return ModConfig.COBBLEGEN_TICK_RATE.get();
    }

    public static class Stone extends CobblegenBlockEntity {
        public Stone(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.STONE_COBBLEGEN.get(), pos, blockState, CobblegenProperties.STONE);
        }
    }

    public static class Iron extends CobblegenBlockEntity {
        public Iron(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.IRON_COBBLEGEN.get(), pos, blockState, CobblegenProperties.IRON);
        }
    }

    public static class Gold extends CobblegenBlockEntity {
        public Gold(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.GOLD_COBBLEGEN.get(), pos, blockState, CobblegenProperties.GOLD);
        }
    }

    public static class Diamond extends CobblegenBlockEntity {
        public Diamond(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DIAMOND_COBBLEGEN.get(), pos, blockState, CobblegenProperties.DIAMOND);
        }
    }

    public static class Netherite extends CobblegenBlockEntity {
        public Netherite(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.NETHERITE_COBBLEGEN.get(), pos, blockState, CobblegenProperties.NETHERITE);
        }
    }
}
