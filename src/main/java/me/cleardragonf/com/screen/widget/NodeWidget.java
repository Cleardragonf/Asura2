package me.cleardragonf.com.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

public class NodeWidget extends AbstractWidget {
    private boolean dragging = false;
    private int dragOffX, dragOffY;
    private final CompoundTag step; // contains id and params
    private boolean selected = false;

    public NodeWidget(int x, int y, int w, int h, CompoundTag step) {
        super(x, y, w, h, Component.literal(step.getString("id")));
        this.step = step;
    }

    public CompoundTag getStep() { return step; }
    public void setSelected(boolean s) { this.selected = s; }
    public boolean isSelected() { return selected; }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int bg = isHoveredOrFocused() ? 0xAA303040 : 0xAA202630;
        g.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
        g.drawString(Minecraft.getInstance().font, this.getMessage(), getX() + 6, getY() + 6, 0xFFFFFF, false);
        // border if selected
        if (selected) {
            int c = 0xFF60A0FF;
            g.fill(getX(), getY(), getX() + getWidth(), getY() + 1, c);
            g.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), c);
            g.fill(getX(), getY(), getX() + 1, getY() + getHeight(), c);
            g.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), c);
        }
        // Show params if present
        int y = getY() + 18;
        if (step.contains("amt")) {
            g.drawString(Minecraft.getInstance().font, Component.literal("amt=" + step.getFloat("amt")), getX() + 6, y, 0xC0FFC0, false);
            y += 10;
        }
        if (step.contains("strength")) {
            g.drawString(Minecraft.getInstance().font, Component.literal("strength=" + String.format("%.2f", step.getDouble("strength"))), getX() + 6, y, 0xC0C0FF, false);
            y += 10;
        }
        if (step.contains("block")) {
            g.drawString(Minecraft.getInstance().font, Component.literal(step.getString("block")), getX() + 6, y, 0xFFC0C0, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == 0) {
            dragging = true;
            dragOffX = (int)mouseX - getX();
            dragOffY = (int)mouseY - getY();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            this.setX((int)mouseX - dragOffX);
            this.setY((int)mouseY - dragOffY);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
