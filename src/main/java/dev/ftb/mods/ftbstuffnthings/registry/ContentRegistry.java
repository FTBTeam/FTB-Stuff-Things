package dev.ftb.mods.ftbstuffnthings.registry;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.fusingmachine.FusingMachineMenu;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarMenu;
import dev.ftb.mods.ftbstuffnthings.blocks.sluice.SluiceType;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerMenu;
import dev.ftb.mods.ftbstuffnthings.blocks.supercooler.SuperCoolerMenu;
import dev.ftb.mods.ftbstuffnthings.crafting.DevEnvironmentCondition;
import dev.ftb.mods.ftbstuffnthings.lootmodifiers.CrookModifier;
import dev.ftb.mods.ftbstuffnthings.lootmodifiers.HammerModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Misc registry for anything that falls outside the scope of the other registry classes
 */
public class ContentRegistry {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FTBStuffNThings.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("obb_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("ftbstuff.itemGroup.tab"))
            .icon(() -> new ItemStack(BlocksRegistry.getSluice(SluiceType.OAK).get()))
            .displayItems((parameters, output) -> {
                for (DeferredHolder<Item, ? extends Item> entry : ItemsRegistry.ITEMS.getEntries()) {
                    output.accept(new ItemStack(entry.get()));
                }
            }).build());

    //-----------------------------------------------

    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, FTBStuffNThings.MOD_ID);

    public static final Supplier<MenuType<TemperedJarMenu>> TEMPERED_JAR_MENU = registerMenu("tempered_jar", TemperedJarMenu::fromNetwork);
    public static final Supplier<MenuType<FusingMachineMenu>> FUSING_MACHINE_MENU = registerMenu("fusing_machine", FusingMachineMenu::new);
    public static final Supplier<MenuType<SuperCoolerMenu>> SUPER_COOLER_MENU = registerMenu("super_cooler", SuperCoolerMenu::new);
    public static final Supplier<MenuType<WaterStrainerMenu>> WATER_STRAINER_MENU = registerMenu("water_strainer", WaterStrainerMenu::new);

    //-----------------------------------------------

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS
            = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, FTBStuffNThings.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<DevEnvironmentCondition>> DEV_ENVIRONMENT
            = CONDITIONS.register("dev_environment", () -> DevEnvironmentCondition.CODEC);

    //-----------------------------------------------

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS_REGISTRY
            = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FTBStuffNThings.MOD_ID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> HAMMER_LOOT_MODIFIER
            = LOOT_MODIFIERS_REGISTRY.register("hammer_loot_modifier", HammerModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> CROOK_LOOT_MODIFIER
            = LOOT_MODIFIERS_REGISTRY.register("crook_loot_modifier", CrookModifier.CODEC);


    //-----------------------------------------------

    public static void init(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
        MENU_TYPES.register(bus);
        CONDITIONS.register(bus);
        LOOT_MODIFIERS_REGISTRY.register(bus);
    }

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> Supplier<T> registerMenu(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return MENU_TYPES.register(name, () -> (T) IMenuTypeExtension.create(f));
    }
}
