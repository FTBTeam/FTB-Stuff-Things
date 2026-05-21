package dev.ftb.mods.ftbstuffnthings.data;

import dev.ftb.mods.ftbstuffnthings.blocks.SerializableComponentsProvider;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.BarrelBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.CrateBlock;
import dev.ftb.mods.ftbstuffnthings.blocks.lootdroppers.SmallCrateBlock;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LootTablesGenerator extends LootTableProvider {
    public LootTablesGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    @Override
    protected void validate(WritableRegistry<LootTable> tables, ValidationContextSource validationContext, ProblemReporter.Collector problems) {

    }

    private static class BlockLoot extends BlockLootSubProvider {
        private BlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlocksRegistry.BLOCKS.getEntries().stream()
                    .filter(holder -> BuiltInRegistries.ITEM.containsKey(holder.getId()))
                    .map(DeferredHolder::get)
                    .collect(Collectors.toList());
        }

        @Override
        protected void generate() {
            for (var holder: BlocksRegistry.BLOCKS.getEntries()) {
                Block b = holder.get();
                if (b instanceof EntityBlock && BuiltInRegistries.ITEM.containsKey(holder.getId())) {
                    addStandardSerializedDrop(b, holder.getId());
                } else if (b.asItem() != Items.AIR) {
                    if (b instanceof CrateBlock || b instanceof SmallCrateBlock || b instanceof BarrelBlock) {
                        continue;
                    }

                    dropSelf(b);
                }
            }

            // Barrels should drop another loot table instead of themselves
            BlocksRegistry.allBarrels().forEach(barrel -> {
                var name = barrel.getId().getPath();
                add(barrel.get(), LootTable.lootTable().withPool(LootPool.lootPool().name(name)));
            });

            add(BlocksRegistry.CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().name("crate")));
            add(BlocksRegistry.SMALL_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().name("small_crate")));
            add(BlocksRegistry.PULSATING_CRATE.get(), LootTable.lootTable().withPool(LootPool.lootPool().name("pulsating_crate")));
        }

        private void addStandardSerializedDrop(Block block, Identifier blockId) {
            LootPoolSingletonContainer.Builder<?> lootBuilder = LootItem.lootTableItem(block)
                    .apply(CopyNameFunction.copyName(LootContext.BlockEntityTarget.BLOCK_ENTITY));

            if (block instanceof SerializableComponentsProvider scp) {
                List<DataComponentType<?>> components = new ArrayList<>();
                scp.addSerializableComponents(components);
                if (!components.isEmpty()) {
                    CopyComponentsFunction.Builder compBuilder = CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY);
                    components.forEach(compBuilder::include);
                    lootBuilder.apply(compBuilder);
                }
            }

            LootPool.Builder builder = LootPool.lootPool()
                    .name(blockId.getPath())
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(lootBuilder);
            add(block, LootTable.lootTable().withPool(builder));
        }
    }
}
