package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum BasaltgenProperties implements IResourceGenProps {
    STONE("cobblestone", ModConfig.STONE_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Stone::new),
    IRON("iron_block", ModConfig.IRON_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Iron::new),
    GOLD("gold_block", ModConfig.GOLD_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Gold::new),
    DIAMOND("diamond_block", ModConfig.DIAMOND_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Diamond::new),
    NETHERITE("netherite_block", ModConfig.NETHERITE_BASALTGEN_AMOUNT, BasaltgenBlockEntity.Netherite::new);

    private final String id;
    private final IntValue itemsPerOp;
    private final BiFunction<BlockPos, BlockState, ? extends BasaltgenBlockEntity> beFactory;

    BasaltgenProperties(String id, IntValue itemsPerOp, BiFunction<BlockPos, BlockState, ? extends BasaltgenBlockEntity> beFactory) {
        this.id = id;
        this.itemsPerOp = itemsPerOp;
        this.beFactory = beFactory;
    }

    @Override
    public String textureId() {
        return id;
    }

    @Override
    public String resourceId() {
        return "basalt";
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
