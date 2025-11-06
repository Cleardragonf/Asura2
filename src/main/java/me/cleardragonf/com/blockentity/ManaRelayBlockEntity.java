package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.registry.ModBlockEntities;
import me.cleardragonf.com.api.ManaReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManaRelayBlockEntity extends BlockEntity {

    // Multiple inputs (generators or relays), single output (relay or battery)
    private java.util.Set<BlockPos> inputs = new java.util.HashSet<>();
    private BlockPos output;

    public static final int TRANSFER_PER_TICK = 50;
    private static final int BUFFER_MAX = 10000;
    private int buffer = 0; // stored mana waiting to be forwarded

    public ManaRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_RELAY_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;

        // Pull from inputs up to TRANSFER_PER_TICK into buffer
        int pullBudget = Math.min(TRANSFER_PER_TICK, Math.max(0, BUFFER_MAX - buffer));
        if (pullBudget > 0 && !inputs.isEmpty()) {
            for (BlockPos in : new java.util.HashSet<>(inputs)) {
                if (pullBudget <= 0) break;
                BlockEntity sbe = level.getBlockEntity(in);
                if (sbe instanceof ManaGeneratorBlockEntity gen) {
                    int available = gen.getMana();
                    if (available > 0) {
                        int taken = Math.min(pullBudget, available);
                        taken = gen.extractMana(taken);
                        if (taken > 0) {
                            buffer += taken;
                            pullBudget -= taken;
                            spawnBeamParticles(level, in, worldPosition);
                        }
                    }
                } else if (sbe instanceof ManaRelayBlockEntity relay) {
                    int taken = relay.pullFromBuffer(pullBudget);
                    if (taken > 0) {
                        buffer += taken;
                        pullBudget -= taken;
                        spawnBeamParticles(level, in, worldPosition);
                    }
                } else if (sbe == null) {
                    // source unloaded/removed: keep it, user may fix later
                }
            }
        }

        // Push from buffer to output up to TRANSFER_PER_TICK
        if (buffer > 0 && output != null) {
            int pushBudget = Math.min(TRANSFER_PER_TICK, buffer);
            BlockEntity tbe = level.getBlockEntity(output);
            if (tbe instanceof ManaReceiver sink) {
                int accepted = sink.receiveMana(pushBudget);
                if (accepted > 0) {
                    buffer -= accepted;
                    spawnBeamParticles(level, worldPosition, output);
                }
            }
        }

        setChanged();
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

    // Linking API
    public void addInput(BlockPos pos) {
        if (pos == null) return;
        this.inputs.add(pos);
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void removeInput(BlockPos pos) {
        if (pos == null) return;
        this.inputs.remove(pos);
        setChanged();
    }

    public java.util.Set<BlockPos> getInputs() { return java.util.Collections.unmodifiableSet(inputs); }

    public void setOutput(BlockPos pos) {
        this.output = pos;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public BlockPos getOutput() { return output; }

    // Relay internal buffer API
    public int receiveMana(int amount) {
        if (amount <= 0) return 0;
        int space = Math.max(0, BUFFER_MAX - buffer);
        int accepted = Math.min(space, amount);
        buffer += accepted;
        return accepted;
    }

    public int pullFromBuffer(int amount) {
        if (amount <= 0) return 0;
        int taken = Math.min(buffer, amount);
        buffer -= taken;
        return taken;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        // Back-compat single source (write first input if present)
        if (!inputs.isEmpty()) {
            BlockPos s = inputs.iterator().next();
            tag.putInt("sx", s.getX()); tag.putInt("sy", s.getY()); tag.putInt("sz", s.getZ());
        }
        if (output != null) {
            tag.putInt("tx", output.getX()); tag.putInt("ty", output.getY()); tag.putInt("tz", output.getZ());
        }
        // New format: list of inputs
        ListTag list = new ListTag();
        for (BlockPos p : inputs) {
            CompoundTag e = new CompoundTag();
            e.putInt("x", p.getX()); e.putInt("y", p.getY()); e.putInt("z", p.getZ());
            list.add(e);
        }
        tag.put("inputs", list);
        tag.putInt("buffer", buffer);
    }

    public void load(CompoundTag tag) {
        inputs.clear();
        if (tag.contains("inputs", Tag.TAG_LIST)) {
            ListTag list = tag.getList("inputs", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                if (t instanceof CompoundTag e) {
                    inputs.add(new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z")));
                }
            }
        } else if (tag.contains("sx")) {
            inputs.add(new BlockPos(tag.getInt("sx"), tag.getInt("sy"), tag.getInt("sz")));
        }
        if (tag.contains("tx")) {
            output = new BlockPos(tag.getInt("tx"), tag.getInt("ty"), tag.getInt("tz"));
        } else {
            output = null;
        }
        buffer = tag.getInt("buffer");
        buffer = Math.min(Math.max(buffer, 0), BUFFER_MAX);
        setChanged();
    }

    // Preferred signature (provider unused but matches 1.21 loader)
    public void load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        load(tag);
    }

    // Some loader/mapping paths call this in 1.21+
    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        load(tag);
    }

    // Networking for client sync
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, null);
        return tag;
    }
}
