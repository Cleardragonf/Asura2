package me.cleardragonf.com.network.msg;

import me.cleardragonf.com.item.WandItem;
import me.cleardragonf.com.spell.program.ProgramExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import me.cleardragonf.com.util.ItemData;

import java.util.function.Supplier;

public class C2SSetProgramChain {
    public final String mode;
    public final CompoundTag chain;

    public C2SSetProgramChain(String mode, CompoundTag chain) {
        this.mode = mode;
        this.chain = chain;
    }

    public static void encode(C2SSetProgramChain msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.mode);
        buf.writeNbt(msg.chain);
    }

    public static C2SSetProgramChain decode(FriendlyByteBuf buf) {
        String m = buf.readUtf();
        CompoundTag tag = buf.readNbt();
        return new C2SSetProgramChain(m, tag == null ? new CompoundTag() : tag);
    }

    public static void handle(C2SSetProgramChain msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            var held = sp.getMainHandItem();
            if (!(held.getItem() instanceof WandItem)) return;
            ListTag steps = msg.chain.getList(ProgramExecutor.KEY_STEPS, net.minecraft.nbt.Tag.TAG_COMPOUND);
            CompoundTag tag = ItemData.getOrCreate(held);
            ProgramExecutor.setChain(tag, msg.mode, steps);
            ItemData.set(held, tag);
        });
        ctx.setPacketHandled(true);
    }
}

