package dev.ftb.mods.ftbstuffnthings.util.lootsummary;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class LootSummaryCollection {
    private static final LootSummaryCollection EMPTY_COLLECTION = new LootSummaryCollection(Map.of(), Map.of());
    private static final Map<Integer, List<WrappedLootSummary>> item2summaryCache = new HashMap<>();

    private static LootSummaryCollection CLIENT_SUMMARY = EMPTY_COLLECTION;

    private static final StreamCodec<FriendlyByteBuf, ResourceKey<?>> RESOURCE_KEY_STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeIdentifier(value.registry());
                buffer.writeIdentifier(value.identifier());
            },
            buffer -> createKey(buffer.readIdentifier(), buffer.readIdentifier())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LootSummaryCollection> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, LootSummary.STREAM_CODEC), v -> v.summaries,
            ByteBufCodecs.map(HashMap::new, ResourceKey.streamCodec(Registries.BLOCK), ByteBufCodecs.INT), v -> v.item2summary,
            LootSummaryCollection::new
    );

    // multiple items may have exactly the same loot table; avoid duplicated data by storing by the summary hashcode
    private final Map<Integer, LootSummary> summaries;
    private final Map<ResourceKey<Block>, Integer> item2summary;

    private LootSummaryCollection(Map<Integer, LootSummary> summaries, Map<ResourceKey<Block>, Integer> item2summary) {
        this.summaries = summaries;
        this.item2summary = item2summary;
    }

    public LootSummaryCollection() {
        this(new HashMap<>(), new HashMap<>());
    }

    public void addEntry(ResourceKey<Block> key, Identifier tableId, LootParams lootParams) {
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);
        LootTable lootTable = lootParams.getLevel().getServer().reloadableRegistries().getLootTable(tableKey);
        LootSummary summary = LootSummary.forLootTable(lootTable, lootParams);
        summaries.put(summary.hashCode(), summary);
        item2summary.put(key, summary.hashCode());
    }

    private static LootParams makeBlockParams(ServerPlayer serverPlayer, BlockState state) {
        return new LootParams.Builder(serverPlayer.level())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, Items.DIAMOND_PICKAXE.getDefaultInstance())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, serverPlayer)
                .create(LootContextParamSets.BLOCK);
    }
    public static void syncFromServer(LootSummaryCollection summary) {
        CLIENT_SUMMARY = summary;
        item2summaryCache.clear();
        FTBStuffNThings.LOGGER.info("received loot summary data from server: {} items, {} unique tables",
                CLIENT_SUMMARY.item2summary.size(), CLIENT_SUMMARY.summaries.size());
    }

    public static LootSummaryCollection getClientSummary() {
        return CLIENT_SUMMARY;
    }

    private static <T> ResourceKey<T> createKey(Identifier reg, Identifier loc) {
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(reg);
        return ResourceKey.create(key, loc);
    }

    public Optional<LootSummary> getLootSummaryForInput(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).map(this::getLootSummaryForInput);
    }

    private LootSummary getLootSummaryForInput(ResourceKey<Block> key) {
        return item2summary.containsKey(key) ? summaries.get(item2summary.get(key)) : null;
    }

    public List<WrappedLootSummary> getLootSummariesForOutput(ItemStack stack) {
        int hash = ItemStack.hashItemAndComponents(stack);
        return item2summaryCache.computeIfAbsent(hash, k -> {
            List<WrappedLootSummary> res = new ArrayList<>();
            for (var key : item2summary.keySet()) {
                LootSummary summary = summaries.get(item2summary.get(key));
                for (var e : summary.entryMap().entrySet()) {
                    for (LootSummary.SummaryEntry entry : e.getValue()) {
                        if (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, entry.stack())) {
                            res.add(WrappedLootSummary.create(key, summary));
                        }
                    }
                }
            }
            return List.copyOf(res);
        });
    }
}
