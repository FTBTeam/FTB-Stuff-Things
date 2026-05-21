package dev.ftb.mods.ftbstuffnthings.integration.wallalike;

import dev.ftb.mods.ftbstuffnthings.blocks.pump.PumpBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PumpDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof PumpBlockEntity pump) {
            compoundTag.putInt("timeLeft", pump.getTimeLeft());
        }
    }

    @Override
    public Identifier getUid() {
        return PumpComponentProvider.ID;
    }
}
