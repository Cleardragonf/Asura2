package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.api.ManaReceiver;
import me.cleardragonf.com.block.ManaBatteryBlock;
import me.cleardragonf.com.menu.ManaBatteryMenu;
import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class ManaBatteryBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider, ManaReceiver {

    public static final int CAPACITY_PER_BLOCK = 1000; // increased per request

    private BlockPos controllerPos; // null/itself when controller
    private boolean isController;
    private int storedMana; // valid on controller only
    private int clusterSize; // cached, controller only
    private final ContainerData data = new SimpleContainerData(2); // stored, capacity

    // Cluster bounds (inclusive). Stored on all members for client rendering.
    private int minX, minY, minZ, maxX, maxY, maxZ;

    public ManaBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_BATTERY_ENTITY.get(), pos, state);
        this.controllerPos = pos;
        this.isController = true;
        this.storedMana = 0;
        this.clusterSize = 1;
    }

    public void serverTick() {
        // no periodic logic needed yet; placeholder for future effects
        if (level != null && !level.isClientSide) {
            data.set(0, getStored());
            data.set(1, getCapacity());
        }
    }

    // ---- Multiblock cluster management ----
    public static void reformCluster(Level level, BlockPos origin) {
        if (level == null || level.isClientSide) return;

        Set<BlockPos> cluster = floodFillCluster(level, origin);
        if (cluster.isEmpty()) return;

        BlockPos controller = pickController(cluster);

        int totalStored = 0;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : cluster) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof ManaBatteryBlockEntity mb) {
                if (mb.isController) {
                    totalStored += mb.storedMana;
                }
            }
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        int size = cluster.size();
        int capacity = size * CAPACITY_PER_BLOCK;
        totalStored = Math.min(totalStored, capacity);

        for (BlockPos p : cluster) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof ManaBatteryBlockEntity mb) {
                mb.isController = p.equals(controller);
                mb.controllerPos = controller;
                mb.minX = minX; mb.minY = minY; mb.minZ = minZ;
                mb.maxX = maxX; mb.maxY = maxY; mb.maxZ = maxZ;
                if (mb.isController) {
                    mb.clusterSize = size;
                    mb.storedMana = totalStored;
                } else {
                    mb.clusterSize = 0;
                }
                mb.setChanged();
                if (mb.level != null) {
                    BlockState s = mb.getBlockState();
                    mb.level.sendBlockUpdated(mb.worldPosition, s, s, 3);
                }
            }
        }
    }

    public ContainerData getContainerData() {
        return data;
    }

    // ---- MenuProvider ----
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ManaBatteryMenu(id, inv, this, this.data);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Mana Battery");
    }

    public static void reformNeighbors(Level level, BlockPos origin) {
        if (level == null || level.isClientSide) return;
        for (Direction d : Direction.values()) {
            BlockPos adj = origin.relative(d);
            if (level.getBlockState(adj).getBlock() instanceof ManaBatteryBlock) {
                reformCluster(level, adj);
            }
        }
    }

    // Expose cluster for other systems (e.g., chained breaking)
    public static Set<BlockPos> getCluster(Level level, BlockPos start) {
        return floodFillCluster(level, start);
    }

    private static Set<BlockPos> floodFillCluster(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> dq = new ArrayDeque<>();
        if (!(level.getBlockState(start).getBlock() instanceof ManaBatteryBlock)) return visited;
        dq.add(start);
        visited.add(start);
        while (!dq.isEmpty()) {
            BlockPos p = dq.removeFirst();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!visited.contains(n) && level.getBlockState(n).getBlock() instanceof ManaBatteryBlock) {
                    visited.add(n);
                    dq.addLast(n);
                }
            }
        }
        return visited;
    }

    private static BlockPos pickController(Set<BlockPos> cluster) {
        BlockPos best = null;
        for (BlockPos p : cluster) {
            if (best == null) {
                best = p;
            } else if (comparePos(p, best) < 0) {
                best = p;
            }
        }
        return best;
    }

    private static int comparePos(BlockPos a, BlockPos b) {
        if (a.getY() != b.getY()) return Integer.compare(a.getY(), b.getY());
        if (a.getX() != b.getX()) return Integer.compare(a.getX(), b.getX());
        return Integer.compare(a.getZ(), b.getZ());
    }

    // ---- Storage API ----
    public int getCapacity() {
        if (isController) return clusterSize * CAPACITY_PER_BLOCK;
        ManaBatteryBlockEntity c = getControllerEntity();
        return c != null ? c.getCapacity() : CAPACITY_PER_BLOCK;
    }

    public int getStored() {
        if (isController) return storedMana;
        ManaBatteryBlockEntity c = getControllerEntity();
        return c != null ? c.getStored() : 0;
    }

    public int addMana(int amount) {
        if (amount <= 0) return 0;
        ManaBatteryBlockEntity c = isController ? this : getControllerEntity();
        if (c == null) return 0;
        int cap = c.getCapacity();
        int space = Math.max(0, cap - c.storedMana);
        int accepted = Math.min(space, amount);
        if (accepted > 0) {
            c.storedMana += accepted;
            c.setChanged();
        }
        return accepted;
    }

    @Override
    public int receiveMana(int amount) {
        return addMana(amount);
    }

    public int extractMana(int amount) {
        if (amount <= 0) return 0;
        ManaBatteryBlockEntity c = isController ? this : getControllerEntity();
        if (c == null) return 0;
        int taken = Math.min(c.storedMana, amount);
        if (taken > 0) {
            c.storedMana -= taken;
            c.setChanged();
        }
        return taken;
    }

    private ManaBatteryBlockEntity getControllerEntity() {
        if (level == null) return null;
        if (controllerPos == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof ManaBatteryBlockEntity mb) return mb;
        return null;
    }

    // ---- NBT Persistence ----
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        tag.putBoolean("Controller", isController);
        if (controllerPos != null) {
            tag.putInt("CtrlX", controllerPos.getX());
            tag.putInt("CtrlY", controllerPos.getY());
            tag.putInt("CtrlZ", controllerPos.getZ());
        }
        if (isController) {
            tag.putInt("Stored", storedMana);
            tag.putInt("Size", clusterSize);
        }
        tag.putInt("MinX", minX); tag.putInt("MinY", minY); tag.putInt("MinZ", minZ);
        tag.putInt("MaxX", maxX); tag.putInt("MaxY", maxY); tag.putInt("MaxZ", maxZ);
    }

    // Compatibility with mappings that call the older signature
    protected void saveAdditional(CompoundTag tag) {
        tag.putBoolean("Controller", isController);
        if (controllerPos != null) {
            tag.putInt("CtrlX", controllerPos.getX());
            tag.putInt("CtrlY", controllerPos.getY());
            tag.putInt("CtrlZ", controllerPos.getZ());
        }
        if (isController) {
            tag.putInt("Stored", storedMana);
            tag.putInt("Size", clusterSize);
        }
    }

    public void load(CompoundTag tag) {
        this.isController = tag.getBoolean("Controller");
        if (tag.contains("CtrlX")) {
            int x = tag.getInt("CtrlX");
            int y = tag.getInt("CtrlY");
            int z = tag.getInt("CtrlZ");
            this.controllerPos = new BlockPos(x, y, z);
        } else {
            this.controllerPos = this.worldPosition;
        }
        if (isController) {
            this.storedMana = tag.getInt("Stored");
            this.clusterSize = Math.max(1, tag.getInt("Size"));
        } else {
            this.storedMana = 0;
            this.clusterSize = 0;
        }
        this.minX = tag.getInt("MinX"); this.minY = tag.getInt("MinY"); this.minZ = tag.getInt("MinZ");
        this.maxX = tag.getInt("MaxX"); this.maxY = tag.getInt("MaxY"); this.maxZ = tag.getInt("MaxZ");
    }

    // Networking for client sync of bounds
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public boolean isController() { return isController; }

    // Compatibility with mappings that pass a Provider as well
    public void load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        load(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // Ensure the renderer is not culled when drawing the cluster as one unit
        if (maxX >= minX && maxY >= minY && maxZ >= minZ) {
            return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        }
        return super.getRenderBoundingBox();
    }
}
