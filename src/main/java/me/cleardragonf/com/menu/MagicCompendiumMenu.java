package me.cleardragonf.com.menu;

import me.cleardragonf.com.item.WandItem;
import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MagicCompendiumMenu extends AbstractContainerMenu {
    private final Player player;
    private String mode = "self"; // default

    public MagicCompendiumMenu(int id, Inventory inv) {
        super(ModMenus.MAGIC_COMPENDIUM_MENU.get(), id);
        this.player = inv.player;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        // Button mapping:
        // 10 -> mode self, 11 -> mode projectile
        // 200.. = add nodes; 300 = clear; 301 = save
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof WandItem)) return false;
        var tag = held.getOrCreateTag();
        var steps = tag.getCompound("ProgramChainWorking").getList("steps", net.minecraft.nbt.Tag.TAG_COMPOUND);
        switch (buttonId) {
            case 10 -> { mode = "self"; tag.getOrCreateTag().putString("ProgramChainWorkingMode", mode); return true; }
            case 11 -> { mode = "projectile"; tag.getOrCreateTag().putString("ProgramChainWorkingMode", mode); return true; }
            case 200 -> { // heal_self
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "heal_self");
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 201 -> { // damage_target
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "damage_target");
                node.putFloat("amt", 4.0f);
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 202 -> { // push_target
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "push_target");
                node.putDouble("strength", 0.5);
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 203 -> { // particle_heart
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "particle_heart");
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 204 -> { // particle_blue
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "particle_blue");
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 205 -> { // place_block stone
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "place_block");
                node.putString("block", "minecraft:stone");
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 206 -> { // remove_block
                var node = new net.minecraft.nbt.CompoundTag();
                node.putString("id", "remove_block");
                steps.add(node); tag.getCompound("ProgramChainWorking").put("steps", steps); return true; }
            case 210 -> { // remove last
                if (!steps.isEmpty()) {
                    steps.remove(steps.size() - 1);
                    tag.getCompound("ProgramChainWorking").put("steps", steps);
                }
                return true; }
            case 211 -> { // inc damage on last
                if (!steps.isEmpty()) {
                    var last = (net.minecraft.nbt.CompoundTag) steps.get(steps.size() - 1);
                    if ("damage_target".equals(last.getString("id"))) {
                        float amt = last.contains("amt") ? last.getFloat("amt") : 4.0f;
                        last.putFloat("amt", Math.min(100f, amt + 1.0f));
                    }
                }
                return true; }
            case 212 -> { // dec damage on last
                if (!steps.isEmpty()) {
                    var last = (net.minecraft.nbt.CompoundTag) steps.get(steps.size() - 1);
                    if ("damage_target".equals(last.getString("id"))) {
                        float amt = last.contains("amt") ? last.getFloat("amt") : 4.0f;
                        last.putFloat("amt", Math.max(0f, amt - 1.0f));
                    }
                }
                return true; }
            case 213 -> { // inc push strength on last
                if (!steps.isEmpty()) {
                    var last = (net.minecraft.nbt.CompoundTag) steps.get(steps.size() - 1);
                    if ("push_target".equals(last.getString("id"))) {
                        double s = last.contains("strength") ? last.getDouble("strength") : 0.5;
                        last.putDouble("strength", Math.min(5.0, s + 0.1));
                    }
                }
                return true; }
            case 214 -> { // dec push strength on last
                if (!steps.isEmpty()) {
                    var last = (net.minecraft.nbt.CompoundTag) steps.get(steps.size() - 1);
                    if ("push_target".equals(last.getString("id"))) {
                        double s = last.contains("strength") ? last.getDouble("strength") : 0.5;
                        last.putDouble("strength", Math.max(0.0, s - 0.1));
                    }
                }
                return true; }
            case 215 -> { // cycle place_block block id on last
                if (!steps.isEmpty()) {
                    var last = (net.minecraft.nbt.CompoundTag) steps.get(steps.size() - 1);
                    if ("place_block".equals(last.getString("id"))) {
                        String cur = last.getString("block");
                        String[] options = new String[]{"minecraft:stone", "minecraft:cobblestone", "minecraft:dirt", "minecraft:oak_log", "minecraft:glass"};
                        int idx = 0;
                        for (int i = 0; i < options.length; i++) if (options[i].equals(cur)) { idx = i; break; }
                        idx = (idx + 1) % options.length;
                        last.putString("block", options[idx]);
                    }
                }
                return true; }
            case 300 -> { // clear
                tag.put("ProgramChainWorking", new net.minecraft.nbt.CompoundTag()); return true; }
            case 301 -> { // save -> ProgramChain
                var working = tag.getCompound("ProgramChainWorking");
                var list = working.getList("steps", net.minecraft.nbt.Tag.TAG_COMPOUND);
                if (list.isEmpty()) return false;
                var chain = new net.minecraft.nbt.CompoundTag();
                chain.putString("mode", tag.getString("ProgramChainWorkingMode").isEmpty() ? mode : tag.getString("ProgramChainWorkingMode"));
                chain.put("steps", list.copy());
                tag.put("ProgramChain", chain);
                return true; }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) { return true; }
}
