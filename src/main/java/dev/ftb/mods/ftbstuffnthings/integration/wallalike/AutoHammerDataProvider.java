package dev.ftb.mods.ftbstuffnthings.integration.wallalike;

import dev.ftb.mods.ftbstuffnthings.blocks.hammer.AutoHammerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum AutoHammerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof AutoHammerBlockEntity autoHammerEntity)) {
            return;
        }

        compoundTag.putInt("progress", autoHammerEntity.getProgress());
        compoundTag.putInt("maxProgress", autoHammerEntity.getMaxProgress());
        compoundTag.putInt("timeout", autoHammerEntity.getTimeout());
        compoundTag.putInt("maxTimeout", autoHammerEntity.getMaxTimeout());
        compoundTag.put("processing", blockAccessor.encodeAsNbt(ItemStack.OPTIONAL_STREAM_CODEC, autoHammerEntity.getProcessingStack()));
        compoundTag.put("output", Util.make(new ListTag(), l ->
                autoHammerEntity.getOverflow().forEach(stack ->
                        l.add(blockAccessor.encodeAsNbt(ItemStack.OPTIONAL_STREAM_CODEC, stack))))
        );
    }

    @Override
    public Identifier getUid() {
        return AutoHammerComponentProvider.ID;
    }
}
