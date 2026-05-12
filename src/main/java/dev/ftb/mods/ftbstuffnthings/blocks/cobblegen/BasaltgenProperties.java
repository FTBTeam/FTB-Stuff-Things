package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum BasaltgenProperties implements IResourceGenProps {
    STONE(ModConfig.STONE_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Stone::new),
    IRON(ModConfig.IRON_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Iron::new),
    GOLD(ModConfig.GOLD_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Gold::new),
    DIAMOND(ModConfig.DIAMOND_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Diamond::new),
    NETHERITE(ModConfig.NETHERITE_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Netherite::new);

    private final IntValue itemsPerOp;
    private final BiFunction<BlockPos, BlockState, ? extends BasaltgenBlockEntity> beFactory;

    BasaltgenProperties(IntValue itemsPerOp, BiFunction<BlockPos, BlockState, ? extends BasaltgenBlockEntity> beFactory) {
        this.itemsPerOp = itemsPerOp;
        this.beFactory = beFactory;
    }

    @Override
    public int itemsPerOperation() {
        return itemsPerOp.get();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState blockState) {
        return beFactory.apply(pos, blockState);
    }
}
