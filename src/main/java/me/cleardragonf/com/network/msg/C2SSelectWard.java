package me.cleardragonf.com.network.msg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SSelectWard {
    public final BlockPos pos;
    public final int index;
    public C2SSelectWard(BlockPos pos, int index) {
        this.pos = pos; this.index = index;
    }

    public static void encode(C2SSelectWard msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.index);
    }

    public static C2SSelectWard decode(FriendlyByteBuf buf) {
        return new C2SSelectWard(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(C2SSelectWard msg, CustomPayloadEvent.Context ctx) {
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
                if (msg.index >= 0 && msg.index < names.size()) {
                    options = master.getOptionsForIndex(msg.index);
                }
                me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                        new S2CWardDataSync(msg.pos, names, actives, linked, msg.index, options),
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(sp));
            }
        });
        ctx.setPacketHandled(true);
    }
}
