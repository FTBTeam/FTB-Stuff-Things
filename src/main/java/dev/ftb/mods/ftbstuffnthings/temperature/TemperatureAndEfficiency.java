package dev.ftb.mods.ftbstuffnthings.temperature;

import dev.ftb.mods.ftbstuffnthings.crafting.NoInventory;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.JarRecipe;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.TemperatureSourceRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.RecipesRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record TemperatureAndEfficiency(Temperature temperature, double efficiency) {
	public static final TemperatureAndEfficiency DEFAULT = new TemperatureAndEfficiency(Temperature.NORMAL, 1D);

	public static TemperatureAndEfficiency fromLevel(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		return level.getServer().getRecipeManager().recipeMap().getRecipesFor(RecipesRegistry.TEMPERATURE_SOURCE_TYPE.get(), NoInventory.INSTANCE, level)
				.filter(r -> r.value().test(state))
				.map(r -> r.value().getTemperatureAndEfficiency())
				.findFirst()
				.orElse(DEFAULT);
	}

	public int getRecipeTime(JarRecipe recipe) {
		return Mth.clamp((int) (recipe.getTime() / efficiency), 1, Short.MAX_VALUE);
	}

	public Component formatEfficiency() {
		int eff = (int) (efficiency() * 100);
		ChatFormatting color = eff == 100 ? ChatFormatting.YELLOW : eff > 100 ? ChatFormatting.GREEN : ChatFormatting.GOLD;
		return Component.literal(eff + "%").withStyle(color);
	}
}
