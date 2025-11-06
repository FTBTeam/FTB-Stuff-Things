package dev.ftb.mods.ftbstuffnthings.integration.wallalike;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

enum AutoHammerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation ID = FTBStuffNThings.id("autohammer");

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

        var helper = IElementHelper.get();
        int timeout = serverData.getInt("timeout");
        int maxTimeout = serverData.getInt("maxTimeout");

        if (maxTimeout == 0) {
            float progress = (float) serverData.getInt("progress") / (float) serverData.getInt("maxProgress");
            iTooltip.add(helper.progress(progress, RUNNING, helper.progressStyle().color(0xAD00FF00), BoxStyle.getNestedBox(), false));
        } else {
            float progress = (float) timeout / (float) maxTimeout;
            iTooltip.add(helper.progress(progress, WAITING, helper.progressStyle().color(0xADFF0000), BoxStyle.getNestedBox(), true));
        }

        ItemStack processingStack = ItemStack.OPTIONAL_CODEC.parse(blockAccessor.nbtOps(), serverData.getCompound("processing")).result()
                .orElse(ItemStack.EMPTY);
        List<ItemStack> outputItems = new ArrayList<>();
        if (serverData.contains("output")) {
            var items = serverData.getList("output", Tag.TAG_COMPOUND);
            items.forEach(tag -> ItemStack.OPTIONAL_CODEC.parse(blockAccessor.nbtOps(), tag).result().ifPresent(outputItems::add));
        }

        if (!processingStack.isEmpty()) {
            iTooltip.add(helper.item(processingStack));
            ITooltip tooltip = helper.tooltip();
            tooltip.append(helper.text(Component.translatable("ftbstuff.jade.processing")));
            iTooltip.append(helper.box(tooltip, BoxStyle.getTransparent()).align(IElement.Align.RIGHT));
        }

        if (!outputItems.isEmpty()) {
            iTooltip.add(helper.spacer(-5, 0));
            ITooltip tooltip = helper.tooltip();

            // Creates rows of 5 to prevent the box getting too big.
            int count = 0;
            float scale = outputItems.size() > 5 ? .8f : 1f;
            for (ItemStack outputItem : outputItems) {
                if (count != 0 && count % 5 == 0) {
                    tooltip.add(helper.item(outputItem, scale));
                    count = 0;
                    continue;
                }
                tooltip.append(helper.item(outputItem, scale));
                count++;
            }

            iTooltip.append(helper.box(tooltip, BoxStyle.getTransparent()));

            // Hacks to make the boxes not look stupid
            ITooltip text = helper.tooltip();
            text.append(helper.text(Component.translatable("ftbstuff.jade.buffer")));
            iTooltip.append(helper.box(text, BoxStyle.getTransparent()).align(IElement.Align.RIGHT));
        }
    }


    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof AutoHammerBlockEntity autoHammerEntity)) {
            return;
        }

        compoundTag.putInt("progress", autoHammerEntity.getProgress());
        compoundTag.putInt("maxProgress", autoHammerEntity.getMaxProgress());
        compoundTag.putInt("timeout", autoHammerEntity.getTimeout());
        compoundTag.putInt("maxTimeout", autoHammerEntity.getMaxTimeout());

        ItemStack.OPTIONAL_CODEC.encodeStart(blockAccessor.nbtOps(), autoHammerEntity.getProcessingStack()).result()
                .ifPresent(tag -> compoundTag.put("processing", tag));

        ListTag tagItems = new ListTag();
        autoHammerEntity.getOverflow().forEach(stack -> ItemStack.OPTIONAL_CODEC.encodeStart(blockAccessor.nbtOps(), stack).result()
                .ifPresent(tagItems::add));

        compoundTag.put("output", tagItems);
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

}
