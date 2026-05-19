package dev.ftb.mods.ftbstuffnthings.blocks.hammer;

import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import dev.ftb.mods.ftbstuffnthings.items.HammerItem;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiFunction;

public enum AutoHammerProperties {
    IRON("iron", ItemsRegistry.IRON_HAMMER, ModConfig.IRON_HAMMER_SPEED, AutoHammerBlockEntity.Iron::new),
    GOLD("gold", ItemsRegistry.GOLD_HAMMER , ModConfig.GOLD_HAMMER_SPEED, AutoHammerBlockEntity.Gold::new),
    DIAMOND("diamond", ItemsRegistry.DIAMOND_HAMMER, ModConfig.DIAMOND_HAMMER_SPEED, AutoHammerBlockEntity.Diamond::new),
    NETHERITE("netherite", ItemsRegistry.NETHERITE_HAMMER, ModConfig.NETHERITE_HAMMER_SPEED, AutoHammerBlockEntity.Netherite::new);

    private final String materialId;
    private final DeferredItem<HammerItem> hammerItem;
    private final IntValue hammerSpeed;
    private final BiFunction<BlockPos, BlockState, ? extends AutoHammerBlockEntity> beFactory;

    AutoHammerProperties(String materialId, DeferredItem<HammerItem> hammerItem, IntValue hammerSpeed, BiFunction<BlockPos, BlockState, ? extends AutoHammerBlockEntity> beFactory) {
        this.materialId = materialId;
        this.hammerItem = hammerItem;
        this.hammerSpeed = hammerSpeed;
        this.beFactory = beFactory;
    }

    public String getMaterialId() {
        return materialId;
    }

    public Item getHammerItem() {
        return hammerItem.get();
    }

    public int getHammerSpeed() {
        return hammerSpeed.get();
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState blockState) {
        return beFactory.apply(pos, blockState);
    }
}
