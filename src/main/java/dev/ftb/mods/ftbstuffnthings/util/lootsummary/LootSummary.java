package dev.ftb.mods.ftbstuffnthings.util.lootsummary;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LootSummary {
    public static final StreamCodec<RegistryFriendlyByteBuf, LootSummary> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, SummaryEntry.LIST_STREAM_CODEC), LootSummary::entryMap,
            LootSummary::new
    );

    private final Map<String,List<SummaryEntry>> entryMap;
    private final int hashCode;

    private LootSummary(Map<String,List<SummaryEntry>> entryMap) {
        this.entryMap = Collections.unmodifiableMap(entryMap);
        this.hashCode = entryMap.hashCode();
    }

    public static LootSummary forLootTable(LootTable table, LootParams params) {
        Map<String,List<SummaryEntry>> map = new LinkedHashMap<>();

        LootContext ctx = new LootContext.Builder(params).create(Optional.empty());

        expandTable(table, ctx, map, 1f);

        Map<String, List<SummaryEntry>> sortedMap = new LinkedHashMap<>();
        map.forEach((pool, entries) -> sortedMap.put(pool, entries.stream().sorted().toList()));
        return new LootSummary(sortedMap);
    }

    private static void expandTable(LootTable table, LootContext ctx, Map<String, List<SummaryEntry>> map, float weightMult) {
        for (LootPool pool : table.pools) {
            ImmutableList.Builder<SummaryEntry> builder = ImmutableList.builder();
            MutableFloat totalWeight = new MutableFloat(0F);
            for (LootPoolEntryContainer entryContainer : pool.entries) {
                if (entryContainer instanceof LootPoolSingletonContainer s) {
                    totalWeight.add(s.weight);
                } else {
                    entryContainer.expand(ctx, entry -> totalWeight.add(entry.getWeight(1f)));
                }
            }
            for (LootPoolEntryContainer entryContainer : pool.entries) {
                if (entryContainer instanceof NestedLootTable nested) {
                    expandTable(getNestedLootTable(nested, ctx), ctx, map, weightMult * nested.weight / totalWeight.floatValue());
                } else {
                    entryContainer.expand(ctx, entry -> entry.createItemStack(stack ->
                            builder.add(new SummaryEntry(weightMult * entry.getWeight(1.0f) / totalWeight.floatValue(), stack)), ctx)
                    );
                }
            }
            String tblName = Objects.requireNonNullElse(pool.getName(), String.format("pool:%X", pool.hashCode()));
            map.computeIfAbsent(tblName, k -> new ArrayList<>()).addAll(builder.build());
        }
    }

    private static LootTable getNestedLootTable(NestedLootTable nested, LootContext ctx) {
        return nested.contents.map(
                resourceKey -> ctx.getResolver().get(resourceKey).map(Holder::value).orElse(LootTable.EMPTY),
                table -> table
        );
    }

    public Map<String,List<SummaryEntry>> entryMap() {
        return entryMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootSummary that = (LootSummary) o;
        return hashCode == that.hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public record SummaryEntry(float weight, ItemStack stack) implements Comparable<SummaryEntry> {
        public static final StreamCodec<RegistryFriendlyByteBuf, SummaryEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, SummaryEntry::weight,
                ItemStack.OPTIONAL_STREAM_CODEC, SummaryEntry::stack,
                SummaryEntry::new
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, List<SummaryEntry>> LIST_STREAM_CODEC
                = SummaryEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SummaryEntry that = (SummaryEntry) o;
            return weight == that.weight && ItemStack.matches(stack, that.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(weight, ItemStack.hashItemAndComponents(stack), stack.getCount());
        }

        @Override
        public int compareTo(@NotNull LootSummary.SummaryEntry o) {
            return Float.compare(o.weight, weight);
        }
    }
}
