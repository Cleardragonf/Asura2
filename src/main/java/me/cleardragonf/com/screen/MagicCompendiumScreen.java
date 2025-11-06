package me.cleardragonf.com.screen;

import me.cleardragonf.com.menu.MagicCompendiumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import me.cleardragonf.com.screen.widget.NodeWidget;
import net.minecraft.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MagicCompendiumScreen extends AbstractContainerScreen<MagicCompendiumMenu> {
    private Button btnModeSelf;
    private Button btnModeProjectile;
    private Button btnHeal;
    private Button btnDmg;
    private Button btnPush;
    private Button btnHeart;
    private Button btnBlue;
    private Button btnPlace;
    private Button btnRemove;
    private Button btnClear;
    private Button btnSave;
    private final List<NodeWidget> nodes = new ArrayList<>();
    private String mode = "self";
    private NodeWidget selected;
    private EditBox fldAmt, fldStrength, fldDur, fldAmp, fldDist, fldHeight, fldBlock, fldEffect;

    public MagicCompendiumScreen(MagicCompendiumMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        btnModeSelf = Button.builder(Component.literal("Mode: Self (Charm)"), b -> { mode = "self"; loadNodesFromWand(); })
                .bounds(x + 10, y + 26, 156, 18).build();
        btnModeProjectile = Button.builder(Component.literal("Mode: Projectile"), b -> { mode = "projectile"; loadNodesFromWand(); })
                .bounds(x + 10, y + 46, 156, 18).build();

        btnHeal = Button.builder(Component.literal("Add: Heal Self"), b -> { if (menu.clickMenuButton(this.minecraft.player, 200)) addNode("heal_self", new CompoundTag()); })
                .bounds(x + 10, y + 70, 156, 18).build();
        btnDmg = Button.builder(Component.literal("Add: Damage Target"), b -> { if (menu.clickMenuButton(this.minecraft.player, 201)) { CompoundTag t=new CompoundTag(); t.putFloat("amt",4.0f); addNode("damage_target", t);} })
                .bounds(x + 10, y + 90, 156, 18).build();
        btnPush = Button.builder(Component.literal("Add: Push Target"), b -> { if (menu.clickMenuButton(this.minecraft.player, 202)) { CompoundTag t=new CompoundTag(); t.putDouble("strength",0.5); addNode("push_target", t);} })
                .bounds(x + 10, y + 110, 156, 18).build();
        btnHeart = Button.builder(Component.literal("Add: Particle Hearts"), b -> { if (menu.clickMenuButton(this.minecraft.player, 203)) addNode("particle_heart", new CompoundTag()); })
                .bounds(x + 10, y + 130, 156, 18).build();
        btnBlue = Button.builder(Component.literal("Add: Particle Blue"), b -> { if (menu.clickMenuButton(this.minecraft.player, 204)) addNode("particle_blue", new CompoundTag()); })
                .bounds(x + 10, y + 150, 156, 18).build();
        btnPlace = Button.builder(Component.literal("Add: Place Stone (on hit)"), b -> { if (menu.clickMenuButton(this.minecraft.player, 205)) { CompoundTag t=new CompoundTag(); t.putString("block","minecraft:stone"); addNode("place_block", t);} })
                .bounds(x + 10, y + 170, 156, 18).build();
        btnRemove = Button.builder(Component.literal("Add: Remove Block (on hit)"), b -> { if (menu.clickMenuButton(this.minecraft.player, 206)) addNode("remove_block", new CompoundTag()); })
                .bounds(x + 10, y + 190, 156, 18).build();

        btnClear = Button.builder(Component.literal("Clear"), b -> { nodes.clear(); menu.clickMenuButton(this.minecraft.player, 300); })
                .bounds(x + 10, y + 212, 76, 18).build();
        btnSave = Button.builder(Component.literal("Save"), b -> saveProgramChain())
                .bounds(x + 90, y + 212, 76, 18).build();

        addRenderableWidget(btnModeSelf);
        addRenderableWidget(btnModeProjectile);
        addRenderableWidget(btnHeal);
        addRenderableWidget(btnDmg);
        addRenderableWidget(btnPush);
        addRenderableWidget(btnHeart);
        addRenderableWidget(btnBlue);
        addRenderableWidget(btnPlace);
        addRenderableWidget(btnRemove);
        addRenderableWidget(btnClear);
        addRenderableWidget(btnSave);

        // Inspector fields for selected node parameters
        int ix = x + 10;
        int iy = y + 326;
        fldAmt = new EditBox(this.font, ix, iy, 74, 18, Component.literal("amt")); addRenderableWidget(fldAmt);
        fldStrength = new EditBox(this.font, ix + 82, iy, 74, 18, Component.literal("str")); addRenderableWidget(fldStrength);
        iy += 22;
        fldDur = new EditBox(this.font, ix, iy, 74, 18, Component.literal("dur")); addRenderableWidget(fldDur);
        fldAmp = new EditBox(this.font, ix + 82, iy, 74, 18, Component.literal("amp")); addRenderableWidget(fldAmp);
        iy += 22;
        fldDist = new EditBox(this.font, ix, iy, 74, 18, Component.literal("dist")); addRenderableWidget(fldDist);
        fldHeight = new EditBox(this.font, ix + 82, iy, 74, 18, Component.literal("height")); addRenderableWidget(fldHeight);
        iy += 22;
        fldBlock = new EditBox(this.font, ix, iy, 156, 18, Component.literal("block id")); addRenderableWidget(fldBlock);
        iy += 22;
        fldEffect = new EditBox(this.font, ix, iy, 156, 18, Component.literal("effect id")); addRenderableWidget(fldEffect);
        iy += 22;
        addRenderableWidget(Button.builder(Component.literal("Apply to Selected"), b -> applyInspector()).bounds(ix, iy, 156, 18).build());
        iy += 20;
        addRenderableWidget(Button.builder(Component.literal("Load from Selected"), b -> loadInspector()).bounds(ix, iy, 156, 18).build());

        // Controls to tweak last node
        addRenderableWidget(Button.builder(Component.literal("Remove Last"), b -> { if(menu.clickMenuButton(this.minecraft.player, 210) && !nodes.isEmpty()) nodes.remove(nodes.size()-1);} ).bounds(x + 10, y + 234, 156, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Damage +"), b -> menu.clickMenuButton(this.minecraft.player, 211)).bounds(x + 10, y + 256, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Damage -"), b -> menu.clickMenuButton(this.minecraft.player, 212)).bounds(x + 90, y + 256, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Push +"), b -> menu.clickMenuButton(this.minecraft.player, 213)).bounds(x + 10, y + 278, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Push -"), b -> menu.clickMenuButton(this.minecraft.player, 214)).bounds(x + 90, y + 278, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Cycle Block"), b -> menu.clickMenuButton(this.minecraft.player, 215)).bounds(x + 10, y + 300, 156, 18).build());

        // Load existing chain for current mode
        loadNodesFromWand();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        // Simple plain background
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight + 160, 0xAA101020);
        g.drawString(this.font, Component.literal("Magic Compendium"), x + 10, y + 10, 0xFFFFFF, false);

        // Node canvas area (right side)
        int cx = x + this.imageWidth + 16;
        int cy = y;
        int cw = Math.min(300, this.width - cx - 16);
        int ch = this.imageHeight + 160;
        g.fill(cx, cy, cx + cw, cy + ch, 0x66182024);
        g.drawString(this.font, Component.literal("Canvas (drag nodes)"), cx + 8, cy + 8, 0xC0C0C0, false);

        for (NodeWidget node : nodes) node.render(g, mouseX, mouseY, partialTicks);
    }

    private void addNode(String id, CompoundTag params) {
        CompoundTag step = params.copy();
        step.putString("id", id);
        int x = (this.width - this.imageWidth) / 2 + this.imageWidth + 24;
        int y = (this.height - this.imageHeight) / 2 + 30 + nodes.size() * 22;
        NodeWidget w = new NodeWidget(x, y, 140, 40, step);
        nodes.add(w);
    }

    private void saveProgramChain() {
        // Build ordered steps from current nodes (top-to-bottom order)
        nodes.sort(Comparator.comparingInt(NodeWidget::getY));
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (NodeWidget w : nodes) list.add(w.getStep().copy());
        net.minecraft.nbt.CompoundTag chain = new net.minecraft.nbt.CompoundTag();
        chain.putString("mode", mode);
        chain.put("steps", list);
        me.cleardragonf.com.network.AsuraNetwork.CHANNEL.sendToServer(
                new me.cleardragonf.com.network.msg.C2SSetProgramChain(mode, chain)
        );
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(Component.literal("Saved program (" + mode + ")"), true);
        }
    }

    private void loadNodesFromWand() {
        nodes.clear();
        var player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return;
        var stack = player.getMainHandItem();
        var tag = stack.getOrCreateTag();
        var chain = me.cleardragonf.com.spell.program.ProgramExecutor.getChain(tag, mode);
        if (chain == null) return;
        var list = chain.getList("steps", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (var t : list) {
            if (t instanceof net.minecraft.nbt.CompoundTag c) {
                addNode(c.getString("id"), c);
            }
        }
    }

    private void applyInspector() {
        if (selected == null) return;
        var s = selected.getStep();
        try { if (!fldAmt.getValue().isEmpty()) s.putFloat("amt", Float.parseFloat(fldAmt.getValue())); } catch (Exception ignored) {}
        try { if (!fldStrength.getValue().isEmpty()) s.putDouble("strength", Double.parseDouble(fldStrength.getValue())); } catch (Exception ignored) {}
        try { if (!fldDur.getValue().isEmpty()) s.putInt("dur", Integer.parseInt(fldDur.getValue())); } catch (Exception ignored) {}
        try { if (!fldAmp.getValue().isEmpty()) s.putInt("amp", Integer.parseInt(fldAmp.getValue())); } catch (Exception ignored) {}
        try { if (!fldDist.getValue().isEmpty()) s.putDouble("dist", Double.parseDouble(fldDist.getValue())); } catch (Exception ignored) {}
        try { if (!fldHeight.getValue().isEmpty()) s.putDouble("height", Double.parseDouble(fldHeight.getValue())); } catch (Exception ignored) {}
        if (!fldBlock.getValue().isEmpty()) s.putString("block", fldBlock.getValue());
        if (!fldEffect.getValue().isEmpty()) s.putString("effect", fldEffect.getValue());
    }

    private void loadInspector() {
        if (selected == null) return;
        var s = selected.getStep();
        fldAmt.setValue(s.contains("amt") ? Float.toString(s.getFloat("amt")) : "");
        fldStrength.setValue(s.contains("strength") ? Double.toString(s.getDouble("strength")) : "");
        fldDur.setValue(s.contains("dur") ? Integer.toString(s.getInt("dur")) : "");
        fldAmp.setValue(s.contains("amp") ? Integer.toString(s.getInt("amp")) : "");
        fldDist.setValue(s.contains("dist") ? Double.toString(s.getDouble("dist")) : "");
        fldHeight.setValue(s.contains("height") ? Double.toString(s.getDouble("height")) : "");
        fldBlock.setValue(s.contains("block") ? s.getString("block") : "");
        fldEffect.setValue(s.contains("effect") ? s.getString("effect") : "");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (NodeWidget n : nodes) {
            boolean over = n.isMouseOver(mouseX, mouseY);
            n.setSelected(false);
            if (over && !handled) { selected = n; n.setSelected(true); handled = true; }
            n.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean any = false;
        for (NodeWidget n : nodes) any |= n.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || any;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean any = false;
        for (NodeWidget n : nodes) any |= n.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button) || any;
    }
}
