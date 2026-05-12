package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.advancements.CustomTrigger;
import dev.ftb.mods.ftbstuffnthings.registry.CriterionTriggerRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AdvancementsGenerator extends AdvancementProvider {
    private static final Identifier BACKGROUND_TEXTURE
            = Identifier.withDefaultNamespace("textures/block/blue_concrete_powder.png");

    public AdvancementsGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider, List.of(new FTBStuffAdvancements()));
    }

    private static class FTBStuffAdvancements implements AdvancementSubProvider {
        private static String id(String s) {
            return FTBStuffNThings.MOD_ID + ":" + s;
        }

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
            AdvancementHolder root = customAdvancement(CriterionTriggerRegistry.FTBSTUFF_ROOT, AdvancementType.TASK, ItemsRegistry.CRATE, true)
                    .save(output, id("root"));

            customAdvancement(CriterionTriggerRegistry.SUPERCHARGED, AdvancementType.TASK, ItemsRegistry.PUMP.asItem(), false)
                    .parent(root)
                    .save(output, id("supercharged"));
        }

        private Advancement.Builder customAdvancement(Supplier<CustomTrigger> triggerSupplier, AdvancementType type, ItemLike itemDisp, boolean stealth) {
            CustomTrigger trigger = triggerSupplier.get();
            String namespace = trigger.getInstance().id().getNamespace();
            String path = trigger.getInstance().id().getPath();
            return Advancement.Builder.advancement()
                    .display(itemDisp,
                            Component.translatable(namespace + ".advancement." + path),
                            Component.translatable(namespace + ".advancement." + path + ".desc"),
                            BACKGROUND_TEXTURE, type, !stealth, !stealth, false)
                    .addCriterion("0", new Criterion<>(trigger, trigger.getInstance()));
        }
    }
}
