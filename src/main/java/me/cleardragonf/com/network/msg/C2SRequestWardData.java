package me.cleardragonf.com.network.msg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SRequestWardData {
    public final BlockPos pos;
    public C2SRequestWardData(BlockPos pos) { this.pos = pos; }

    public static void encode(C2SRequestWardData msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static C2SRequestWardData decode(FriendlyByteBuf buf) {
        return new C2SRequestWardData(buf.readBlockPos());
    }

    public static void handle(C2SRequestWardData msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            var level = sp.serverLevel();
            var be = level.getBlockEntity(msg.pos);
            if (be instanceof me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity master) {
                java.util.List<String> names = new java.util.ArrayList<>(master.getWardNames());
                java.util.List<Boolean> actives = master.getWardActivesInOrder();
                int linked = master.getWardStones().size();
                net.minecraft.nbt.CompoundTag options = new net.minecraft.nbt.CompoundTag();
                me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                        new S2CWardDataSync(msg.pos, names, actives, linked, -1, options),
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(sp));
            }
        });
        ctx.setPacketHandled(true);
    }
}
