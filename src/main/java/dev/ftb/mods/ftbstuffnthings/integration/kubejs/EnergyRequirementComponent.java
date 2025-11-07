package dev.ftb.mods.ftbstuffnthings.integration.kubejs;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EnergyRequirementComponent extends SimpleRecipeComponent<EnergyRequirement> {
    public static final TypeInfo TYPE_INFO = TypeInfo.of(EnergyRequirement.class);
    public static final RecipeComponentType.Unit<EnergyRequirement> TYPE
            = RecipeComponentType.unit(FTBStuffNThings.id("energy"), EnergyRequirementComponent::new);

    public EnergyRequirementComponent(RecipeComponentType<?> type) {
        super(type, EnergyRequirement.CODEC, TYPE_INFO);
    }
}
