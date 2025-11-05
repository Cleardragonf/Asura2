package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManaRelayBlockEntity extends BlockEntity {

    private BlockPos source; // ManaGenerator position
    private BlockPos target; // Any ManaBattery block position (controller or member)

    public static final int TRANSFER_PER_TICK = 50;

    public ManaRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_RELAY_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        if (source == null || target == null) return;

        BlockEntity sbe = level.getBlockEntity(source);
        BlockEntity tbe = level.getBlockEntity(target);
        if (!(sbe instanceof ManaGeneratorBlockEntity gen)) return;
        if (!(tbe instanceof ManaBatteryBlockEntity bat)) return;

        int available = gen.getMana();
        if (available <= 0) return;
        int toSend = Math.min(TRANSFER_PER_TICK, available);
        int accepted = bat.addMana(toSend);
        if (accepted > 0) {
            gen.extractMana(accepted);
            spawnBeamParticles(level, worldPosition, source);
            spawnBeamParticles(level, worldPosition, target);
            setChanged();
        }
    }

    private void spawnBeamParticles(Level lvl, BlockPos from, BlockPos to) {
        // simple particle beam along the line
        double sx = from.getX() + 0.5, sy = from.getY() + 0.5, sz = from.getZ() + 0.5;
        double tx = to.getX() + 0.5, ty = to.getY() + 0.5, tz = to.getZ() + 0.5;
        int steps = 8 + (int) (from.distManhattan(to) * 4);
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = sx + (tx - sx) * t;
            double y = sy + (ty - sy) * t;
            double z = sz + (tz - sz) * t;
            if (lvl instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
            }
        }
    }

    public void setSource(BlockPos pos) {
        this.source = pos;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setTarget(BlockPos pos) {
        this.target = pos;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public BlockPos getSource() { return source; }
    public BlockPos getTarget() { return target; }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        if (source != null) {
            tag.putInt("sx", source.getX()); tag.putInt("sy", source.getY()); tag.putInt("sz", source.getZ());
        }
        if (target != null) {
            tag.putInt("tx", target.getX()); tag.putInt("ty", target.getY()); tag.putInt("tz", target.getZ());
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains("sx")) {
            source = new BlockPos(tag.getInt("sx"), tag.getInt("sy"), tag.getInt("sz"));
        }
        if (tag.contains("tx")) {
            target = new BlockPos(tag.getInt("tx"), tag.getInt("ty"), tag.getInt("tz"));
        }
    }
}
