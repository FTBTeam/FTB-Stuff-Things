package dev.ftb.mods.ftbstuffnthings.blocks.cobblegen;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum BasaltgenProperties implements IResourceGenProps {
    STONE("stone", "cobblestone", ModConfig.STONE_BASALTGEN_AMOUNT),
    IRON("iron", "iron_block", ModConfig.IRON_BASALTGEN_AMOUNT),
    GOLD("gold", "gold_block", ModConfig.GOLD_BASALTGEN_AMOUNT),
    DIAMOND("diamond", "diamond_block", ModConfig.DIAMOND_BASALTGEN_AMOUNT),
    NETHERITE("netherite", "netherite_block", ModConfig.NETHERITE_BASALTGEN_AMOUNT);

    private final String name;
    private final String textureId;
    private final IntValue itemsPerOp;

    BasaltgenProperties(String name, String textureId, IntValue itemsPerOp) {
        this.name = name;
        this.textureId = textureId;
        this.itemsPerOp = itemsPerOp;
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
        return "basalt";
    }

    @Override
    public int itemsPerOperation() {
        return itemsPerOp.get();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState blockState) {
        return new BasaltgenBlockEntity(pos, blockState);
    }
}
