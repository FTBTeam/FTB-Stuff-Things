package dev.ftb.mods.ftbstuffnthings.lootmodifiers;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.function.Supplier;

public class HammerModifier extends LootModifier {
    public static final Supplier<MapCodec<HammerModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(builder -> codecStart(builder).apply(builder, HammerModifier::new)));

    public HammerModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> list, LootContext context) {
        ItemStack hammer = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || hammer == null || blockState == null || !hammer.is(FTBStuffTags.Items.HAMMERS)) {
            return list;
        }

        ItemStack stack = new ItemStack(blockState.getBlock());
        AutoHammerBlockEntity.getRecipeForStack(context.getLevel(), stack).ifPresent(recipe -> {
            list.clear();
            list.addAll(recipe.getResults().stream().map(ItemStack::copy).toList());
        });

        return list;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
