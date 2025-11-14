package dev.ftb.mods.ftbstuffnthings.lootmodifiers;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import dev.ftb.mods.ftbstuffnthings.crafting.RecipeCaches;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.CrookRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CrookModifier extends LootModifier {
    public static final Supplier<MapCodec<CrookModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(
            builder -> codecStart(builder).apply(builder, CrookModifier::new))
    );

    public CrookModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> list, LootContext context) {
        ItemStack crook = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || crook == null || blockState == null || !crook.is(FTBStuffTags.Items.CROOKS)) {
            return list;
        }

        List<ItemStack> crookDrops = new ArrayList<>();
        int maxDrops = -1;
        boolean replaceDrops = false;
        for (RecipeHolder<CrookRecipe> holder : RecipeCaches.CROOK.getCachedRecipes(() -> findRecipes(context.getLevel(), blockState), blockState::hashCode)) {
            CrookRecipe recipe = holder.value();
            if (recipe.replaceDrops()) {
                replaceDrops = true;
            }
            if (recipe.getMax() > 0) {
                maxDrops = Math.max(maxDrops, recipe.getMax());
            }
            recipe.getResults().forEach(itemWithChance -> {
                if (context.getRandom().nextDouble() <= itemWithChance.chance()) {
                    crookDrops.add(itemWithChance.item().copy());
                }
            });
        }
        if (!crookDrops.isEmpty()) {
            Collections.shuffle(crookDrops);
            if (replaceDrops) {
                list.clear();
            }
            crookDrops.stream().limit(maxDrops).forEach(list::add);
        }

        return list;
    }

    private List<RecipeHolder<CrookRecipe>> findRecipes(Level level, BlockState blockState) {
        ItemStack input = new ItemStack(blockState.getBlock());

        return level.getRecipeManager().getAllRecipesFor(RecipesRegistry.CROOK_TYPE.get()).stream()
                .filter(holder -> holder.value().getIngredient().test(input))
                .toList();
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
