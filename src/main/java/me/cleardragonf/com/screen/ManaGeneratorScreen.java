package me.cleardragonf.com.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.menu.ManaGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ManaGeneratorScreen extends AbstractContainerScreen<ManaGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/mana_generator.png");

    public ManaGeneratorScreen(ManaGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw background
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Example mana bar (fill from 0–100 for now)
        int manaBarWidth = (int) (getManaLevel() * 100 / getManaMax());
        guiGraphics.fillGradient(x + 38, y + 65, x + 38 + manaBarWidth, y + 75, 0xFF00AFFF, 0xFF0077AA);
    }

    private int getManaLevel() {
        return 60; // TODO: Pull actual mana from block entity sync
    }

    private int getManaMax() {
        return 100; // placeholder max
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, "Mana Generator", 8, 6, 0x88CCFF, false);
        guiGraphics.drawString(this.font, "Mana: " + getManaLevel() + "/" + getManaMax(), 8, 20, 0x00FFFF, false);
        guiGraphics.drawString(this.font, "Type: Water", 8, 32, 0x66CCFF, false);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
