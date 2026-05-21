package dev.ftb.mods.ftbstuffnthings.integration.wallalike;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PumpComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    static final Identifier ID = FTBStuffNThings.id("pump");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();
        serverData.getInt("timeLeft").ifPresent(timeLeft ->
                iTooltip.add(Component.translatable("ftbstuff.jade.time_left", timeLeft)));
    }

    @Override
    public Identifier getUid() {
        return ID;
    }

    private static String getTimeString(int ticks) {
        int seconds = ticks / 20;

        int i = (seconds % 3600) / 60;
        return (i > 0 ? i + "m " : "") + (seconds % 3600) % 60 + "s";
    }
}
