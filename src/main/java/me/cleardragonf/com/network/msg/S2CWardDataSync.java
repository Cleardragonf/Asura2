package me.cleardragonf.com.network.msg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.ArrayList;
import java.util.List;

public class S2CWardDataSync {
    public final BlockPos pos;
    public final List<String> names;
    public final List<Boolean> actives;
    public final int linkedCount;
    public final int selectedIndex;
    public final net.minecraft.nbt.CompoundTag selectedOptions;

    public S2CWardDataSync(BlockPos pos, List<String> names, List<Boolean> actives, int linkedCount, int selectedIndex,
                           net.minecraft.nbt.CompoundTag selectedOptions) {
        this.pos = pos;
        this.names = names;
        this.actives = actives;
        this.linkedCount = linkedCount;
        this.selectedIndex = selectedIndex;
        this.selectedOptions = selectedOptions == null ? new net.minecraft.nbt.CompoundTag() : selectedOptions;
    }

    public static void encode(S2CWardDataSync msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.linkedCount);
        buf.writeVarInt(msg.names.size());
        for (String s : msg.names) buf.writeUtf(s);
        buf.writeVarInt(msg.actives.size());
        for (Boolean b : msg.actives) buf.writeBoolean(b);
        buf.writeVarInt(msg.selectedIndex);
        buf.writeNbt(msg.selectedOptions);
    }

    public static S2CWardDataSync decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int linked = buf.readVarInt();
        int n = buf.readVarInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(buf.readUtf());
        int m = buf.readVarInt();
        List<Boolean> acts = new ArrayList<>();
        for (int i = 0; i < m; i++) acts.add(buf.readBoolean());
        int sel = buf.readVarInt();
        net.minecraft.nbt.CompoundTag opts = buf.readNbt();
        return new S2CWardDataSync(pos, list, acts, linked, sel, opts == null ? new net.minecraft.nbt.CompoundTag() : opts);
    }

    public static void handle(S2CWardDataSync msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // Store on the screen if open
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof me.cleardragonf.com.screen.MasterWardStoneScreen s && s.getPos().equals(msg.pos)) {
                s.updateWardData(msg.names, msg.actives, msg.linkedCount, msg.selectedIndex, msg.selectedOptions);
            }
            // Also attempt to reflect into client BE
            var be = mc.level != null ? mc.level.getBlockEntity(msg.pos) : null;
            if (be instanceof me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity master) {
                // Minimal merge: if BE still empty, populate for display
                if (master.getWardNames().isEmpty()) {
                    // Create a copy through NBT route for safety
                    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                    net.minecraft.nbt.ListTag wn = new net.minecraft.nbt.ListTag();
                    for (String sName : msg.names) {
                        net.minecraft.nbt.CompoundTag e = new net.minecraft.nbt.CompoundTag();
                        e.putString("n", sName);
                        wn.add(e);
                    }
                    tag.put("ward_names", wn);
                    master.handleUpdateTag(tag, null);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
