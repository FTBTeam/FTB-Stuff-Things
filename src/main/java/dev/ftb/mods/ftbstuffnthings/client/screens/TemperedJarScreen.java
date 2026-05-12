package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarMenu;
import dev.ftb.mods.ftbstuffnthings.client.GuiUtil;
import dev.ftb.mods.ftbstuffnthings.network.ToggleJarCraftingPacket;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TemperedJarScreen extends AbstractContainerScreen<TemperedJarMenu> {
    private static final Identifier TEXTURE = FTBStuffNThings.id("textures/gui/tempered_jar.png");
    private static final Identifier CRAFTING_ICON = FTBStuffNThings.id("textures/gui/crafting_icon.png");

    public static final Rect2i FLUID_AREA = new Rect2i(55, 30, 48, 76);
    public static final Rect2i TEMPERATURE_AREA = new Rect2i(55 + FLUID_AREA.getWidth() / 2 - 8, 30 + FLUID_AREA.getHeight() + 5, 16, 16);
    public static final Rect2i JEI_AREA = new Rect2i(132, 75, 16, 16);

    private Button startButton;

    public TemperedJarScreen(TemperedJarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        imageHeight = 214;
        inventoryLabelY = 125;
    }

    @Override
    protected void init() {
        super.init();

        startButton = Button.builder(Component.translatable("ftbstuff.start_mix"), b -> ToggleJarCraftingPacket.sendToServer())
                .size(56, 20)
                .pos(leftPos + 112, topPos + 40)
                .build();
        addRenderableWidget(startButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        TemperedJarBlockEntity.JarStatus status = menu.getJar().getStatus();
        startButton.active = status == TemperedJarBlockEntity.JarStatus.READY || status == TemperedJarBlockEntity.JarStatus.CRAFTING;
        startButton.setMessage(Component.translatable(menu.getJar().getRemainingTime() > 0 ? "ftbstuff.stop_mix" : "ftbstuff.start_mix"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int minX = leftPos + FLUID_AREA.getX();
        int maxX = leftPos + FLUID_AREA.getX() + FLUID_AREA.getWidth();
        int minY = topPos + FLUID_AREA.getY();
        int maxY = topPos + FLUID_AREA.getY() + FLUID_AREA.getHeight();

        guiGraphics.fill(minX, minY, maxX, maxY, 0xFF8B8B8B);
        guiGraphics.blit(TEXTURE, minX - 4, minY - 13, 176, 0, 56, 94);
        guiGraphics.blit(CRAFTING_ICON, leftPos + JEI_AREA.getX(), topPos + JEI_AREA.getY(), 0, 0, 16, 16, 16, 16);

        renderTemperatureIndicator(guiGraphics, mouseX, mouseY, minX, maxY);
        renderFluids(guiGraphics, mouseX, mouseY);
        renderProgressBar(guiGraphics);
        renderStatusInfo(guiGraphics, mouseX, mouseY);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderTemperatureIndicator(GuiGraphics guiGraphics, int mouseX, int mouseY, int xPos, int yPos) {
        TemperatureAndEfficiency temp = menu.getJar().getTemperature();
        guiGraphics.blit(temp.temperature().getTexture(),
                leftPos + TEMPERATURE_AREA.getX(), topPos + TEMPERATURE_AREA.getY(),
                0, 0, 16, 16, 16, 16);

        if (!ModList.get().isLoaded("jei")) {
            // when JEI is loaded, it handles the tooltip here, since it's also used for the "Show Recipes" action
            if (TEMPERATURE_AREA.contains(mouseX - leftPos, mouseY - topPos)) {
                List<Component> list = List.of(
                        Component.translatable("ftbstuff.temperature", temp.temperature().getName()),
                        Component.translatable("ftbstuff.efficiency", temp.formatEfficiency())
                );
                guiGraphics.renderTooltip(font, list, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void renderStatusInfo(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (startButton.isHovered()) {
            List<Component> outputs = new ArrayList<>();
            outputs.add(getMenu().getJar().getStatus().displayString());
            getMenu().getJar().getCurrentRecipe().ifPresent(holder -> {
                outputs.add(Component.translatable("ftbstuff.making"));
                holder.value().getOutputItems().forEach(stack -> outputs.add(
                        Component.literal("• ").append(stack.getCount() + " x ").append(stack.getHoverName()))
                );
                holder.value().getOutputFluids().forEach(stack -> outputs.add(
                        Component.literal("• ").append(stack.getAmount() + "mB ").append(stack.getHoverName()))
                );
                if (Minecraft.getInstance().options.advancedItemTooltips) {
                    outputs.add(Component.literal("Recipe: " + holder.id()).withStyle(ChatFormatting.DARK_GRAY));
                }
            });
            guiGraphics.renderTooltip(font, outputs, Optional.empty(), mouseX, mouseY);
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics) {
        int remaining = menu.getJar().getRemainingTime();
        int total = menu.getJar().getProcessingTime();

        if (menu.getJar().getStatus() == TemperedJarBlockEntity.JarStatus.CRAFTING && total > 0) {
            int x1 = startButton.getX();
            int x2 = startButton.getX() + startButton.getWidth();
            int y1 = startButton.getY() + startButton.getHeight() + 2;
            int y2 = y1 + 8;
            guiGraphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF606060);
            guiGraphics.fill(x1 , y1, x2, y2, 0xFFA0A0A0);
            guiGraphics.blit(TEXTURE, x1, y1, 0, 240, (x2 - x1) * (total - remaining) / total, 8);
        }
    }

    private void renderFluids(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        IFluidHandler fluidHandler = menu.getJar().getFluidHandler();

        int yPos = FLUID_AREA.getY() + FLUID_AREA.getHeight();
        int total = fluidHandler.getTanks() * TemperedJarBlockEntity.TANK_CAPACITY;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 0);
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fs = fluidHandler.getFluidInTank(i);
            if (!fs.isEmpty()) {
                int ySize = FLUID_AREA.getHeight() * fs.getAmount() / total;
                yPos -= ySize;
                GuiUtil.drawFluid(guiGraphics, new Rect2i(FLUID_AREA.getX(), yPos, FLUID_AREA.getWidth(), ySize), fs, null);
            }
        }
        guiGraphics.pose().popPose();

        if (FLUID_AREA.contains(mouseX - leftPos, mouseY - topPos)) {
            List<Component> lines = new ArrayList<>();
            for (int i = fluidHandler.getTanks() - 1; i >= 0; i--) {
                FluidStack fs = fluidHandler.getFluidInTank(i);
                if (!fs.isEmpty()) {
                    lines.add(Component.translatable("ftblibrary.mb", fs.getAmount(), fs.getHoverName()));
                }
            }
            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, x, titleLabelY, 0xFF404040, false);
    }
}
