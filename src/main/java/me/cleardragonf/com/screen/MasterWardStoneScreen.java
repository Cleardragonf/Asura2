package me.cleardragonf.com.screen;

import me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.menu.MasterWardStoneMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class MasterWardStoneScreen extends AbstractContainerScreen<MasterWardStoneMenu> {
    private java.util.List<String> cachedNames = new java.util.ArrayList<>();
    private java.util.List<Boolean> cachedActives = new java.util.ArrayList<>();
    private int cachedLinked = -1;
    private int scrollIndex = 0;
    private int selectedIndex = -1;
    private net.minecraft.nbt.CompoundTag selectedOptions = new net.minecraft.nbt.CompoundTag();

    // Column layout for ward list (relative to GUI top-left)
    private static final int LIST_LEFT = 12;
    private static final int LIST_TOP = 118;
    private static final int LIST_WIDTH = 148;  // total width including scrollbar gutter
    private static final int LIST_HEIGHT = 279;
    private static final net.minecraft.resources.ResourceLocation BASE_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/master_ward_stone.png");
    private static final ResourceLocation WARD_SUMMARY_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/ward_summary.png");
    private static final ResourceLocation STAR_ON_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/ward_star_on.png");
    private static final ResourceLocation STAR_OFF_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/ward_star_off.png");
    private static final ResourceLocation LED_ON_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/ward_led_on.png");
    private static final ResourceLocation LED_OFF_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/gui/ward_led_off.png");

    private static final int ROW_HEIGHT = 44;
    private static final int ROW_GAP = 8; // distance between items

    private static boolean hasTexture(ResourceLocation loc) {
        try {
            return net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(loc).isPresent();
        } catch (Throwable t) {
            return false;
        }
    }

    public MasterWardStoneScreen(MasterWardStoneMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 620;
        this.imageHeight = 410;
        // Ask server for the latest data when the screen opens
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                    new me.cleardragonf.com.network.msg.C2SRequestWardData(menu.getPos()),
                    conn.getConnection());
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // Draw background texture for the full GUI
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, BASE_TEX);
        g.blit(BASE_TEX, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(menu.getPos());
        int linked;
        List<String> names;
        List<Boolean> acts;
        // Prefer cached snapshot if present to avoid empty client BE state
        if (!cachedNames.isEmpty()) {
            linked = (cachedLinked >= 0) ? cachedLinked : 0;
            names = cachedNames;
            acts = cachedActives;
        } else if (be instanceof MasterWardStoneBlockEntity master) {
            linked = master.getWardStones().size();
            names = master.getWardNames();
            acts = master.getWardActivesInOrder();
            if (selectedIndex >= 0 && selectedIndex < names.size()) {
                selectedOptions = master.getOptionsForIndex(selectedIndex);
            }
        } else {
            linked = 0;
            names = java.util.List.of();
            acts = java.util.List.of();
        }

        // Optional count overlay (keep lightweight; background art should dominate)
        g.drawString(this.font, "Linked Ward Stones: " + linked, x + 12, y + 12, 0xA0E0FF);

        // Section titles (centered in their header slots)
        drawCenteredTitle(g,
                net.minecraft.network.chat.Component.translatable("gui.asura.master_ward_stone.list_title"),
                x + 15, y + 90, 142, 20, 0xFFEDEDED);
        drawCenteredTitle(g,
                net.minecraft.network.chat.Component.translatable("gui.asura.master_ward_stone.options_title"),
                x + 176, y + 89, 219, 21, 0xFFEDEDED);

        int listX = x + LIST_LEFT;
        int listY = y + LIST_TOP;
        int listW = LIST_WIDTH;
        int listH = LIST_HEIGHT;

        // Background image provides the frame; draw items + scrollbar

        int rowStride = ROW_HEIGHT + ROW_GAP;
        int maxLines = Math.max(1, (listH - 8) / rowStride);
        int total = names.size();
        int maxStart = Math.max(0, total - maxLines);
        if (scrollIndex > maxStart) scrollIndex = maxStart;
        if (scrollIndex < 0) scrollIndex = 0;

        // Draw visible slice
        int end = Math.min(total, scrollIndex + maxLines);
        int drawY = listY + 4;
        int mouseXRel = (int) (Minecraft.getInstance().mouseHandler.xpos() / (double)Minecraft.getInstance().getWindow().getGuiScale());
        int mouseYRel = (int) (Minecraft.getInstance().mouseHandler.ypos() / (double)Minecraft.getInstance().getWindow().getGuiScale());
        boolean drewTooltip = false;
        for (int i = scrollIndex; i < end; i++) {
            String label = names.get(i);
            // Row background (fallback to simple rounded-ish rect if texture missing)
            if (hasTexture(WARD_SUMMARY_TEX)) {
                com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, WARD_SUMMARY_TEX);
                g.blit(WARD_SUMMARY_TEX, listX + 2, drawY, 0, 0, listW - 10, ROW_HEIGHT, listW - 10, ROW_HEIGHT);
            } else {
                int rx1 = listX + 2, ry1 = drawY, rx2 = rx1 + (listW - 10), ry2 = ry1 + ROW_HEIGHT;
                g.fill(rx1, ry1, rx2, ry2, 0x22000000);
                g.hLine(rx1, rx2, ry1, 0x44333333);
                g.hLine(rx1, rx2, ry2 - 1, 0x44111111);
                g.vLine(rx1, ry1, ry2, 0x44333333);
                g.vLine(rx2 - 1, ry1, ry2, 0x44111111);
            }

            boolean active = (i < acts.size()) && Boolean.TRUE.equals(acts.get(i));
            ResourceLocation ledTex = active ? LED_ON_TEX : LED_OFF_TEX;
            int ledX = listX + 8;
            int ledY = drawY + (ROW_HEIGHT - 10) / 2;
            if (hasTexture(ledTex)) {
                com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, ledTex);
                g.blit(ledTex, ledX, ledY, 0, 0, 10, 10, 10, 10);
            } else {
                g.fill(ledX, ledY, ledX + 10, ledY + 10, active ? 0xFF00FF66 : 0xFF2A2A2A);
            }

            ResourceLocation starTex = active ? STAR_ON_TEX : STAR_OFF_TEX;
            int starX = listX + listW - 24;
            int starY = drawY + (ROW_HEIGHT - 12) / 2;
            if (hasTexture(starTex)) {
                com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, starTex);
                g.blit(starTex, starX, starY, 0, 0, 12, 12, 12, 12);
            } else {
                g.fill(starX, starY, starX + 12, starY + 12, active ? 0xFFE6A800 : 0xFF333333);
            }

            int color = (i == selectedIndex) ? 0xFFFFFFFF : 0xFFE6A8;
            g.drawString(this.font, label, listX + 24, drawY + (ROW_HEIGHT - 8) / 2, color);

            int left = listX + 2, right = left + (listW - 10), top = drawY, bottom = drawY + ROW_HEIGHT;
            if (!drewTooltip && mouseXRel >= left && mouseXRel <= right && mouseYRel >= top && mouseYRel <= bottom) {
                g.renderTooltip(this.font,
                        java.util.List.of(Component.literal(label), Component.literal("Active: " + (active ? "Yes" : "No"))),
                        java.util.Optional.empty(), mouseXRel, mouseYRel);
                drewTooltip = true;
            }

            drawY += rowStride;
        }

        // Scrollbar
        if (total > maxLines) {
            int sbX = listX + listW - 6;
            int sbY = listY + 2;
            int sbH = listH - 4;
            g.fill(sbX, sbY, sbX + 4, sbY + sbH, 0x33333333);
            float ratio = maxLines / (float) total;
            int thumbH = Math.max(12, (int) (sbH * ratio));
            int maxThumbTravel = sbH - thumbH;
            int thumbY = sbY + (int) ((scrollIndex / (float) maxStart) * (maxThumbTravel));
            g.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, 0x99FFFFFF);
        }

        // Options panel (right side)
        renderOptionsPanel(g, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Only scroll if mouse is within the list bounds
        int left = (width - imageWidth) / 2 + LIST_LEFT;
        int top = (height - imageHeight) / 2 + LIST_TOP;
        if (mouseX >= left && mouseX <= left + LIST_WIDTH && mouseY >= top && mouseY <= top + LIST_HEIGHT) {
            int dir = deltaY > 0 ? -1 : 1;
            this.scrollIndex += dir;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - imageWidth) / 2 + LIST_LEFT;
        int top = (height - imageHeight) / 2 + LIST_TOP;
        if (mouseX >= left && mouseX <= left + LIST_WIDTH && mouseY >= top && mouseY <= top + LIST_HEIGHT) {
            int yIn = (int)(mouseY - top) - 4;
            int rowStride = ROW_HEIGHT + ROW_GAP;
            if (yIn >= 0) {
                int localIndex = yIn / rowStride;
                this.selectedIndex = scrollIndex + localIndex;
                // Notify server of selection; it will echo back details (and selection index)
                var conn = Minecraft.getInstance().getConnection();
                if (conn != null) {
                    me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                            new me.cleardragonf.com.network.msg.C2SSelectWard(menu.getPos(), this.selectedIndex),
                            conn.getConnection());
                }
                return true;
            }
        }
        // Handle star toggle if clicking in star area of selected row
        if (handleStarClick(mouseX, mouseY)) return true;
        // Handle option clicks
        if (handleOptionClick(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    public net.minecraft.core.BlockPos getPos() {
        return menu.getPos();
    }

    public void updateWardData(java.util.List<String> names, java.util.List<Boolean> actives, int linked, int selectedIndex, net.minecraft.nbt.CompoundTag options) {
        this.cachedNames = new java.util.ArrayList<>(names);
        // Store actives parallel to names; if sizes mismatch, pad false
        this.cachedActives = new java.util.ArrayList<>();
        for (int i = 0; i < this.cachedNames.size(); i++) {
            this.cachedActives.add(i < actives.size() && Boolean.TRUE.equals(actives.get(i)));
        }
        this.cachedLinked = linked;
        if (selectedIndex >= 0 && selectedIndex < this.cachedNames.size())
            this.selectedIndex = selectedIndex;
        if (options != null) this.selectedOptions = options;
    }

    // ---- Options panel rendering and interaction ----
    private static final int OPT_LEFT = 170;
    private static final int OPT_TOP = 117;
    private static final int OPT_WIDTH = 232;
    private static final int OPT_HEIGHT = 131;
    private int optScroll = 0;

    private void renderOptionsPanel(GuiGraphics g, int guiLeft, int guiTop) {
        int x = guiLeft + OPT_LEFT;
        int y = guiTop + OPT_TOP;
        int w = OPT_WIDTH;
        int h = OPT_HEIGHT;
        // Draw simple outline inside background art
        g.hLine(x, x + w, y, 0x22FFFFFF);
        g.hLine(x, x + w, y + h - 1, 0x22111111);
        g.vLine(x, y, y + h, 0x22FFFFFF);
        g.vLine(x + w - 1, y, y + h, 0x22111111);

        // List boolean options from selectedOptions
        int rowH = 14;
        java.util.List<String> keys = new java.util.ArrayList<>(selectedOptions.getAllKeys());
        int max = Math.max(1, (h - 8) / rowH);
        int start = Math.min(optScroll, Math.max(0, keys.size() - max));
        int end = Math.min(keys.size(), start + max);
        int drawY = y + 4;
        for (int i = start; i < end; i++) {
            String k = keys.get(i);
            boolean v = selectedOptions.getBoolean(k);
            // Checkbox square
            int cbX = x + 6;
            int cbY = drawY + 2;
            g.fill(cbX, cbY, cbX + 10, cbY + 10, 0xFF333333);
            if (v) g.fill(cbX + 2, cbY + 2, cbX + 8, cbY + 8, 0xFF00FF66);
            g.drawString(this.font, k, cbX + 14, drawY + 2, 0xFFE0E0E0);
            drawY += rowH;
        }
    }

    private void drawCenteredTitle(GuiGraphics g, net.minecraft.network.chat.Component text,
                                   int rectLeft, int rectTop, int rectWidth, int rectHeight, int color) {
        int textW = this.font.width(text);
        int x = rectLeft + Math.max(0, (rectWidth - textW) / 2);
        int y = rectTop + Math.max(0, (rectHeight - this.font.lineHeight) / 2);
        g.drawString(this.font, text, x, y, color);
    }

    private boolean handleOptionClick(double mouseX, double mouseY) {
        int left = (width - imageWidth) / 2 + OPT_LEFT;
        int top = (height - imageHeight) / 2 + OPT_TOP;
        if (mouseX < left || mouseX > left + OPT_WIDTH || mouseY < top || mouseY > top + OPT_HEIGHT) return false;
        // Determine which option row
        int yIn = (int)mouseY - top - 4;
        int rowH = 14;
        int idxLocal = yIn / rowH + optScroll;
        java.util.List<String> keys = new java.util.ArrayList<>(selectedOptions.getAllKeys());
        if (idxLocal >= 0 && idxLocal < keys.size()) {
            String key = keys.get(idxLocal);
            boolean v = selectedOptions.getBoolean(key);
            selectedOptions.putBoolean(key, !v);
            // Send to server to persist for this ward
            var conn = Minecraft.getInstance().getConnection();
            if (conn != null && selectedIndex >= 0) {
                me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                        new me.cleardragonf.com.network.msg.C2SSetWardOptionBool(menu.getPos(), selectedIndex, key, !v),
                        conn.getConnection());
            }
            return true;
        }
        return false;
    }

    private boolean handleStarClick(double mouseX, double mouseY) {
        // Star bounds for currently visible rows
        int baseLeft = (width - imageWidth) / 2 + LIST_LEFT;
        int baseTop = (height - imageHeight) / 2 + LIST_TOP;
        int rowStride = ROW_HEIGHT + ROW_GAP;
        int startY = baseTop + 4;
        // Determine which row, then check star rect near right edge
        int yIn = (int)mouseY - startY;
        if (yIn < 0) return false;
        int localIndex = yIn / rowStride;
        int index = scrollIndex + localIndex;
        int starX = baseLeft + LIST_WIDTH - 24;
        int starY = startY + localIndex * rowStride + (ROW_HEIGHT - 12) / 2;
        if (mouseX >= starX && mouseX <= starX + 12 && mouseY >= starY && mouseY <= starY + 12) {
            var conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(
                        new me.cleardragonf.com.network.msg.C2SToggleWardActive(menu.getPos(), index),
                        conn.getConnection());
            }
            return true;
        }
        return false;
    }
}
