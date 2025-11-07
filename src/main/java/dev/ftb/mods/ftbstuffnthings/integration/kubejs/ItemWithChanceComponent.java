package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.ItemWithChance;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ItemWithChanceComponent extends SimpleRecipeComponent<ItemWithChance> {
    public static final TypeInfo TYPE_INFO = TypeInfo.of(ItemWithChance.class);
    public static final RecipeComponentType.Unit<ItemWithChance> TYPE
            = RecipeComponentType.unit(FTBStuffNThings.id("item_with_chance"), ItemWithChanceComponent::new);

    public ItemWithChanceComponent(RecipeComponentType<?> type) {
        super(type, ItemWithChance.CODEC, TYPE_INFO);
    }
}
