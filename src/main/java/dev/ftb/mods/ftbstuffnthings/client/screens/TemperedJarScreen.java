package dev.ftb.mods.ftbstuffnthings.client.screens;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarBlockEntity;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarMenu;
import dev.ftb.mods.ftbstuffnthings.client.GuiUtil;
import dev.ftb.mods.ftbstuffnthings.network.ToggleJarCraftingPacket;
import dev.ftb.mods.ftbstuffnthings.temperature.TemperatureAndEfficiency;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

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
        super(menu, playerInventory, title, 176, 214);

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
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);

        int minX = leftPos + FLUID_AREA.getX();
        int maxX = leftPos + FLUID_AREA.getX() + FLUID_AREA.getWidth();
        int minY = topPos + FLUID_AREA.getY();
        int maxY = topPos + FLUID_AREA.getY() + FLUID_AREA.getHeight();

        guiGraphics.fill(minX, minY, maxX, maxY, 0xFF8B8B8B);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, minX - 4, minY - 13,
                176, 0, 56, 94, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CRAFTING_ICON, leftPos + JEI_AREA.getX(), topPos + JEI_AREA.getY(),
                0, 0, 16, 16, 16, 16);

        renderTemperatureIndicator(guiGraphics, mouseX, mouseY, minX, maxY);
        renderFluids(guiGraphics, mouseX, mouseY);
        renderProgressBar(guiGraphics);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (!ModList.get().isLoaded("jei")) {
            // when JEI is loaded, it handles the tooltip here, since it's also used for the "Show Recipes" action
            TemperatureAndEfficiency temp = menu.getJar().getTemperature();
            if (TEMPERATURE_AREA.contains(mouseX - leftPos, mouseY - topPos)) {
                List<Component> list = List.of(
                        Component.translatable("ftbstuff.temperature", temp.temperature().getName()),
                        Component.translatable("ftbstuff.efficiency", temp.formatEfficiency())
                );
                graphics.setTooltipForNextFrame(font, list, Optional.empty(), mouseX, mouseY);
            }
        }
        if (startButton.isHovered()) {
            List<Component> outputs = new ArrayList<>();
            outputs.add(getMenu().getJar().getStatus().displayString());
            getMenu().getJar().getCurrentRecipe().ifPresent(holder -> {
                outputs.add(Component.translatable("ftbstuff.making"));
                holder.value().getOutputItems().forEach(stack -> outputs.add(
                        Component.literal("• ").append(stack.count() + " x ").append(stack.create().getHoverName()))
                );
                holder.value().getOutputFluids().forEach(stack -> outputs.add(
                        Component.literal("• ").append(stack.amount() + "mB ").append(stack.create().getHoverName()))
                );
                if (Minecraft.getInstance().options.advancedItemTooltips) {
                    outputs.add(Component.literal("Recipe: " + holder.id()).withStyle(ChatFormatting.DARK_GRAY));
                }
            });
            graphics.setTooltipForNextFrame(font, outputs, Optional.empty(), mouseX, mouseY);
        }
        if (FLUID_AREA.contains(mouseX - leftPos, mouseY - topPos)) {
            var fluidHandler = menu.getJar().getFluidHandler();
            List<Component> lines = new ArrayList<>();
            for (int i = fluidHandler.size() - 1; i >= 0; i--) {
                FluidStack fs = FluidUtil.getStack(fluidHandler, i);
                if (!fs.isEmpty()) {
                    lines.add(Component.translatable("ftblibrary.mb", fs.getAmount(), fs.getHoverName()));
                }
            }
            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    private void renderTemperatureIndicator(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int xPos, int yPos) {
        TemperatureAndEfficiency temp = menu.getJar().getTemperature();
        guiGraphics.blit(temp.temperature().getTexture(),
                leftPos + TEMPERATURE_AREA.getX(), topPos + TEMPERATURE_AREA.getY(),
                0, 0, 16, 16, 16, 16);
    }

    private void renderProgressBar(GuiGraphicsExtractor guiGraphics) {
        int remaining = menu.getJar().getRemainingTime();
        int total = menu.getJar().getProcessingTime();

        if (menu.getJar().getStatus() == TemperedJarBlockEntity.JarStatus.CRAFTING && total > 0) {
            int x1 = startButton.getX();
            int x2 = startButton.getX() + startButton.getWidth();
            int y1 = startButton.getY() + startButton.getHeight() + 2;
            int y2 = y1 + 8;
            guiGraphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF606060);
            guiGraphics.fill(x1 , y1, x2, y2, 0xFFA0A0A0);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x1, y1,
                    0, 240, (x2 - x1) * (total - remaining) / total, 8, 256, 256);
        }
    }

    private void renderFluids(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        var fluidHandler = menu.getJar().getFluidHandler();

        int yPos = FLUID_AREA.getY() + FLUID_AREA.getHeight();
        int total = fluidHandler.size() * TemperedJarBlockEntity.TANK_CAPACITY;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);
        for (int i = 0; i < fluidHandler.size(); i++) {
            FluidStack fs = FluidUtil.getStack(fluidHandler, i);
            if (!fs.isEmpty()) {
                int ySize = FLUID_AREA.getHeight() * fs.getAmount() / total;
                yPos -= ySize;
                GuiUtil.drawFluid(guiGraphics, new Rect2i(FLUID_AREA.getX(), yPos, FLUID_AREA.getWidth(), ySize), fs, 0);
            }
        }
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        int x = (imageWidth - font.width(title)) / 2;
        graphics.text(font, title, x, titleLabelY, 0xFF404040, false);
    }
}
