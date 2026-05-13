package dev.ftb.mods.ftbstuffnthings.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * Common base class for "machine" recipes with vanilla boilerplate handled
 */
public abstract class BaseRecipe<T extends Recipe<NoInventory>> implements Recipe<NoInventory> {
    private final Supplier<RecipeSerializer<T>> serializer;
    private final Supplier<FTBStuffRecipeType<T>> recipeType;

    protected BaseRecipe(Supplier<RecipeSerializer<T>> serializer, Supplier<FTBStuffRecipeType<T>> recipeType) {
        this.serializer = serializer;
        this.recipeType = recipeType;
    }

    @Override
    public boolean matches(NoInventory inv, Level world) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventory input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(List.of());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<NoInventory>> getSerializer() {
        return serializer.get();
    }

    @Override
    public RecipeType<? extends Recipe<NoInventory>> getType() {
        return recipeType.get();
    }

    //    @Override
//    public ItemStack getResultItem(HolderLookup.Provider registries) {
//        return ItemStack.EMPTY;
//    }
//
//    @Override
//    public RecipeSerializer<T> getSerializer() {
//        return serializer;
//    }
//
//    @Override
//    public RecipeType<T> getType() {
//        return recipeType;
//    }
}
