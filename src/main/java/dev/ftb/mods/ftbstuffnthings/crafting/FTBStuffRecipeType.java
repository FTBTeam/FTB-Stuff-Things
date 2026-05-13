package dev.ftb.mods.ftbstuffnthings.crafting;

import dev.ftb.mods.ftbstuffnthings.client.FTBStuffNThingsClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class FTBStuffRecipeType<T extends Recipe<NoInventory>> implements RecipeType<T> {
    private final String name;

    public FTBStuffRecipeType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Stream<RecipeHolder<T>> streamRecipes(Level level) {
        RecipeMap map = level instanceof ServerLevel serverLevel ?
                serverLevel.getServer().getRecipeManager().recipeMap() :
                FTBStuffNThingsClient.getInstance().getRecipeMap();
        return map.getRecipesFor(this, NoInventory.INSTANCE, level);
    }
}
