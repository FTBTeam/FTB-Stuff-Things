package dev.ftb.mods.ftbstuffnthings.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbstuffnthings.client.screens.FusingMachineScreen;
import dev.ftb.mods.ftbstuffnthings.crafting.EnergyRequirement;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.FusingMachineRecipe;
import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public class FusingMachineCategory extends BaseStuffCategory<FusingMachineRecipe> {
    public static final ResourceLocation BACKGROUND = bgTexture("jei_fusing_machine.png");

    private static final Rect2i CLICK_AREA = new Rect2i(89, 26, 26, 19);

    private final IDrawableAnimated powerBar;
    private final IDrawableAnimated progress;

    public FusingMachineCategory() {
        super(RecipeTypes.FUSING_MACHINE,
                Component.translatable("block.ftbstuff.fusing_machine"),
                guiHelper().drawableBuilder(BACKGROUND, 0, 0, 106, 28)
                        .setTextureSize(134, 28)
                        .build(),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, ItemsRegistry.FUSING_MACHINE.get().getDefaultInstance())
        );

        powerBar = guiHelper().drawableBuilder(BACKGROUND, 106, 0, 6, 16)
                .setTextureSize(134, 28)
                .buildAnimated(guiHelper().createTickTimer(120, 16, false), IDrawableAnimated.StartDirection.BOTTOM);

        progress = guiHelper().drawableBuilder(BACKGROUND, 112, 0, 22, 16)
                .setTextureSize(134, 28)
                .buildAnimated(guiHelper().createTickTimer(120, 22, true), IDrawableAnimated.StartDirection.LEFT);
    }

    @Override
    public void draw(FusingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

        this.powerBar.draw(graphics, 6, 6);
        this.progress.draw(graphics, 57, 6);

        PoseStack stack = graphics.pose();

        stack.pushPose();
        stack.translate(5, 25, 0);
        stack.scale(0.5F, 0.5F, 0.5F);

        EnergyRequirement energyRequirement = recipe.getEnergyComponent();
        int ticks = energyRequirement.ticksToProcess();
        int energyPerTick = energyRequirement.fePerTick();
        int totalEnergy = ticks * energyPerTick;

        graphics.drawString(Minecraft.getInstance().font, "%sFE/t (%sFE)".formatted(energyPerTick, totalEnergy), 0, 0, 0x404040, false);

        stack.popPose();

        stack.pushPose();
        stack.translate(83, 25, 0);
        stack.scale(0.5F, 0.5F, 0.5F);
        graphics.drawString(Minecraft.getInstance().font, "%s ticks".formatted(ticks), 0, 0, 0x404040, false);

        stack.popPose();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FusingMachineRecipe fusingRecipe, IFocusGroup iFocusGroup) {
        for (int i = 0; i < fusingRecipe.getInputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 18 + i * 18, 6).addIngredients(fusingRecipe.getInputs().get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 6).addFluidStack(fusingRecipe.getFluidResult().getFluid(), fusingRecipe.getFluidResult().getAmount())
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal(fusingRecipe.getFluidResult().getAmount() + " mB"));
                });
    }

    enum ContainerHandler implements IGuiContainerHandler<FusingMachineScreen> {
        INSTANCE;

        @Override
        public Collection<IGuiClickableArea> getGuiClickableAreas(FusingMachineScreen containerScreen, double guiMouseX, double guiMouseY) {
            return List.of(
                    IGuiClickableArea.createBasic(CLICK_AREA.getX(), CLICK_AREA.getY(),
                            CLICK_AREA.getWidth(), CLICK_AREA.getHeight(),
                            RecipeTypes.FUSING_MACHINE)
            );
        }
    }
}
