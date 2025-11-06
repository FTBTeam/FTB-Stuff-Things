package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.crafting.recipe.WoodenBasinRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.BlocksRegistry;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class WoodenBasinCategory extends BaseStuffCategory<WoodenBasinRecipe> {
    protected WoodenBasinCategory() {
        super(RecipeTypes.WOODEN_BASIN,
                Component.translatable(BlocksRegistry.WOODEN_BASIN.get().getDescriptionId()),
                guiHelper().drawableBuilder(bgTexture("jei_wooden_basin.png"), 0, 0, 64, 64)
                        .setTextureSize(64, 64).build(),
                guiHelper().createDrawableItemStack(new ItemStack(ItemsRegistry.WOODEN_BASIN.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WoodenBasinRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inputBuilder = builder.addInputSlot(6, 25);
        recipe.getInputsForDisplay().forEach(input ->
                input.ifLeft(stack -> inputBuilder.addIngredient(VanillaTypes.ITEM_STACK, stack))
                        .ifRight(fluid -> inputBuilder.addFluidStack(fluid, 1000L))
        );

        builder.addOutputSlot(43, 43)
                .addFluidStack(recipe.getFluid().getFluid(), recipe.getFluid().getAmount())
                .setOverlay(new FluidAmountDrawable(recipe.getFluid().getAmount()), 0, 0);
    }

    @Override
    public void draw(WoodenBasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        guiGraphics.renderItem(ItemsRegistry.WOODEN_BASIN.toStack(), 6, 43);

        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath("ftblibrary", "textures/icons/info.png"), 42, 5, 0, 0, 16, 16,16, 16, 16);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, WoodenBasinRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 41 && mouseX <= 59 && mouseY >= 4 && mouseY <= 22) {
            tooltip.add(Component.translatable("ftbstuff.jei.wooden_basin_info"));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("ftbstuff.wooden_basin.produce_chance", (int) (recipe.getProductionChance() * 100))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("ftbstuff.wooden_basin.consume_chance", (int) (recipe.getBlockConsumeChance() * 100))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
