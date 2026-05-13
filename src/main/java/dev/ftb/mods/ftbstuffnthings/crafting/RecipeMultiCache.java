package dev.ftb.mods.ftbstuffnthings.crafting;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class RecipeMultiCache<R extends Recipe<?>> {
    private static final int MAX_CACHE_SIZE = 1024;

    private final Int2ObjectLinkedOpenHashMap<List<RecipeHolder<R>>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

    /**
     * Very much like {@link RecipeCache} but allows a machine state to match multiple recipes. It's up to the
     * {@code recipeFinder} method to return the list of recipes in the appropriate priority order, typically the
     * recipes with the largest set of inputs first.
     *
     * @param recipeFinder (potentially expensive) method to find one or more recipes matching the input state
     * @param hashCodeGenerator method to generate a unique hash code from the input state
     * @return a list of zero or more recipes which match the input state
     */
    public List<RecipeHolder<R>> getCachedRecipes(ServerLevel level, Function<ServerLevel, List<RecipeHolder<R>>> recipeFinder, IntSupplier hashCodeGenerator) {
        int key = hashCodeGenerator.getAsInt();

        if (recipeCache.containsKey(key)) {
            return recipeCache.getAndMoveToFirst(key);
        } else {
            List<RecipeHolder<R>> newRecipes = recipeFinder.apply(level);
            while (recipeCache.size() >= MAX_CACHE_SIZE) {
                recipeCache.removeLast();
            }
            recipeCache.put(key, newRecipes);
            return newRecipes;
        }
    }

    void clear() {
        recipeCache.clear();
    }
}
