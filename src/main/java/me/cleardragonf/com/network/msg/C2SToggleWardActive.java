package me.cleardragonf.com.network.msg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SToggleWardActive {
    public final BlockPos pos;
    public final int index;
    public C2SToggleWardActive(BlockPos pos, int index) { this.pos = pos; this.index = index; }

    public static void encode(C2SToggleWardActive msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.index);
    }

    public static C2SToggleWardActive decode(FriendlyByteBuf buf) {
        return new C2SToggleWardActive(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(C2SToggleWardActive msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            var level = sp.serverLevel();
            var be = level.getBlockEntity(msg.pos);
            if (be instanceof me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity master) {
                java.util.List<String> names = new java.util.ArrayList<>(master.getWardNames());
                if (msg.index >= 0 && msg.index < names.size()) {
                    boolean nowActive = !master.isWardActiveIndex(msg.index);
                    master.setWardActiveIndex(msg.index, nowActive);
                }
                // Echo back the updated state including options for selected index
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
