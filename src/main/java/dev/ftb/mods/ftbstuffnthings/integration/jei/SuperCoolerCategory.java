package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.client.screens.SuperCoolerScreen;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.SuperCoolerRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.MiscUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;

public class SuperCoolerCategory extends BaseStuffCategory<SuperCoolerRecipe> {
    public static final Identifier BACKGROUND = bgTexture("jei_super_cooler.png");

    private static final Rect2i CLICK_AREA = new Rect2i(77, 25, 27, 21);

    private final IDrawableAnimated powerBar;
    private final IDrawableAnimated progress;

    public SuperCoolerCategory() {
        super(JeiRecipeTypes.SUPER_COOLER,
                Component.translatable("block.ftbstuff.super_cooler"),
                guiHelper().drawableBuilder(BACKGROUND, 0, 0, 146, 28)
                        .setTextureSize(174, 28)
                        .build(),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemsRegistry.SUPER_COOLER.toStack())
        );
        this.powerBar = guiHelper().drawableBuilder(BACKGROUND, 146, 0, 6, 16)
                .setTextureSize(174, 28)
                .buildAnimated(guiHelper().createTickTimer(120, 16, false), IDrawableAnimated.StartDirection.BOTTOM);

        this.progress = guiHelper().drawableBuilder(BACKGROUND, 152, 0, 22, 16)
                .setTextureSize(174, 28)
                .buildAnimated(guiHelper().createTickTimer(120, 22, true), IDrawableAnimated.StartDirection.LEFT);
    }

    @Override
    public void draw(SuperCoolerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

        this.powerBar.draw(graphics, 6, 6);
        this.progress.draw(graphics, 97, 6);

        EnergyRequirement energyRequirement = recipe.getEnergyComponent();
        int ticks = energyRequirement.ticksToProcess();
        int energyPerTick = energyRequirement.fePerTick();
        int totalEnergy = ticks * energyPerTick;

        var stack = graphics.pose();
        stack.pushMatrix();
        stack.translate(5, 25);
        stack.scale(0.5F, 0.5F);
        graphics.text(Minecraft.getInstance().font, "%sFE/t (%sFE)".formatted(energyPerTick, totalEnergy), 0, 0, 0xBEFFFFFF);
        stack.popMatrix();

        stack.pushMatrix();
        stack.translate(96, 25);
        stack.scale(0.5F, 0.5F);
        graphics.text(Minecraft.getInstance().font, "%s ticks".formatted(ticks), 0, 0, 0xBEFFFFFF);
        stack.popMatrix();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SuperCoolerRecipe superCoolerRecipe, IFocusGroup iFocusGroup) {
        for (int i = 0; i < superCoolerRecipe.getInputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 40 + i * 18, 6).add(superCoolerRecipe.getInputs().get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 6).add(superCoolerRecipe.getResult());

        List<Fluid> fluids = MiscUtil.getFluidsForSizedIngredient(superCoolerRecipe.getFluidInput()).stream()
                .map(FluidStack::getFluid).toList();
        if (!fluids.isEmpty()) {
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 18, 6);
            fluids.forEach(fluid -> slotBuilder.add(fluid, superCoolerRecipe.getFluidInput().amount()));
            slotBuilder.addRichTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.literal(superCoolerRecipe.getFluidInput().amount() + " mB")));
        }
    }

    enum ContainerHandler implements IGuiContainerHandler<SuperCoolerScreen> {
        INSTANCE;

        @Override
        public Collection<IGuiClickableArea> getGuiClickableAreas(SuperCoolerScreen containerScreen, double guiMouseX, double guiMouseY) {
            return List.of(
                    IGuiClickableArea.createBasic(CLICK_AREA.getX(), CLICK_AREA.getY(),
                            CLICK_AREA.getWidth(), CLICK_AREA.getHeight(),
                            JeiRecipeTypes.SUPER_COOLER)
            );
        }
    }
}
