package me.cleardragonf.com.spell.impl;

import me.cleardragonf.com.spell.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class WaterBoltSpell implements Spell {
    private static final int COLOR = 0x3399FF;
    private static final ParticleOptions PARTICLE = new DustParticleOptions(new org.joml.Vector3f(0.2f, 0.5f, 1.0f), 1.0f);

    @Override public String id() { return "asura:water_bolt"; }
    @Override public int color() { return COLOR; }
    @Override public ParticleOptions trailParticle() { return PARTICLE; }
    @Override public CastSupport castSupport() { return CastSupport.PROJECTILE; }

    @Override
    public void onHitEntity(ServerLevel level, Entity target, LivingEntity caster, int spellLevel) {
        if (target.isAlive()) {
            if (target instanceof LivingEntity le) {
                DamageSource src = level.damageSources().magic();
                float dmg = 3.0f + 1.0f * Math.max(0, spellLevel - 1);
                le.hurt(src, dmg);
            }
            Vec3 push = target.position().subtract(caster.position()).normalize().scale(0.35 + 0.05 * spellLevel);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.1, push.z));
        }
    }

    @Override
    public void onHitBlock(ServerLevel level, BlockPos pos, LivingEntity caster, int spellLevel) {
        if (level.getBlockState(pos).is(Blocks.FIRE)) {
            level.removeBlock(pos, false);
        }
        for (int i = 0; i < 10 + spellLevel; i++) {
            double dx = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
            double dy = pos.getY() + 0.5 + level.random.nextDouble() * 0.4;
            double dz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
            level.sendParticles(PARTICLE, dx, dy, dz, 1, 0, 0, 0, 0);
        }
    }
}
