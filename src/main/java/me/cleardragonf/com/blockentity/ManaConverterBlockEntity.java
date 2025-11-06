package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.api.ManaReceiver;
import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.SimpleContainer;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class ManaConverterBlockEntity extends BlockEntity implements ManaReceiver, MenuProvider {

    private static final Map<Item, Integer> COSTS = new HashMap<>();
    static {
        // Example: diamond costs 5000 mana per duplicate
        net.minecraft.world.item.Items items = null; // only to help IDE; we use Items.DIAMOND directly
        COSTS.put(net.minecraft.world.item.Items.DIAMOND, 5000);
        COSTS.put(net.minecraft.world.item.Items.EMERALD, 4000);
        COSTS.put(net.minecraft.world.item.Items.IRON_INGOT, 250);
        COSTS.put(net.minecraft.world.item.Items.GOLD_INGOT, 500);
    }

    private final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public int getMaxStackSize() { return 64; }
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (slot == 0) return getCostFor(stack) > 0; // template only for known items
            return false; // output not player-insertable
        }
        @Override
        public void setChanged() {
            super.setChanged();
            // Mirror slots to fields and mark dirty so NBT saves
            input = getItem(0).copy();
            output = getItem(1).copy();
            ManaConverterBlockEntity.this.setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private ItemStack input = ItemStack.EMPTY;  // mirror of slot 0
    private ItemStack output = ItemStack.EMPTY; // mirror of slot 1
    private int buffer = 0;
    private final ContainerData data = new SimpleContainerData(2); // [0]=buffer, [1]=cost

    public ManaConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CONVERTER_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        // Mirror inventory to fields
        input = inventory.getItem(0);
        output = inventory.getItem(1);

        data.set(0, buffer);
        int currentCost = getCostFor(input);
        data.set(1, currentCost);

        if (input.isEmpty() || currentCost <= 0) return;
        Integer cost = currentCost;
        if (cost == null || cost <= 0) return;
        if (buffer < cost) return;

        // Ensure output is same item
        if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, input)) {
            return; // wait for player to take mismatched output
        }
        if (output.isEmpty()) {
            output = new ItemStack(input.getItem(), 0);
        }
        if (output.getCount() >= output.getMaxStackSize()) return;

        buffer -= cost;
        output.grow(1);
        inventory.setItem(1, output.copy());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public int receiveMana(int amount) {
        if (amount <= 0) return 0;
        int before = buffer;
        buffer = Math.min(Integer.MAX_VALUE / 2, buffer + amount);
        return buffer - before;
    }

    public ItemStack getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public void setInput(ItemStack stack) { this.input = stack.copy(); inventory.setItem(0, this.input.copy()); setChanged(); }

    public ContainerData getContainerData() { return data; }
    public SimpleContainer getInventory() { return inventory; }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        var access = provider != null ? provider : (level != null ? level.registryAccess() : null);
        if (access != null) {
            // Save both slots so output is always persisted reliably
            net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack st = inventory.getItem(i);
                if (!st.isEmpty()) {
                    net.minecraft.nbt.Tag t = st.save(access);
                    if (t instanceof net.minecraft.nbt.CompoundTag e) {
                        e.putByte("slot", (byte)i);
                        items.add(e);
                    }
                }
            }
            tag.put("items", items);
        }
        tag.putInt("buffer", buffer);
    }

    // Legacy fallback (some loaders still call this)
    public void load(CompoundTag tag) {
        loadFromTag(tag, level != null ? level.registryAccess() : null);
    }

    // Preferred load with registry access (1.21+)
    public void load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        loadFromTag(tag, provider);
    }

    // Some mappings call this variant instead of load(...)
    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        loadFromTag(tag, provider);
    }

    private void loadFromTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider access) {
        // Prefer list-based save to restore both slots; fallback to legacy keys.
        input = ItemStack.EMPTY;
        output = ItemStack.EMPTY;
        if (access != null && tag.contains("items", net.minecraft.nbt.Tag.TAG_LIST)) {
            var list = tag.getList("items", net.minecraft.nbt.Tag.TAG_COMPOUND);
            // Clear inventory first
            inventory.setItem(0, ItemStack.EMPTY);
            inventory.setItem(1, ItemStack.EMPTY);
            for (var t : list) {
                if (t instanceof net.minecraft.nbt.CompoundTag e) {
                    int slot = e.getByte("slot") & 255;
                    ItemStack st = ItemStack.parseOptional(access, e);
                    if (!st.isEmpty() && slot >=0 && slot < inventory.getContainerSize()) {
                        inventory.setItem(slot, st);
                    }
                }
            }
            input = inventory.getItem(0).copy();
            output = inventory.getItem(1).copy();
        } else {
            if (access != null && tag.contains("input")) input = ItemStack.parseOptional(access, tag.getCompound("input"));
            if (access != null && tag.contains("output")) output = ItemStack.parseOptional(access, tag.getCompound("output"));
            inventory.setItem(0, input.copy());
            inventory.setItem(1, output.copy());
        }
        buffer = tag.getInt("buffer");
        setChanged();
    }

    // Networking for client sync
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        // For client sync, send both slots in a compact list
        var access = level != null ? level.registryAccess() : null;
        if (access != null) {
            net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack st = inventory.getItem(i);
                if (!st.isEmpty()) {
                    net.minecraft.nbt.Tag t = st.save(access);
                    if (t instanceof net.minecraft.nbt.CompoundTag e) {
                        e.putByte("slot", (byte)i);
                        items.add(e);
                    }
                }
            }
            tag.put("items", items);
        }
        tag.putInt("buffer", buffer);
        return tag;
    }

    // ---- MenuProvider ----
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new me.cleardragonf.com.menu.ManaConverterMenu(id, inv, this, data);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Mana Converter");
    }

    // ---- Costs ----
    public static int getCostFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return getCostFor(stack.getItem());
    }

    public static int getCostFor(Item item) {
        Integer base = COSTS.get(item);
        if (base != null) return base;
        // Derive from known 9x block forms
        var I = net.minecraft.world.item.Items.class;
        if (item == net.minecraft.world.item.Items.DIAMOND_BLOCK) return scaleBlock(net.minecraft.world.item.Items.DIAMOND);
        if (item == net.minecraft.world.item.Items.EMERALD_BLOCK) return scaleBlock(net.minecraft.world.item.Items.EMERALD);
        if (item == net.minecraft.world.item.Items.GOLD_BLOCK) return scaleBlock(net.minecraft.world.item.Items.GOLD_INGOT);
        if (item == net.minecraft.world.item.Items.IRON_BLOCK) return scaleBlock(net.minecraft.world.item.Items.IRON_INGOT);
        if (item == net.minecraft.world.item.Items.COPPER_BLOCK) return scaleBlock(net.minecraft.world.item.Items.COPPER_INGOT);
        if (item == net.minecraft.world.item.Items.REDSTONE_BLOCK) return scaleBlock(net.minecraft.world.item.Items.REDSTONE);
        if (item == net.minecraft.world.item.Items.LAPIS_BLOCK) return scaleBlock(net.minecraft.world.item.Items.LAPIS_LAZULI);
        if (item == net.minecraft.world.item.Items.COAL_BLOCK) return scaleBlock(net.minecraft.world.item.Items.COAL);
        if (item == net.minecraft.world.item.Items.QUARTZ_BLOCK) return scaleBlock(net.minecraft.world.item.Items.QUARTZ);
        if (item == net.minecraft.world.item.Items.NETHERITE_BLOCK) return scaleBlock(net.minecraft.world.item.Items.NETHERITE_INGOT);
        return 0;
    }

    private static int scaleBlock(Item baseItem) {
        int base = COSTS.getOrDefault(baseItem, 0);
        if (base <= 0) return 0;
        // 9x for composition with a rarity multiplier
        int rarityMul = rarityMultiplier(new ItemStack(baseItem).getRarity());
        // Add mild exponential bump for bulk crafting
        double exp = Math.pow(1.08, 9); // ~1.999 for 9 items
        return (int)Math.ceil(base * 9 * rarityMul / 100.0 * exp);
    }

    private static int rarityMultiplier(net.minecraft.world.item.Rarity r) {
        if (r == net.minecraft.world.item.Rarity.EPIC) return 200;
        if (r == net.minecraft.world.item.Rarity.RARE) return 150;
        if (r == net.minecraft.world.item.Rarity.UNCOMMON) return 120;
        return 100;
    }
}
