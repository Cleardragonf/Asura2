package me.cleardragonf.com.network.msg;

import me.cleardragonf.com.item.WandItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SLeftClickCast {
    public static void encode(C2SLeftClickCast msg, FriendlyByteBuf buf) {}
    public static C2SLeftClickCast decode(FriendlyByteBuf buf) { return new C2SLeftClickCast(); }

    public static void handle(C2SLeftClickCast msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            if (sp.level() instanceof ServerLevel sl) {
                var held = sp.getMainHandItem();
                if (held.getItem() instanceof WandItem) {
                    WandItem.castAway(sl, sp, held);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

