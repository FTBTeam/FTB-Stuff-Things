package dev.ftb.mods.ftbstuffnthings.blocks.sluice;

import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.DoubleValue;
import dev.ftb.mods.ftblibrary.config.value.IntValue;

public record SluiceProperties(DoubleValue timeMod, DoubleValue fluidMod, IntValue tankCap,
                               BooleanValue itemIO, BooleanValue fluidIO, BooleanValue upgradeable, IntValue energyCost) {
}
