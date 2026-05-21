package dev.ftb.mods.ftbstuffnthings.integration.jei;

import com.google.common.base.MoreObjects;
import dev.ftb.mods.ftbstuffnthings.config.ServerConfig;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.config.StartupConfig;
import dev.ftb.mods.ftbstuffnthings.temperature.Temperature;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum TemperatureHelper implements IIngredientHelper<Temperature> {
    INSTANCE;

    @Override
    public IIngredientType<Temperature> getIngredientType() {
        return FTBStuffIngredientTypes.TEMPERATURE;
    }

    @Override
    public String getDisplayName(Temperature ingredient) {
        return ingredient.getName().getString();
    }

    @Override
    public Object getUid(Temperature ingredient, UidContext context) {
        return ingredient.getSerializedName();
    }

    @Override
    public Identifier getIdentifier(Temperature ingredient) {
        return FTBStuffNThings.id(ingredient.getSerializedName());
    }

    @Override
    public Temperature copyIngredient(Temperature ingredient) {
        return ingredient;
    }

    @Override
    public boolean isHiddenFromRecipeViewersByTags(Temperature ingredient) {
        return StartupConfig.HIDE_TEMPERATURE_INGREDIENTS.get();
    }

    @Override
    public String getErrorInfo(@Nullable Temperature ingredient) {
        if (ingredient == null) {
            return "null";
        }

        return MoreObjects.toStringHelper(Temperature.class).add("ID", ingredient.getSerializedName()).toString();
    }

}
