package me.cleardragonf.com.item;

import me.cleardragonf.com.entity.SpellProjectile;
import me.cleardragonf.com.registry.ModEntityTypes;
import me.cleardragonf.com.spell.Spell;
import me.cleardragonf.com.spell.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WandItem extends Item {
    public WandItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            // Cycle selected spell
            if (!level.isClientSide) {
                var learned = getLearned(stack);
                if (!learned.isEmpty()) {
                    int sel = stack.getOrCreateTag().getInt("Selected");
                    sel = (sel + 1) % learned.size();
                    setSelectedIndex(stack, sel);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Selected: " + learned.get(sel)), true);
                }
            }
            return InteractionResultHolder.success(stack);
        }
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        // Right-click = self-cast (Charm) via ProgramChain if present, else via Spell
        var tag = stack.getOrCreateTag();
        if (me.cleardragonf.com.spell.program.ProgramExecutor.hasChain(tag, "self")) {
            int lvl = 1; // could use level per node later
            ServerLevel sl = (ServerLevel) level;
            me.cleardragonf.com.spell.program.ProgramExecutor.executeSelf(sl, player, me.cleardragonf.com.spell.program.ProgramExecutor.getChain(tag, "self"), lvl);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.6f);
            player.getCooldowns().addCooldown(this, 8);
            return InteractionResultHolder.success(stack);
        }

        String spellId = getProgramActionForType(stack, "self");
        if (spellId.isEmpty()) spellId = getSelectedSpell(stack);
        Spell spell = SpellRegistry.get(spellId);
        if (spell != null && (spell.castSupport() == Spell.CastSupport.SELF || spell.castSupport() == Spell.CastSupport.BOTH)) {
            int lvl = getSpellLevel(stack, spellId);
            ServerLevel sl = (ServerLevel) level;
            spell.onCast(sl, player, stack, lvl);
            spell.onSelfCast(sl, player, stack, lvl);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.6f);
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    // NBT helpers: learned spells + selected index
    public static List<String> getLearned(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        var list = new ArrayList<String>();
        if (tag.contains("Spells", 9)) { // list of strings
            var l = tag.getList("Spells", 8);
            for (int i = 0; i < l.size(); i++) list.add(l.getString(i));
        }
        return list;
    }

    public static void setLearned(ItemStack stack, List<String> spells) {
        CompoundTag tag = stack.getOrCreateTag();
        net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
        for (String s : spells) l.add(net.minecraft.nbt.StringTag.valueOf(s));
        tag.put("Spells", l);
    }

    public static String getSelectedSpell(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int idx = tag.getInt("Selected");
        List<String> learned = getLearned(stack);
        if (learned.isEmpty()) return "";
        idx = Math.max(0, Math.min(idx, learned.size() - 1));
        return learned.get(idx);
    }

    public static void setSelectedIndex(ItemStack stack, int idx) {
        stack.getOrCreateTag().putInt("Selected", idx);
    }

    public static void ensureDefaultSpells(ItemStack stack) {
        List<String> learned = getLearned(stack);
        if (learned.isEmpty()) {
            learned.add("asura:water_bolt");
            learned.add("asura:heal_pulse");
            setLearned(stack, learned);
            setSelectedIndex(stack, 0);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, LivingEntity entity, int slot, boolean selected) {
        if (!level.isClientSide) ensureDefaultSpells(stack);
    }

    // Per-spell level handling
    public static int getSpellLevel(ItemStack stack, String id) {
        var tag = stack.getOrCreateTag();
        if (tag.contains("SpellLevels", 10)) {
            var m = tag.getCompound("SpellLevels");
            int v = m.getInt(id);
            return v <= 0 ? 1 : v;
        }
        return 1;
    }

    public static void setSpellLevel(ItemStack stack, String id, int level) {
        var tag = stack.getOrCreateTag();
        var m = tag.getCompound("SpellLevels");
        m.putInt(id, Math.max(1, level));
        tag.put("SpellLevels", m);
    }

    // Cast away (projectile) used from input handler
    public static boolean castAway(ServerLevel level, Player player, ItemStack stack) {
        // 1) ProgramChain projectile mode
        var tag = stack.getOrCreateTag();
        if (me.cleardragonf.com.spell.program.ProgramExecutor.hasChain(tag, "projectile")) {
            SpellProjectile proj = me.cleardragonf.com.registry.ModEntityTypes.SPELL_PROJECTILE.get().create(level);
            if (proj == null) return false;
            proj.setOwner(player);
            proj.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            proj.setSpellId("");
            proj.setSpellLevel(1);
            proj.getPersistentData().put(me.cleardragonf.com.spell.program.ProgramExecutor.KEY_CHAIN,
                    me.cleardragonf.com.spell.program.ProgramExecutor.getChain(tag, "projectile").copy());
            var look = player.getLookAngle();
            proj.shoot(look.x, look.y, look.z, 1.5f, 0.0f);
            level.addFreshEntity(proj);
            level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7f, 1.2f);
            player.getCooldowns().addCooldown(stack.getItem(), 8);
            return true;
        }

        // 2) Fallback to Spell projectile
        String spellId = getProgramActionForType(stack, "projectile");
        if (spellId.isEmpty()) spellId = getSelectedSpell(stack);
        Spell spell = SpellRegistry.get(spellId);
        if (spell == null) return false;
        if (!(spell.castSupport() == Spell.CastSupport.PROJECTILE || spell.castSupport() == Spell.CastSupport.BOTH)) return false;

        SpellProjectile proj = me.cleardragonf.com.registry.ModEntityTypes.SPELL_PROJECTILE.get().create(level);
        if (proj == null) return false;
        proj.setOwner(player);
        proj.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        proj.setSpellId(spellId);
        int lvl = getSpellLevel(stack, spellId);
        proj.setSpellLevel(lvl);
        double speed = 1.5 * spell.speedMultiplier(lvl);
        var look = player.getLookAngle();
        proj.shoot(look.x, look.y, look.z, (float) speed, 0.0f);
        level.addFreshEntity(proj);

        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7f, 1.2f);
        player.getCooldowns().addCooldown(stack.getItem(), 8);
        spell.onCast(level, player, stack, lvl);
        return true;
    }

    private static String getProgramActionForType(ItemStack stack, String type) {
        var tag = stack.getOrCreateTag();
        if (tag.contains("Program", 10)) {
            var p = tag.getCompound("Program");
            if (type.equals(p.getString("type"))) {
                return p.getString("action");
            }
        }
        return "";
    }
}
