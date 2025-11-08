package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class MasterWardStoneBlockEntity extends BlockEntity {

    private final Set<BlockPos> wardStones = new HashSet<>();
    private final List<String> wardNames = new ArrayList<>();
    // Per-entry active flags and options aligned by index in wardNames
    private final java.util.ArrayList<Boolean> wardActive = new java.util.ArrayList<>();
    private final java.util.ArrayList<net.minecraft.nbt.CompoundTag> wardOptions = new java.util.ArrayList<>();

    // Placeholder for memorized contents (future expansion)
    private final Set<BlockPos> memorizedBarrier = new HashSet<>();

    public MasterWardStoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MASTER_WARD_STONE_ENTITY.get(), pos, state);
    }

    // Linking API
    public void addWardStone(BlockPos pos) {
        if (pos == null) return;
        wardStones.add(pos.immutable());
        setChanged();
        syncToClients();
    }

    public void removeWardStone(BlockPos pos) {
        if (pos == null) return;
        wardStones.remove(pos);
        setChanged();
    }

    public Set<BlockPos> getWardStones() {
        return Collections.unmodifiableSet(wardStones);
    }

    // Ward names API (added via WardSpellItem)
    public void addWardName(String name) {
        if (name == null || name.isBlank()) return;
        wardNames.add(name);
        wardActive.add(Boolean.FALSE);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        ensureDefaultOptionsInto(name, tag);
        wardOptions.add(tag);
        setChanged();
        syncToClients();
        // Future hook: activate logic tied to this name
    }

    public List<String> getWardNames() {
        return Collections.unmodifiableList(wardNames);
    }

    public boolean isWardActiveIndex(int idx) {
        if (idx < 0 || idx >= wardActive.size()) return false;
        return Boolean.TRUE.equals(wardActive.get(idx));
    }

    public void setWardActiveIndex(int idx, boolean active) {
        if (idx < 0 || idx >= wardActive.size()) return;
        wardActive.set(idx, active);
        ensureDefaultOptionsIndex(idx);
        setChanged();
        syncToClients();
    }

    public java.util.List<Boolean> getWardActivesInOrder() {
        return new java.util.ArrayList<>(wardActive);
    }

    public net.minecraft.nbt.CompoundTag getOptionsForIndex(int idx) {
        ensureDefaultOptionsIndex(idx);
        if (idx < 0 || idx >= wardOptions.size()) return new net.minecraft.nbt.CompoundTag();
        return wardOptions.get(idx).copy();
    }

    public void setOptionBoolIndex(int idx, String key, boolean value) {
        if (idx < 0 || idx >= wardOptions.size()) return;
        net.minecraft.nbt.CompoundTag tag = wardOptions.get(idx);
        tag.putBoolean(key, value);
        setChanged();
        syncToClients();
    }

    private void ensureDefaultOptionsIndex(int idx) {
        if (idx < 0 || idx >= wardNames.size()) return;
        String name = wardNames.get(idx);
        net.minecraft.nbt.CompoundTag tag = wardOptions.get(idx);
        ensureDefaultOptionsInto(name, tag);
    }

    private void ensureDefaultOptionsInto(String name, net.minecraft.nbt.CompoundTag tag) {
        me.cleardragonf.com.ward.WardSpec spec = me.cleardragonf.com.ward.WardSpecs.get(name);
        if (spec != null) {
            net.minecraft.nbt.CompoundTag defaults = spec.defaultOptionsTag();
            for (String k : defaults.getAllKeys()) {
                if (!tag.contains(k)) tag.put(k, defaults.get(k).copy());
            }
        } else {
            // Fallback default
            if (!tag.contains("this_is_a_test")) tag.putBoolean("this_is_a_test", false);
        }
    }

    // Memorization placeholder API
    public void memorizeBarrierPosition(BlockPos pos) {
        if (pos != null) memorizedBarrier.add(pos.immutable());
    }

    public Set<BlockPos> getMemorizedBarrier() {
        return Collections.unmodifiableSet(memorizedBarrier);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        // Save ward stones
        ListTag ws = new ListTag();
        for (BlockPos p : wardStones) {
            CompoundTag e = new CompoundTag();
            e.putInt("x", p.getX()); e.putInt("y", p.getY()); e.putInt("z", p.getZ());
            ws.add(e);
        }
        tag.put("ward_stones", ws);

        // Save ward list (name + active + options)
        ListTag wl = new ListTag();
        for (int i = 0; i < wardNames.size(); i++) {
            String s = wardNames.get(i);
            CompoundTag e = new CompoundTag();
            e.putString("n", s);
            boolean a = (i < wardActive.size()) && Boolean.TRUE.equals(wardActive.get(i));
            e.putBoolean("a", a);
            if (i < wardOptions.size()) e.put("opt", wardOptions.get(i));
            wl.add(e);
        }
        tag.put("ward_list", wl);

        // Save memorized barrier
        ListTag mb = new ListTag();
        for (BlockPos p : memorizedBarrier) {
            CompoundTag e = new CompoundTag();
            e.putInt("x", p.getX()); e.putInt("y", p.getY()); e.putInt("z", p.getZ());
            mb.add(e);
        }
        tag.put("memorized_barrier", mb);
    }

    public void load(CompoundTag tag) {
        wardStones.clear();
        wardNames.clear();
        wardActive.clear();
        wardOptions.clear();
        memorizedBarrier.clear();

        if (tag.contains("ward_stones", Tag.TAG_LIST)) {
            ListTag ws = tag.getList("ward_stones", Tag.TAG_COMPOUND);
            for (Tag t : ws) {
                if (t instanceof CompoundTag e) {
                    wardStones.add(new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z")));
                }
            }
        }
        if (tag.contains("ward_list", Tag.TAG_LIST)) {
            ListTag wl = tag.getList("ward_list", Tag.TAG_COMPOUND);
            for (Tag t : wl) {
                if (t instanceof CompoundTag e) {
                    String n = e.getString("n");
                    boolean a = e.getBoolean("a");
                    wardNames.add(n);
                    wardActive.add(a);
                    if (e.contains("opt", Tag.TAG_COMPOUND)) {
                        wardOptions.add(e.getCompound("opt").copy());
                    } else {
                        wardOptions.add(new net.minecraft.nbt.CompoundTag());
                    }
                }
            }
        } else if (tag.contains("ward_names", Tag.TAG_LIST)) {
            // Backward compat: names without active flags -> default inactive
            ListTag wn = tag.getList("ward_names", Tag.TAG_COMPOUND);
            for (Tag t : wn) {
                if (t instanceof CompoundTag e) {
                    String n = e.getString("n");
                    wardNames.add(n);
                    wardActive.add(Boolean.FALSE);
                    wardOptions.add(new net.minecraft.nbt.CompoundTag());
                }
            }
        }
        if (tag.contains("memorized_barrier", Tag.TAG_LIST)) {
            ListTag mb = tag.getList("memorized_barrier", Tag.TAG_COMPOUND);
            for (Tag t : mb) {
                if (t instanceof CompoundTag e) {
                    memorizedBarrier.add(new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z")));
                }
            }
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        load(tag);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, null);
        return tag;
    }

    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    // Some mappings require manual client load for BE updates
    public void onDataPacket(net.minecraft.network.Connection net,
                             net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }

    public void handleUpdateTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        load(tag);
    }

    private void syncToClients() {
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            BlockState s = getBlockState();
            sl.sendBlockUpdated(worldPosition, s, s, 3);
            sl.getChunkSource().blockChanged(worldPosition);
        }
    }
}
