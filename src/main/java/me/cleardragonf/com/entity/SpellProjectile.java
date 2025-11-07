package me.cleardragonf.com.entity;

import me.cleardragonf.com.spell.Spell;
import me.cleardragonf.com.spell.SpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SpellProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<String> DATA_SPELL = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final String NBT_PROGRAM_CHAIN = me.cleardragonf.com.spell.program.ProgramExecutor.KEY_CHAIN;

    private int spellLevel = 1;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level, LivingEntity owner) {
        super(type, owner, level);
    }

    public void setSpellId(String id) {
        this.entityData.set(DATA_SPELL, id == null ? "" : id);
    }

    public String getSpellId() { return this.entityData.get(DATA_SPELL); }

    public Spell getSpell() { return SpellRegistry.get(getSpellId()); }
    public int getSpellLevel() { return spellLevel; }
    public void setSpellLevel(int lvl) { this.spellLevel = Math.max(1, lvl); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPELL, "");
    }

    @Override
    protected Item getDefaultItem() {
        // Not used for rendering (we use NoopRenderer); return air
        return net.minecraft.world.item.Items.AIR;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            Spell s = getSpell();
            if (s != null) s.onTick(sl, this, getSpellLevel());
            // trail particles
            if (s != null && s.trailParticle() != null) {
                sl.sendParticles(s.trailParticle(), this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            } else if (this.getPersistentData().contains(NBT_PROGRAM_CHAIN)) {
                // Basic trail if program configured: glow particle
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            Spell s = getSpell();
            if (s != null) s.onHitEntity(sl, hit.getEntity(), (LivingEntity) this.getOwner(), getSpellLevel());
            if (this.getPersistentData().contains(NBT_PROGRAM_CHAIN) && this.getOwner() instanceof LivingEntity caster) {
                me.cleardragonf.com.spell.program.ProgramExecutor.executeOnEntity(sl, (LivingEntity) caster, hit.getEntity(), this.getPersistentData().getCompound(NBT_PROGRAM_CHAIN), getSpellLevel());
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            Spell s = getSpell();
            if (s != null) s.onHitBlock(sl, hit.getBlockPos(), (LivingEntity) this.getOwner(), getSpellLevel());
            if (this.getPersistentData().contains(NBT_PROGRAM_CHAIN) && this.getOwner() instanceof LivingEntity caster) {
                me.cleardragonf.com.spell.program.ProgramExecutor.executeOnBlock(sl, (LivingEntity) caster, hit.getBlockPos(), this.getPersistentData().getCompound(NBT_PROGRAM_CHAIN), getSpellLevel());
            }
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("spell", getSpellId());
        tag.putInt("lvl", getSpellLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSpellId(tag.getString("spell"));
        if (tag.contains("lvl")) setSpellLevel(tag.getInt("lvl"));
    }
}
