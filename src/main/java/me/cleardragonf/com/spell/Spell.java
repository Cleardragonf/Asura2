package me.cleardragonf.com.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface Spell {
    enum CastSupport { SELF, PROJECTILE, BOTH }

    String id();
    int color(); // RGB for UI if needed
    ParticleOptions trailParticle();
    CastSupport castSupport();
    default int maxLevel() { return 5; }

    // Self-cast is considered a "Charm"
    default boolean isCharm() { return castSupport() == CastSupport.SELF; }

    // Called when casting begins (both modes)
    default void onCast(ServerLevel level, LivingEntity caster, ItemStack wand, int spellLevel) {}

    // Self-cast effect (Charm)
    default void onSelfCast(ServerLevel level, LivingEntity caster, ItemStack wand, int spellLevel) {}

    // Projectile flight hook and impacts
    default void onTick(ServerLevel level, Entity projectile, int spellLevel) {}
    default void onHitEntity(ServerLevel level, Entity target, LivingEntity caster, int spellLevel) {}
    default void onHitBlock(ServerLevel level, BlockPos pos, LivingEntity caster, int spellLevel) {}

    default double speedMultiplier(int spellLevel) { return 1.0 + 0.05 * Math.max(0, spellLevel - 1); }
}
