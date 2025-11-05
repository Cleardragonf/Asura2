package me.cleardragonf.com.screen;

import me.cleardragonf.com.menu.ManaBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ManaBatteryScreen extends AbstractContainerScreen<ManaBatteryMenu> {
    public ManaBatteryScreen(ManaBatteryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 90;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // Minimal UI: just a frame and text
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xAA000000);

        int stored = menu.getStored();
        int cap = menu.getCapacity();
        String text = "Stored: " + stored + " / " + cap;
        g.drawString(this.font, text, x + 10, y + 10, 0xFFFFFF);

        // Simple bar
        int barW = imageWidth - 20;
        int barX = x + 10;
        int barY = y + 30;
        g.fill(barX, barY, barX + barW, barY + 12, 0xFF333333);
        int fillW = cap > 0 ? (int)((stored / (float)cap) * barW) : 0;
        g.fill(barX, barY, barX + fillW, barY + 12, 0xFF00B2FF);
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

