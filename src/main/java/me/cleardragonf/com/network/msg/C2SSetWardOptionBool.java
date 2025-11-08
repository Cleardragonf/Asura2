package me.cleardragonf.com.network.msg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SSetWardOptionBool {
    public final BlockPos pos;
    public final int index;
    public final String key;
    public final boolean value;
    public C2SSetWardOptionBool(BlockPos pos, int index, String key, boolean value) {
        this.pos = pos; this.index = index; this.key = key; this.value = value;
    }

    public static void encode(C2SSetWardOptionBool msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.index);
        buf.writeUtf(msg.key);
        buf.writeBoolean(msg.value);
    }

    public static C2SSetWardOptionBool decode(FriendlyByteBuf buf) {
        return new C2SSetWardOptionBool(buf.readBlockPos(), buf.readVarInt(), buf.readUtf(), buf.readBoolean());
    }

    public static void handle(C2SSetWardOptionBool msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            var level = sp.serverLevel();
            var be = level.getBlockEntity(msg.pos);
            if (be instanceof me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity master) {
                java.util.List<String> names = new java.util.ArrayList<>(master.getWardNames());
                if (msg.index >= 0 && msg.index < names.size()) {
                    master.setOptionBoolIndex(msg.index, msg.key, msg.value);
                }
                // Echo back updated options for selected index
                java.util.List<Boolean> actives = master.getWardActivesInOrder();
                int linked = master.getWardStones().size();
                net.minecraft.nbt.CompoundTag options = new net.minecraft.nbt.CompoundTag();
                if (msg.index >= 0 && msg.index < names.size()) options = master.getOptionsForIndex(msg.index);
                me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                        new S2CWardDataSync(msg.pos, names, actives, linked, msg.index, options),
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(sp));
            }
        });
        ctx.setPacketHandled(true);
    }
}
