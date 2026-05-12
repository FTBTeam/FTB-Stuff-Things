package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BasaltgenBlockEntity extends BaseResourceGenBlockEntity {
    protected BasaltgenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, IResourceGenProps props) {
        super(type, pos, blockState, props);
    }

    @Override
    public Item generatedItem() {
        return Items.BASALT;
    }

    @Override
    protected int tickRate() {
        return ModConfig.BASALTGEN_TICK_RATE.get();
    }

    public static class Stone extends BasaltgenBlockEntity {
        public Stone(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.STONE_BASALT_GENERATOR.get(), pos, blockState, BasaltgenProperties.STONE);
        }
    }

    public static class Iron extends BasaltgenBlockEntity {
        public Iron(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.IRON_BASALT_GENERATOR.get(), pos, blockState, BasaltgenProperties.IRON);
        }
    }

    public static class Gold extends BasaltgenBlockEntity {
        public Gold(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.GOLD_BASALT_GENERATOR.get(), pos, blockState, BasaltgenProperties.GOLD);
        }
    }

    public static class Diamond extends BasaltgenBlockEntity {
        public Diamond(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.DIAMOND_BASALT_GENERATOR.get(), pos, blockState, BasaltgenProperties.DIAMOND);
        }
    }

    public static class Netherite extends BasaltgenBlockEntity {
        public Netherite(BlockPos pos, BlockState blockState) {
            super(BlockEntitiesRegistry.NETHERITE_BASALT_GENERATOR.get(), pos, blockState, BasaltgenProperties.NETHERITE);
        }
    }
}
