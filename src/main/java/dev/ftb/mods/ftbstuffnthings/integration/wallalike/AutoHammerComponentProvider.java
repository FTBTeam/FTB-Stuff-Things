package dev.ftb.mods.ftbstuffnthings.integration.wallalike;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;

import java.util.ArrayList;
import java.util.List;

enum AutoHammerComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    static final Identifier ID = FTBStuffNThings.id("autohammer");

    private static final Component WAITING = Component.literal(" ")
            .append(Component.translatable("ftbstuff.autohammer.waiting").withStyle(ChatFormatting.WHITE));
    private static final Component RUNNING = Component.literal(" ")
            .append(Component.translatable("ftbstuff.autohammer.running").withStyle(ChatFormatting.WHITE));


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();
        if (!serverData.contains("progress")) {
            return;
        }

        // FIXME how do progress bars work now??
//        int timeout = serverData.getIntOr("timeout", 0);
//        int maxTimeout = serverData.getIntOr("maxTimeout", 0);

//        if (maxTimeout == 0) {
//            float progress = (float) serverData.getIntOr("progress", 0) / (float) serverData.getIntOr("maxProgress", 1);
//            iTooltip.add(JadeUI.progress(progress, RUNNING, JadeUI.progressStyle().color(0xAD00FF00), BoxStyle.nestedBox(), false));
//        } else {
//            float progress = (float) timeout / (float) maxTimeout;
//            iTooltip.add(JadeUI.progress(progress, WAITING, JadeUI.progressStyle().color(0xADFF0000), BoxStyle.nestedBox(), true));
//        }

        ItemStack processingStack = blockAccessor.decodeFromNbt(ItemStack.OPTIONAL_STREAM_CODEC, serverData.get("processing"))
                .orElse(ItemStack.EMPTY);

        List<ItemStack> outputItems = new ArrayList<>();
        if (serverData.contains("output")) {
            serverData.getList("output").ifPresent(listTag -> {
                listTag.forEach(tag -> blockAccessor.decodeFromNbt(ItemStack.OPTIONAL_STREAM_CODEC, tag)
                        .ifPresent(outputItems::add)
                );
            });
        }

        if (!processingStack.isEmpty()) {
            iTooltip.add(JadeUI.item(processingStack));
            ITooltip tooltip = JadeUI.tooltip();
            tooltip.append(JadeUI.text(Component.translatable("ftbstuff.jade.processing")));
            iTooltip.append(JadeUI.box(tooltip, BoxStyle.transparent()).alignSelfEnd());
        }

        if (!outputItems.isEmpty()) {
            iTooltip.add(JadeUI.spacer(-5, 0));
            ITooltip tooltip = JadeUI.tooltip();

            // Creates rows of 5 to prevent the box getting too big.
            int count = 0;
            float scale = outputItems.size() > 5 ? .8f : 1f;
            for (ItemStack outputItem : outputItems) {
                if (count != 0 && count % 5 == 0) {
                    tooltip.add(JadeUI.item(outputItem, scale));
                    count = 0;
                    continue;
                }
                tooltip.append(JadeUI.item(outputItem, scale));
                count++;
            }

            iTooltip.append(JadeUI.box(tooltip, BoxStyle.transparent()));

            // Hacks to make the boxes not look stupid
            ITooltip text = JadeUI.tooltip();
            text.append(JadeUI.text(Component.translatable("ftbstuff.jade.buffer")));
            iTooltip.append(JadeUI.box(text, BoxStyle.transparent()).alignSelfEnd());
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }

}
