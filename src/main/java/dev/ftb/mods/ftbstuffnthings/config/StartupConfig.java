package dev.ftb.mods.ftbstuffnthings.config;

import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;

public interface StartupConfig {
    String KEY = FTBStuffNThings.MOD_ID + "-startup";

    Config CONFIG = Config.create(KEY);

    Config GENERAL = CONFIG.addGroup("general");
    BooleanValue INCLUDE_DEV_RECIPES = GENERAL.addBoolean("include_dev_recipes", false)
            .comment("If true, dev/testing recipes will be available outside a development environment", "Leave this false unless actually testing the mod.");
    BooleanValue HIDE_TEMPERATURE_INGREDIENTS = GENERAL.addBoolean("hide_temperature_ingredients", false)
            .comment("If true, the custom temperature ingredients will not be displayed");

}
