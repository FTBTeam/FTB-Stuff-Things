package dev.ftb.mods.ftbstuffnthings.blocks.basaltgen;


import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftbstuffnthings.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum BasaltGeneratorProperties {

    STONE(Config.STONE_BASALTGEN_SPEED, BasaltGeneratorBlockEntity.Stone::new),
    IRON(Config.IRON_BASALTGEN_SPEED, BasaltGeneratorBlockEntity.Iron::new),
    GOLD(Config.GOLD_BASALTGEN_SPEED, BasaltGeneratorBlockEntity.Gold::new),
    DIAMOND(Config.DIAMOND_BASALTGEN_SPEED, BasaltGeneratorBlockEntity.Diamond::new),
    NETHERITE(Config.NETHERITE_BASALTGEN_SPEED, BasaltGeneratorBlockEntity.Netherite::new);

    private final IntValue genSpeed;
    private final BiFunction<BlockPos, BlockState, ? extends  BasaltGeneratorBlockEntity> beFactory;

    BasaltGeneratorProperties(IntValue speed, BiFunction<BlockPos, BlockState, ? extends  BasaltGeneratorBlockEntity> beFactory) {
        this.genSpeed = speed;
        this.beFactory = beFactory;
    }

    public int getGeneratorSpeed() {
        return genSpeed.get();
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return beFactory.apply(pos, state);
    }
}
