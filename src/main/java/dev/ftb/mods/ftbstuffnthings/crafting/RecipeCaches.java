package dev.ftb.mods.ftbstuffnthings.crafting;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.strainer.WaterStrainerBlockEntity;
import dev.ftb.mods.ftbstuffnthings.crafting.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public interface RecipeCaches {
    RecipeMultiCache<JarRecipe> TEMPERED_JAR = new RecipeMultiCache<>();
    RecipeCache<DripperRecipe> DRIPPER = new RecipeCache<>();
    RecipeCache<WoodenBasinRecipe> WOODEN_BASIN = new RecipeCache<>();
    RecipeCache<FusingMachineRecipe> FUSING_MACHINE = new RecipeCache<>();
    RecipeCache<SuperCoolerRecipe> SUPER_COOLER = new RecipeCache<>();
    RecipeCache<SluiceRecipe> SLUICE = new RecipeCache<>();
    RecipeCache<HammerRecipe> HAMMER = new RecipeCache<>();
    RecipeMultiCache<CrookRecipe> CROOK = new RecipeMultiCache<>();

    static void clearAll() {
        TEMPERED_JAR.clear();
        DRIPPER.clear();
        WOODEN_BASIN.clear();
        FUSING_MACHINE.clear();
        SUPER_COOLER.clear();
        SLUICE.clear();
        HAMMER.clear();
        CROOK.clear();

        WaterStrainerBlockEntity.clearCachedLootTable();

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.schedule(server.wrapRunnable(
                    () -> server.getPlayerList().getPlayers().forEach(FTBStuffNThings::syncLootSummaries)
            ));
        }
    }
}
