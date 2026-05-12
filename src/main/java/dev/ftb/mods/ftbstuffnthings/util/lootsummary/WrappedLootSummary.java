package dev.ftb.mods.ftbstuffnthings.util.lootsummary;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public record WrappedLootSummary(Block inputStack, LootSummary summary) {
    public static WrappedLootSummary create(ResourceKey<Block> key, LootSummary summary) {
        return new WrappedLootSummary(BuiltInRegistries.BLOCK.getValueOrThrow(key), summary);
    }
}
