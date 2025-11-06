package me.cleardragonf.com.vitals.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.vitals.IPlayerVitals;
import me.cleardragonf.com.vitals.PlayerVitalsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Asura.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VitalsOverlay {

    public static final IGuiOverlay OVERLAY = (gui, guiGraphics, partialTick, width, height) -> {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || player.isSpectator()) return;

        player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
            int x = 10;
            int y = height - 50;
            drawThirstBar(guiGraphics, x, y, v);
            drawTempBar(guiGraphics, x, y - 12, v);
        });
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("asura_vitals", OVERLAY);
    }

    private static void drawThirstBar(GuiGraphics g, int x, int y, IPlayerVitals v) {
        int max = 100;
        int val = Math.max(0, Math.min(max, v.getThirst()));
        int w = 80;
        int h = 6;
        // background
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xAA000000);
        // bar color: blue
        int bar = (int) Math.round(w * (val / (double) max));
        g.fill(x, y, x + bar, y + h, 0xAA2E86FF);
    }

    private static void drawTempBar(GuiGraphics g, int x, int y, IPlayerVitals v) {
        int min = -100, max = 100;
        int val = Math.max(min, Math.min(max, v.getTemperature()));
        int w = 80;
        int h = 6;
        // background
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xAA000000);
        // 0 position in the middle
        int mid = x + w / 2;
        // left (cold) is cyan, right (hot) is red
        if (val < 0) {
            int len = (int) Math.round((w / 2.0) * (-val / 100.0));
            g.fill(mid - len, y, mid, y + h, 0xAA00FFFF);
        } else if (val > 0) {
            int len = (int) Math.round((w / 2.0) * (val / 100.0));
            g.fill(mid, y, mid + len, y + h, 0xAAF44336);
        }
        // center marker
        g.fill(mid, y, mid + 1, y + h, 0xAAFFFFFF);
    }
}

