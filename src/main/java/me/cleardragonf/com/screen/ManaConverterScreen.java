package me.cleardragonf.com.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.menu.ManaConverterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ManaConverterScreen extends AbstractContainerScreen<ManaConverterMenu> {
    private static final ResourceLocation GUI_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/mana_generator.png");

    public ManaConverterScreen(ManaConverterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(GUI_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight);
        

        // Simple progress bars for buffer vs. cost
        int buf = this.menu.getBuffer();
        int cost = this.menu.getCost();
        if (cost > 0) {
            int w = Math.min(150, (int)(150.0 * Math.min(1.0, buf / (double)cost)));
            // draw a filled bar (reuse a slice of GUI texture or a solid color rect)
            // Using a colored rect for simplicity
            g.fill(x + 13, y + 20, x + 13 + w, y + 28, 0xAA00B0FF);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g, mouseX, mouseY, partialTicks);
        super.render(g, mouseX, mouseY, partialTicks);
        this.renderTooltip(g, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int buf = this.menu.getBuffer();
        int cost = this.menu.getCost();
        Component c = Component.literal("Mana: " + buf + (cost > 0 ? (" / cost " + cost) : ""));
        g.drawString(this.font, c, x + 8, y + 6, 0x4040FF, false);
    }
}
