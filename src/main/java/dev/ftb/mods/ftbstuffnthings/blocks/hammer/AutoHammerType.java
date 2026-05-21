package dev.ftb.mods.ftbstuffnthings.blocks.hammer;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.config.ServerConfig;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.apache.commons.lang3.StringUtils;

public enum AutoHammerType {
    IRON("iron", ServerConfig.IRON_HAMMER_SPEED),
    GOLD("gold", ServerConfig.GOLD_HAMMER_SPEED),
    DIAMOND("diamond", ServerConfig.DIAMOND_HAMMER_SPEED),
    NETHERITE("netherite", ServerConfig.NETHERITE_HAMMER_SPEED);

    private final String materialId;
    private final IntValue hammerSpeed;

    AutoHammerType(String materialId, IntValue hammerSpeed) {
        this.materialId = materialId;
        this.hammerSpeed = hammerSpeed;
    }

    public String getMaterialId() {
        return materialId;
    }

    public int getHammerSpeed() {
        return hammerSpeed.get();
    }

    public String description() {
        return StringUtils.capitalize(materialId) + " Auto-Hammer";
    }

    public DeferredBlock<AutoHammerBlock> getBlock() {
        return BlocksRegistry.getAutoHammer(this);
    }
}
