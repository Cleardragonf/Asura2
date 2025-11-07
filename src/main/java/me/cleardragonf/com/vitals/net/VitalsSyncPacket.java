package me.cleardragonf.com.vitals.net;

import me.cleardragonf.com.vitals.PlayerVitalsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

public class VitalsSyncPacket {
    private final int temperature;
    private final int thirst;

    public VitalsSyncPacket(int temperature, int thirst) {
        this.temperature = temperature;
        this.thirst = thirst;
    }

    public static void encode(VitalsSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.temperature);
        buf.writeVarInt(pkt.thirst);
    }

    public static VitalsSyncPacket decode(FriendlyByteBuf buf) {
        int t = buf.readVarInt();
        int h = buf.readVarInt();
        return new VitalsSyncPacket(t, h);
    }

    public static void handle(VitalsSyncPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
                v.setTemperature(pkt.temperature);
                v.setThirst(pkt.thirst);
            });
        });
        ctx.setPacketHandled(true);
    }
}

