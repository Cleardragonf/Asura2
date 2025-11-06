package me.cleardragonf.com.client;

import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import me.cleardragonf.com.blockentity.ManaConverterBlockEntity;
import me.cleardragonf.com.blockentity.ManaGeneratorBlockEntity;
import me.cleardragonf.com.blockentity.ManaRelayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugHudTicker {
    private static int tick;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;

        tick++;
        if ((tick % 10) != 0) return; // throttle
        if (!Screen.hasShiftDown()) return; // only while holding Shift

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr)) return;
        BlockPos pos = bhr.getBlockPos();
        var be = mc.level.getBlockEntity(pos);
        if (be == null) return;

        String msg;
        if (be instanceof ManaBatteryBlockEntity b) {
            msg = String.format("Battery @%d,%d,%d ctrl=%s stored=%d cap=%d",
                    pos.getX(), pos.getY(), pos.getZ(), b.isController(), b.getStored(), b.getCapacity());
        } else if (be instanceof ManaRelayBlockEntity r) {
            String out = r.getOutput() == null ? "-" : (r.getOutput().getX()+","+r.getOutput().getY()+","+r.getOutput().getZ());
            msg = String.format("Relay @%d,%d,%d inputs=%d out=%s",
                    pos.getX(), pos.getY(), pos.getZ(), r.getInputs().size(), out);
        } else if (be instanceof ManaGeneratorBlockEntity g) {
            msg = String.format("Generator @%d,%d,%d mana=%d type=%s",
                    pos.getX(), pos.getY(), pos.getZ(), g.getMana(), g.getManaType());
        } else if (be instanceof ManaConverterBlockEntity c) {
            int buf = c.getContainerData().get(0);
            int cost = c.getContainerData().get(1);
            String in = c.getInput().isEmpty()?"<empty>":c.getInput().getHoverName().getString();
            int outCount = c.getOutput().isEmpty()?0:c.getOutput().getCount();
            msg = String.format("Converter @%d,%d,%d buf=%d cost=%d in=%s out=%d",
                    pos.getX(), pos.getY(), pos.getZ(), buf, cost, in, outCount);
        } else {
            return;
        }
        mc.player.displayClientMessage(Component.literal(msg), true); // action bar
    }
}

