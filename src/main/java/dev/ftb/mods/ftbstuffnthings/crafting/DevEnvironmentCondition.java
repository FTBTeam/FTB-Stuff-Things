package dev.ftb.mods.ftbstuffnthings.crafting;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbstuffnthings.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.conditions.ICondition;

public enum DevEnvironmentCondition implements ICondition {
    INSTANCE;

    public static final MapCodec<DevEnvironmentCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(IContext context) {
        return ModConfig.INCLUDE_DEV_RECIPES.get() || !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
