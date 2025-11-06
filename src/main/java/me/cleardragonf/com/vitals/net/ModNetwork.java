package me.cleardragonf.com.vitals.net;

import me.cleardragonf.com.Asura;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Asura.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    private static int id = 0;
    public static void register() {
        CHANNEL.messageBuilder(VitalsSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VitalsSyncPacket::encode)
                .decoder(VitalsSyncPacket::decode)
                .consumerMainThread(VitalsSyncPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    private ModNetwork() {}
}
