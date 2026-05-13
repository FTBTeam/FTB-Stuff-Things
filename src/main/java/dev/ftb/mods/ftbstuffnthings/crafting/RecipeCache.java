package dev.ftb.mods.ftbstuffnthings.crafting;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class RecipeCache<R extends Recipe<?>> {
    private static final int MAX_CACHE_SIZE = 1024;

    private final Int2ObjectLinkedOpenHashMap<Optional<RecipeHolder<R>>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

    /**
     * Find a recipe, and if found, cache it. This method takes two method references, both of which methods have
     * access to the machine's input state, i.e. the contents of the machine which will be used to look up recipes
     * which apply to those contents. Typically, these are item and fluid inventories, but other conditions may also
     * be considered, e.g. a machine's current "temperature" based on adjacent blocks (think: Tempered Jars).
     *
     * @param recipeFinder a method to find a recipe, which could be an expensive search, since it needs to be linear
     *                     along all known recipes of this type; this method is only called if there is no cache hit
     * @param hashCodeGenerator a method to generate a hashcode from the state of the machine which are pertinent to
     *                          finding the recipe; this hashcode is used as the key to quickly finding the recipe in
     *                          future
     * @return a cached recipe, or {@code Optional.empty()} if no recipe exists for the generated hashcode
     */
    public Optional<RecipeHolder<R>> getCachedRecipe(Level level, Function<Level, Optional<RecipeHolder<R>>> recipeFinder, IntSupplier hashCodeGenerator) {
        int key = hashCodeGenerator.getAsInt();

        if (recipeCache.containsKey(key)) {
            return recipeCache.getAndMoveToFirst(key);
        } else {
            Optional<RecipeHolder<R>> newRecipe = recipeFinder.apply(level);
            while (recipeCache.size() >= MAX_CACHE_SIZE) {
                recipeCache.removeLast();
            }
            recipeCache.put(key, newRecipe);
            return newRecipe;
        }
    }

    void clear() {
        recipeCache.clear();
    }
}
