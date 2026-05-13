package dev.ftb.mods.ftbstuffnthings.items;

import net.minecraft.world.item.Item;

public class MeshItem extends Item {
    public final MeshType mesh;

    public MeshItem(Properties properties, MeshType m) {
        super(properties.stacksTo(16));
        this.mesh = m;
    }
}
