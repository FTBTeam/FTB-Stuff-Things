package dev.ftb.mods.ftbstuffnthings.mixin;

import dev.ftb.mods.ftbstuffnthings.blocks.woodbasin.WoodenBasinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method="fallOn", at = @At("RETURN"))
    public void onFallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (level.getBlockState(pos.below()).getBlock() instanceof WoodenBasinBlock basin) {
            basin.onEntityFall(entity, pos.below(), fallDistance);
        }
    }
}
