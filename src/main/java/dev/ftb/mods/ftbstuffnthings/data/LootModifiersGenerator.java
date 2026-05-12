package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.lootmodifiers.CrookModifier;
import dev.ftb.mods.ftbstuffnthings.lootmodifiers.HammerModifier;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class LootModifiersGenerator extends GlobalLootModifierProvider {
    public LootModifiersGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, FTBStuffNThings.MOD_ID);
    }

    @Override
    protected void start() {
        add("crook_loot_modifier", new CrookModifier(new LootItemCondition[] {
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), FTBStuffTags.Items.CROOKS)).build()
        }, 1000));

        add("hammer_loot_modifier", new HammerModifier(new LootItemCondition[] {
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), FTBStuffTags.Items.HAMMERS)).build()
        }, 1000));
    }
}
