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

    private static final float CENTER_DX = 0f;
    private static final float CENTER_DY = 1f;

    private static final int ICON_SIZE = 40;       // final on-screen size
    private static final int ICON_TEX_SIZE = 500;  // texture pixel size (500x500)
    private static final float ICON_SCALE = (float) ICON_SIZE / ICON_TEX_SIZE; // scale factor

    public ManaGeneratorScreen(ManaGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        double centerX = x + imageWidth / 2.0 + CENTER_DX;
        double centerY = y + imageHeight / 2.0 + CENTER_DY;

        // === Background ===
        RenderSystem.setShaderTexture(0, BASE_TEX);
        g.blit(BASE_TEX, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // === Rings ===
        long time = System.currentTimeMillis();
        float outerRotation = (time % 20000L) / 20000.0f * 360.0f;
        float middleRotation = (time % 10000L) / 10000.0f * 360.0f;
        float innerRotation = (time % 4000L) / 4000.0f * 360.0f;

        renderRotatingRing(g, centerX, centerY, "textures/gui/mana_ring_outer.png", -outerRotation);
        renderRotatingRing(g, centerX, centerY, "textures/gui/mana_ring_middle.png", middleRotation);
        renderRotatingRing(g, centerX, centerY, "textures/gui/mana_ring_inner.png", -innerRotation);

        // === Elements ===
        boolean[] elements = menu.getElements();
        String[] names = {"light", "dark", "fire", "water", "earth", "air"};

        double radius = 92.0;
        double angleStep = 360.0 / names.length;

        for (int i = 0; i < names.length; i++) {
            boolean active = elements.length > i && elements[i];
            String texName = "textures/gui/elements/element_" + names[i] + "_" + (active ? "bright" : "dull") + ".png";
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Asura.MODID, texName);

            double angleDeg = angleStep * i - 90;
            double angleRad = Math.toRadians(angleDeg);
            double iconCenterX = centerX + Math.cos(angleRad) * radius;
            double iconCenterY = centerY + Math.sin(angleRad) * radius;

            // ✅ Apply scaling using matrix transform
            g.pose().pushPose();
            g.pose().translate(iconCenterX - ICON_SIZE / 2f, iconCenterY - ICON_SIZE / 2f, 0);
            g.pose().scale(ICON_SCALE, ICON_SCALE, 1.0f);

            RenderSystem.setShaderTexture(0, tex);
            g.blit(tex, 0, 0, 0, 0, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE);

            g.pose().popPose();
        }
    }

    private void renderRotatingRing(GuiGraphics g, double cx, double cy, String texturePath, float rotationDeg) {
        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Asura.MODID, texturePath);
        RenderSystem.setShaderTexture(0, tex);
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(rotationDeg));
        g.blit(tex, -200, -200, 0, 0, 400, 400, 400, 400);
        g.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
