package dev.ftb.mods.ftbstuffnthings.network;

import dev.ftb.mods.ftbstuffnthings.FTBStuffNThings;
import dev.ftb.mods.ftbstuffnthings.blocks.jar.TemperedJarMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Received on: SERVER<br>
 * Sent by client when Mix/Stop button is pressed on the jar GUI
 */
public enum ToggleJarCraftingPacket implements CustomPacketPayload {
    INSTANCE;

    public static final Type<ToggleJarCraftingPacket> TYPE = new Type<>(FTBStuffNThings.id("start_jar_crafting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleJarCraftingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendToServer() {
        ClientPacketDistributor.sendToServer(INSTANCE);
    }

    public static void handleData(ToggleJarCraftingPacket ignored, IPayloadContext context) {
        if (context.player().containerMenu instanceof TemperedJarMenu menu) {
            menu.getJar().toggleCrafting();
        }
    }
}
