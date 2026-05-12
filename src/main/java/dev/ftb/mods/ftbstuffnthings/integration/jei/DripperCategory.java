package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.DripperRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class DripperCategory extends BaseStuffCategory<DripperRecipe> {
	public DripperCategory() {
		super(RecipeTypes.DRIPPER,
				Component.translatable(BlocksRegistry.DRIPPER.get().getDescriptionId()),
				guiHelper().drawableBuilder(bgTexture("jei_dripper.png"),
						0, 0, 91, 30).setTextureSize(128, 64).build(),
				guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemsRegistry.DRIPPER.get().getDefaultInstance())
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DripperRecipe recipe, IFocusGroup focuses) {
		IRecipeSlotBuilder outputBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 7);
		recipe.getOutputItemOrFluid()
				.ifLeft(outputBuilder::add)
				.ifRight(outputBuilder::add);

		IRecipeSlotBuilder inputBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 23, 7);
		recipe.getInputsForDisplay().forEach(input ->
				input.ifLeft(stack -> inputBuilder.add(VanillaTypes.ITEM_STACK, stack))
						.ifRight(fluid -> inputBuilder.add(fluid, 1000L))
		);

		builder.addSlot(RecipeIngredientRole.INPUT, 3, 7)
				.add(NeoForgeTypes.FLUID_STACK, recipe.getFluid())
				.setOverlay(new FluidAmountDrawable(recipe.getFluid().getAmount()), 0, 0)
				.addRichTooltipCallback((recipeSlotView, tooltipBuilder) -> addTooltipInfo(recipe, tooltipBuilder));
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, DripperRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);

		if (mouseX > 42 && mouseX < 65) {
			// over the arrow icon
			addTooltipInfo(recipe, tooltip);
		}
	}

	private void addTooltipInfo(DripperRecipe recipe, ITooltipBuilder tooltipBuilder) {
		String pct = String.format("%.0f", recipe.getChance() * 100d);
		tooltipBuilder.add(Component.translatable("ftbstuff.dripper.chance", pct).withStyle(ChatFormatting.YELLOW));
		if (recipe.consumeFluidOnFail()) {
			tooltipBuilder.add(Component.translatable("ftbstuff.dripper.consume_on_fail").withStyle(ChatFormatting.GOLD));
		}
	}
}
