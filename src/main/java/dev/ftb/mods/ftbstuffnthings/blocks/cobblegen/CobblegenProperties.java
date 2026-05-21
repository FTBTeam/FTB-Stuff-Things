package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum CobblegenProperties implements IResourceGenProps {
    STONE("stone", "cobblestone", ServerConfig.STONE_COBBLEGEN_AMOUNT),
    IRON("iron", "iron_block", ServerConfig.IRON_COBBLEGEN_AMOUNT),
    GOLD("gold", "gold_block", ServerConfig.GOLD_COBBLEGEN_AMOUNT),
    DIAMOND("diamond", "diamond_block", ServerConfig.DIAMOND_COBBLEGEN_AMOUNT),
    NETHERITE("netherite", "netherite_block", ServerConfig.NETHERITE_COBBLEGEN_AMOUNT);

    private final String name;
    private final String textureId;
    private final IntValue cobblegenSpeed;

    CobblegenProperties(String name, String textureId, IntValue cobblegenSpeed) {
        this.name = name;
        this.textureId = textureId;
        this.cobblegenSpeed = cobblegenSpeed;
    }

    @Override
    public String getName() {
        return name;
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
        return new CobblegenBlockEntity(pos, blockState);
    }
}
