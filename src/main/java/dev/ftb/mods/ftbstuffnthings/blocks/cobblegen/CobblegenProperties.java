package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public enum CobblegenProperties implements IResourceGenProps {
    STONE("cobblestone", ModConfig.STONE_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Stone::new),
    IRON("iron_block", ModConfig.IRON_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Iron::new),
    GOLD("gold_block", ModConfig.GOLD_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Gold::new),
    DIAMOND("diamond_block", ModConfig.DIAMOND_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Diamond::new),
    NETHERITE("netherite_block", ModConfig.NETHERITE_COBBLEGEN_AMOUNT, CobblegenBlockEntity.Netherite::new);

    private final String textureId;
    private final IntValue cobblegenSpeed;
    private final BiFunction<BlockPos, BlockState, ? extends CobblegenBlockEntity> beFactory;

    CobblegenProperties(String textureId, IntValue cobblegenSpeed, BiFunction<BlockPos, BlockState, ? extends CobblegenBlockEntity> beFactory) {
        this.textureId = textureId;
        this.cobblegenSpeed = cobblegenSpeed;
        this.beFactory = beFactory;
    }

    @Override
    public String textureId() {
        return textureId;
    }

    @Override
    public String resourceId() {
        return "cobblestone";
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
