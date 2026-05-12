package dev.ftb.mods.ftbstuffnthings.registry;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.advancements.CustomTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CriterionTriggerRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, FTBStuffNThings.MOD_ID);

    public static final Supplier<CustomTrigger> FTBSTUFF_ROOT = register("root");
    public static final Supplier<CustomTrigger> SUPERCHARGED = register("supercharged");

    private static Supplier<CustomTrigger> register(String name) {
        return CRITERION_TRIGGERS.register(name, () -> new CustomTrigger(name));
    }

    public static void init(IEventBus modEventBus) {
        CRITERION_TRIGGERS.register(modEventBus);
    }
}
