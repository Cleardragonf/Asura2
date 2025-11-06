package me.cleardragonf.com.spell.impl;

import me.cleardragonf.com.spell.Spell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class HealPulseSpell implements Spell {
    @Override public String id() { return "asura:heal_pulse"; }
    @Override public int color() { return 0xFF66AA; }
    @Override public ParticleOptions trailParticle() { return ParticleTypes.HEART; }
    @Override public CastSupport castSupport() { return CastSupport.SELF; }

    @Override
    public void onSelfCast(ServerLevel level, LivingEntity caster, net.minecraft.world.item.ItemStack wand, int spellLevel) {
        int dur = 80 + 20 * Math.max(0, spellLevel - 1); // 4s base, +1s/level
        int amp = Math.max(0, (spellLevel - 1) / 2);
        caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, amp));
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY(0.75), caster.getZ(), 8 + spellLevel, 0.3, 0.4, 0.3, 0);
    }
}
