package me.cleardragonf.com.spell.program;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ProgramExecutor {
    public static final String KEY_CHAIN = "ProgramChain"; // legacy single-chain
    public static final String KEY_CHAIN_SELF = "ProgramChainSelf";
    public static final String KEY_CHAIN_PROJECTILE = "ProgramChainProjectile";
    public static final String KEY_MODE = "mode"; // "self" or "projectile"
    public static final String KEY_STEPS = "steps"; // list of compounds

    public static boolean hasChain(CompoundTag tag, String mode) {
        if (tag == null) return false;
        // Prefer mode-specific keys
        if ("self".equals(mode) && tag.contains(KEY_CHAIN_SELF, Tag.TAG_COMPOUND)) {
            CompoundTag c = tag.getCompound(KEY_CHAIN_SELF);
            return c.contains(KEY_STEPS, Tag.TAG_LIST);
        }
        if ("projectile".equals(mode) && tag.contains(KEY_CHAIN_PROJECTILE, Tag.TAG_COMPOUND)) {
            CompoundTag c = tag.getCompound(KEY_CHAIN_PROJECTILE);
            return c.contains(KEY_STEPS, Tag.TAG_LIST);
        }
        // Legacy single chain with mode
        if (tag.contains(KEY_CHAIN, Tag.TAG_COMPOUND)) {
            CompoundTag chain = tag.getCompound(KEY_CHAIN);
            return mode.equals(chain.getString(KEY_MODE)) && chain.contains(KEY_STEPS, Tag.TAG_LIST);
        }
        return false;
    }

    public static CompoundTag getChain(CompoundTag tag, String mode) {
        if (!hasChain(tag, mode)) return null;
        if ("self".equals(mode) && tag.contains(KEY_CHAIN_SELF, Tag.TAG_COMPOUND)) return tag.getCompound(KEY_CHAIN_SELF);
        if ("projectile".equals(mode) && tag.contains(KEY_CHAIN_PROJECTILE, Tag.TAG_COMPOUND)) return tag.getCompound(KEY_CHAIN_PROJECTILE);
        return tag.getCompound(KEY_CHAIN);
    }

    public static void setChain(CompoundTag tag, String mode, ListTag steps) {
        CompoundTag chain = new CompoundTag();
        chain.putString(KEY_MODE, mode);
        chain.put(KEY_STEPS, steps);
        // Store both under legacy KEY_CHAIN and mode-specific key
        tag.put(KEY_CHAIN, chain);
        if ("self".equals(mode)) tag.put(KEY_CHAIN_SELF, chain.copy());
        if ("projectile".equals(mode)) tag.put(KEY_CHAIN_PROJECTILE, chain.copy());
    }

    // Execute for self-cast (Charm)
    public static void executeSelf(ServerLevel level, LivingEntity caster, CompoundTag chain, int spellLevel) {
        if (chain == null) return;
        ListTag steps = chain.getList(KEY_STEPS, Tag.TAG_COMPOUND);
        for (Tag t : steps) {
            if (!(t instanceof CompoundTag step)) continue;
            runStepSelf(level, caster, spellLevel, step);
        }
    }

    // Execute for projectile hit on entity
    public static void executeOnEntity(ServerLevel level, LivingEntity caster, Entity target, CompoundTag chain, int spellLevel) {
        if (chain == null) return;
        ListTag steps = chain.getList(KEY_STEPS, Tag.TAG_COMPOUND);
        for (Tag t : steps) {
            if (!(t instanceof CompoundTag step)) continue;
            runStepEntity(level, caster, target, spellLevel, step);
        }
    }

    // Execute for projectile hit on block
    public static void executeOnBlock(ServerLevel level, LivingEntity caster, BlockPos pos, CompoundTag chain, int spellLevel) {
        if (chain == null) return;
        ListTag steps = chain.getList(KEY_STEPS, Tag.TAG_COMPOUND);
        for (Tag t : steps) {
            if (!(t instanceof CompoundTag step)) continue;
            runStepBlock(level, caster, pos, spellLevel, step);
        }
    }

    private static void runStepSelf(ServerLevel level, LivingEntity caster, int spellLevel, CompoundTag step) {
        String id = step.getString("id");
        switch (id) {
            case "particle_heart" -> level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY(0.75), caster.getZ(), 10 + spellLevel, 0.3, 0.4, 0.3, 0);
            case "heal_self" -> {
                int dur = step.getInt("dur");
                int amp = step.getInt("amp");
                if (dur <= 0) dur = 100 + 20 * (spellLevel - 1);
                if (amp < 0) amp = Math.max(0, (spellLevel - 1) / 2);
                caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, amp));
            }
            case "teleport_self_forward" -> {
                double dist = step.contains("dist") ? step.getDouble("dist") : 5.0;
                var look = caster.getLookAngle().normalize();
                var dest = caster.position().add(look.scale(dist));
                caster.teleportTo(dest.x, dest.y, dest.z);
                level.sendParticles(ParticleTypes.POOF, dest.x, dest.y + 0.2, dest.z, 8, 0.2, 0.2, 0.2, 0);
            }
            case "summon" -> {
                String ent = step.getString("entity");
                try {
                    EntityType<?> type = EntityType.byString(ent).orElse(null);
                    if (type != null) {
                        Entity e = type.create(level);
                        if (e != null) {
                            e.moveTo(caster.getX(), caster.getY(), caster.getZ(), caster.getYRot(), caster.getXRot());
                            level.addFreshEntity(e);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static void runStepEntity(ServerLevel level, LivingEntity caster, Entity target, int spellLevel, CompoundTag step) {
        String id = step.getString("id");
        switch (id) {
            case "particle_heart" -> level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(0.5), target.getZ(), 6 + spellLevel, 0.2, 0.4, 0.2, 0);
            case "damage_target" -> {
                float amt = step.contains("amt") ? step.getFloat("amt") : (3.0f + spellLevel);
                if (target instanceof LivingEntity le) le.hurt(level.damageSources().magic(), amt);
            }
            case "push_target" -> {
                double s = step.contains("strength") ? step.getDouble("strength") : (0.35 + 0.05 * spellLevel);
                var dir = target.position().subtract(caster.position()).normalize().scale(s);
                target.setDeltaMovement(target.getDeltaMovement().add(dir.x, 0.1, dir.z));
            }
            case "effect_target" -> {
                if (target instanceof LivingEntity le) {
                    String eff = step.getString("effect");
                    int dur = step.contains("dur") ? step.getInt("dur") : 100;
                    int amp = step.contains("amp") ? step.getInt("amp") : 0;
                    var key = net.minecraft.resources.ResourceLocation.tryParse(eff);
                    var reg = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS;
                    var effect = (key != null) ? reg.getValue(key) : null;
                    if (effect != null) le.addEffect(new MobEffectInstance(effect, dur, amp));
                }
            }
            case "teleport_target_up" -> {
                double h = step.contains("height") ? step.getDouble("height") : 5.0;
                target.teleportTo(target.getX(), target.getY() + h, target.getZ());
            }
            case "remove_block" -> {
                // No-op in entity context
            }
        }
    }

    private static void runStepBlock(ServerLevel level, LivingEntity caster, BlockPos pos, int spellLevel, CompoundTag step) {
        String id = step.getString("id");
        switch (id) {
            case "particle_blue" -> level.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6 + spellLevel, 0.2, 0.2, 0.2, 0);
            case "place_block" -> {
                String blockId = step.getString("block");
                try {
                    Block b = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    if (b != null) {
                        BlockState st = b.defaultBlockState();
                        level.setBlock(pos, st, 3);
                    }
                } catch (Exception ignored) {}
            }
            case "remove_block" -> level.removeBlock(pos, false);
            case "summon_at" -> {
                String ent = step.getString("entity");
                try {
                    EntityType<?> type = EntityType.byString(ent).orElse(null);
                    if (type != null) {
                        Entity e = type.create(level);
                        if (e != null) {
                            e.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, caster.getYRot(), caster.getXRot());
                            level.addFreshEntity(e);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
