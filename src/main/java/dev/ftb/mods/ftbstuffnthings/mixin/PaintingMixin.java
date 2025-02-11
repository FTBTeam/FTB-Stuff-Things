package dev.ftb.mods.ftbstuffnthings.mixin;

import dev.ftb.mods.ftbstuffnthings.FTBStuffTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Painting.class)
public abstract class PaintingMixin extends HangingEntity {

    protected PaintingMixin(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "dropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/Painting;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), cancellable = true)
    public void dropItem(Entity brokenEntity, CallbackInfo ci) {
        Painting painting = (Painting) (Object) this;
        Holder<PaintingVariant> variant = painting.getVariant();

        if (variant.is(FTBStuffTags.Painting.DROPS_WITH_VARIANT)) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString(Entity.ID_TAG, BuiltInRegistries.ENTITY_TYPE.getKey(getType()).toString());
            Painting.VARIANT_CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), variant)
                    .ifSuccess((tag) -> compoundTag.merge((CompoundTag) tag));

            ItemStack itemStack = new ItemStack(Items.PAINTING);
            itemStack.set(DataComponents.ENTITY_DATA, CustomData.of(compoundTag));

            this.spawnAtLocation(itemStack);
            ci.cancel();
        }
    }
}
