package dev.ftb.mods.ftbstuffnthings.integration.jei;

import dev.ftb.mods.ftbstuffnthings.registry.ItemsRegistry;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.LootSummary;
import dev.ftb.mods.ftbstuffnthings.util.lootsummary.WrappedLootSummary;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootSummaryCategory extends BaseStuffCategory<WrappedLootSummary> {
    private static final ChatFormatting[] POOL_COLS = new ChatFormatting[] {
            ChatFormatting.WHITE,
            ChatFormatting.YELLOW,
            ChatFormatting.AQUA,
            ChatFormatting.GREEN,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GOLD,
    };
    public static final int MAX_DISPLAYABLE_IDX = 34;  // 0-indexed, 5x7 entries

    private int nItems;

    public LootSummaryCategory() {
        super(
                RecipeTypes.LOOT_SUMMARY,
                Component.translatable("ftbstuff.jei.loot_summary"),
                guiHelper().drawableBuilder(bgTexture("jei_loot_table.png"), 0, 0, 156, 126).setTextureSize(180, 126).build(),
                guiHelper().createDrawableItemStack(ItemsRegistry.CRATE.toStack())
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WrappedLootSummary recipe, IFocusGroup focuses) {
        nItems = 0;

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .add(recipe.inputStack().asItem().getDefaultInstance());

        var poolMap = recipe.summary().entryMap();
        int nPools = poolMap.keySet().size();

        Map<String,Integer> pool2idx = new HashMap<>();
        int n = 0;
        for (var entry : poolMap.entrySet()) {
            pool2idx.put(entry.getKey(), n++);
        }

        MutableInt poolIdx = new MutableInt(0);
        for (var entry : poolMap.entrySet()) {
            String poolName = entry.getKey();
            for (LootSummary.SummaryEntry summaryEntry : entry.getValue()) {
                if (!summaryEntry.stack().isEmpty()) {
                    if (nItems <= MAX_DISPLAYABLE_IDX) {
                        builder.addSlot(RecipeIngredientRole.OUTPUT, 28 + (nItems % 7 * 18), 5 + nItems / 7 * 24)
                                .add(summaryEntry.stack())
                                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                                    if (nPools > 1) {
                                        Component poolComp = Component.literal(poolName).withStyle(poolColor(pool2idx.getOrDefault(poolName, 0)));
                                        tooltip.add(Component.translatable("ftbstuff.jei.loot_summary.pool", poolComp)
                                                .withStyle(ChatFormatting.GRAY));
                                    }
                                });
                    }
                    nItems++;
                }
            }
            poolIdx.increment();
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, WrappedLootSummary recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);

        Map<String, List<LootSummary.SummaryEntry>> entryMap = recipe.summary().entryMap();
        int nPools = entryMap.keySet().size();
        if (nPools > 1 && mouseX >= 5 && mouseX <= 21 && mouseY >= 25 && mouseY <= 41) {
            tooltip.add(Component.translatable("ftbstuff.jei.loot_summary.pool_header", nPools));
            int i = 0;
            for (String pool : entryMap.keySet()) {
                tooltip.add(Component.literal("█ " + pool).withStyle(poolColor(i)));
                i++;
            }
        }
        if (mouseX >= 5 && mouseX <= 21 && mouseY >= 110 && mouseY <= 126) {
            tooltip.add(Component.translatable("ftbstuff.jei.loot_summary.too_many", (nItems - MAX_DISPLAYABLE_IDX) + 1));
        }
    }

    @Override
    public void draw(WrappedLootSummary recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        var poolMap = recipe.summary().entryMap();

        if (poolMap.keySet().size() > 1) {
            // multiple pools
            guiGraphics.blit(Identifier.parse("ftblibrary:textures/icons/info.png"), 5, 25, 0, 0, 16, 16, 16, 16);
        }
        if (nItems >= MAX_DISPLAYABLE_IDX) {
            guiGraphics.blit(Identifier.withDefaultNamespace("textures/gui/sprites/icon/unseen_notification.png"), 5, 110, 0, 0, 16, 16, 16, 16);
        }

        int idx = 0;
        int poolIdx = 0;
        for (var entryList : poolMap.values()) {
            for (LootSummary.SummaryEntry summaryEntry : entryList) {
                if (!summaryEntry.stack().isEmpty()) {
                    var stack = guiGraphics.pose();
                    stack.pushMatrix();
                    //noinspection IntegerDivisionInFloatingPointContext
                    stack.translate(36 + (idx % 7 * 18), 23.5f + (idx / 7 * 24));  // int division is what we need here
                    stack.scale(.5F, .5F);
                    String weightStr = String.format("%.2f%%", summaryEntry.weight() * 100f);
                    guiGraphics.centeredText(Minecraft.getInstance().font, Component.literal(weightStr).withStyle(poolColor(poolIdx)), 0, 0, 0xFFFFFF);
                    stack.popMatrix();
                    if (idx++ >= MAX_DISPLAYABLE_IDX) {
                        return;
                    }
                }
            }
            poolIdx++;
        }
    }

    static ChatFormatting poolColor(int n) {
        return POOL_COLS[n % POOL_COLS.length];
    }
}
