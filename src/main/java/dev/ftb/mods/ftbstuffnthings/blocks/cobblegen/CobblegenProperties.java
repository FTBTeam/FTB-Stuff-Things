package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum CobblegenProperties implements IResourceGenProps {
    STONE(ModConfig.STONE_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Stone::new),
    IRON(ModConfig.IRON_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Iron::new),
    GOLD(ModConfig.GOLD_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Gold::new),
    DIAMOND(ModConfig.DIAMOND_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Diamond::new),
    NETHERITE(ModConfig.NETHERITE_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Netherite::new);

    private final IntValue cobblegenSpeed;
    private final BiFunction<BlockPos, BlockState, ? extends CobblegenBlockEntity> beFactory;

    CobblegenProperties(IntValue cobblegenSpeed, BiFunction<BlockPos, BlockState, ? extends CobblegenBlockEntity> beFactory) {
        this.cobblegenSpeed = cobblegenSpeed;
        this.beFactory = beFactory;
    }

    @Override
    public int itemsPerOperation() {
        return cobblegenSpeed.get();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState blockState) {
        return beFactory.apply(pos, blockState);
    }
}
