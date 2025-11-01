package me.cleardragonf.com.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.menu.ManaGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ManaGeneratorScreen extends AbstractContainerScreen<ManaGeneratorMenu> {
    private static final ResourceLocation BASE_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/mana_generator.png");

    // === Alignment Offsets ===
    // Adjust to ensure the spinning rings line up perfectly with your new etched circles.
    private static final float CENTER_DX = 0f;   // Left/right adjustment
    private static final float CENTER_DY = 1f; // Up/down adjustment

    public ManaGeneratorScreen(ManaGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;  // displayed size (scaled down)
        this.imageHeight = 256;
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        double centerX = x + imageWidth / 2.0 + CENTER_DX;
        double centerY = y + imageHeight / 2.0 + CENTER_DY;

        // === Draw Base Background ===
        RenderSystem.setShaderTexture(0, BASE_TEX);
        guiGraphics.blit(BASE_TEX, x, y, imageWidth, imageHeight, imageWidth, imageHeight);

        // === Animated Arcane Rings ===
        long time = System.currentTimeMillis();

        float outerRotation  = (time % 20000L) / 20000.0f * 360.0f; // Slow CCW
        float middleRotation = (time % 10000L) / 10000.0f * 360.0f; // Medium CW
        float innerRotation  = (time % 4000L)  / 4000.0f  * 360.0f; // Fast CCW

        renderRotatingRing(guiGraphics, centerX, centerY,
                "textures/gui/mana_ring_outer.png", -outerRotation);   // CCW
        renderRotatingRing(guiGraphics, centerX, centerY,
                "textures/gui/mana_ring_middle.png", middleRotation);  // CW
        renderRotatingRing(guiGraphics, centerX, centerY,
                "textures/gui/mana_ring_inner.png", -innerRotation);   // CCW
    }

    /** Draws a rotating texture centered around the GUI */
    private void renderRotatingRing(GuiGraphics g, double cx, double cy, String texturePath, float rotationDeg) {
        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Asura.MODID, texturePath);
        RenderSystem.setShaderTexture(0, tex);

        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(rotationDeg));

        // Since your ring textures are 1024x1024, but centered smaller visually,
        // adjust to draw them at a reasonable scale (like 400x400) so they overlap correctly.
        g.blit(tex, -200, -200, 0, 0, 400, 400, 400, 400);

        g.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The title “Mana Collection” is now baked into the texture,
        // so we skip drawing text here to keep it clean.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
