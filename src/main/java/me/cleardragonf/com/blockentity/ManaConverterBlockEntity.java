package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.api.ManaReceiver;
import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ManaConverterBlockEntity extends BlockEntity implements ManaReceiver, MenuProvider {

    private static final Map<Item, Integer> BASE_COSTS = new HashMap<>();
    private static final Map<Item, Integer> DERIVED_COST_CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Set<Item>> COMPUTE_STACK = ThreadLocal.withInitial(HashSet::new);
    static {
        // Baseline costs for common/base items, roughly by abundance/rarity
        // Ores/ingots/gems
        BASE_COSTS.put(Items.COAL, 120);
        BASE_COSTS.put(Items.REDSTONE, 80);
        BASE_COSTS.put(Items.LAPIS_LAZULI, 150);
        BASE_COSTS.put(Items.COPPER_INGOT, 150);
        BASE_COSTS.put(Items.IRON_INGOT, 250);
        BASE_COSTS.put(Items.GOLD_INGOT, 500);
        BASE_COSTS.put(Items.QUARTZ, 300);
        BASE_COSTS.put(Items.EMERALD, 4000);
        BASE_COSTS.put(Items.DIAMOND, 5000);
        BASE_COSTS.put(Items.NETHERITE_INGOT, 20000);

        // Stone/earth
        BASE_COSTS.put(Items.STONE, 8);
        BASE_COSTS.put(Items.COBBLESTONE, 6);
        BASE_COSTS.put(Items.DEEPSLATE, 10);
        BASE_COSTS.put(Items.ANDESITE, 8);
        BASE_COSTS.put(Items.DIORITE, 8);
        BASE_COSTS.put(Items.GRANITE, 8);
        BASE_COSTS.put(Items.DIRT, 2);
        BASE_COSTS.put(Items.GRASS_BLOCK, 4);
        BASE_COSTS.put(Items.SAND, 4);
        BASE_COSTS.put(Items.RED_SAND, 5);
        BASE_COSTS.put(Items.GRAVEL, 4);
        BASE_COSTS.put(Items.CLAY_BALL, 6);
        BASE_COSTS.put(Items.SNOWBALL, 3);
        BASE_COSTS.put(Items.ICE, 12);
        BASE_COSTS.put(Items.NETHERRACK, 3);
        BASE_COSTS.put(Items.BLACKSTONE, 12);
        BASE_COSTS.put(Items.BASALT, 10);
        BASE_COSTS.put(Items.END_STONE, 20);

        // Logs (any overworld wood types roughly similar)
        BASE_COSTS.put(Items.OAK_LOG, 12);
        BASE_COSTS.put(Items.SPRUCE_LOG, 12);
        BASE_COSTS.put(Items.BIRCH_LOG, 12);
        BASE_COSTS.put(Items.JUNGLE_LOG, 12);
        BASE_COSTS.put(Items.ACACIA_LOG, 12);
        BASE_COSTS.put(Items.DARK_OAK_LOG, 12);
        BASE_COSTS.put(Items.MANGROVE_LOG, 12);
        BASE_COSTS.put(Items.CHERRY_LOG, 14);
        // Nether woods
        BASE_COSTS.put(Items.CRIMSON_STEM, 16);
        BASE_COSTS.put(Items.WARPED_STEM, 16);

        // Simple plants/foods
        BASE_COSTS.put(Items.SUGAR_CANE, 6);
        BASE_COSTS.put(Items.CACTUS, 6);
        BASE_COSTS.put(Items.WHEAT_SEEDS, 2);
        BASE_COSTS.put(Items.WHEAT, 10);
        BASE_COSTS.put(Items.BEETROOT, 10);
        BASE_COSTS.put(Items.CARROT, 12);
        BASE_COSTS.put(Items.POTATO, 12);
        BASE_COSTS.put(Items.SWEET_BERRIES, 8);
        BASE_COSTS.put(Items.GLOW_BERRIES, 14);

        // Fish
        BASE_COSTS.put(Items.COD, 24);
        BASE_COSTS.put(Items.SALMON, 28);
        BASE_COSTS.put(Items.TROPICAL_FISH, 40);
        BASE_COSTS.put(Items.PUFFERFISH, 50);

        // Buckets (as base for fluids)
        BASE_COSTS.put(Items.WATER_BUCKET, 150);
        BASE_COSTS.put(Items.LAVA_BUCKET, 500);
        BASE_COSTS.put(Items.MILK_BUCKET, 120);

        // Mobs drops simple
        BASE_COSTS.put(Items.STRING, 18);
        BASE_COSTS.put(Items.FEATHER, 10);
        BASE_COSTS.put(Items.LEATHER, 20);
        BASE_COSTS.put(Items.BONE, 16);
        BASE_COSTS.put(Items.ROTTEN_FLESH, 8);
        BASE_COSTS.put(Items.SPIDER_EYE, 20);
        BASE_COSTS.put(Items.GUNPOWDER, 30);
        BASE_COSTS.put(Items.SLIME_BALL, 60);
        BASE_COSTS.put(Items.ENDER_PEARL, 600);
    }

    private final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public int getMaxStackSize() { return 64; }
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (slot == 0) return ManaConverterBlockEntity.this.resolveCost(stack.getItem()) > 0; // template only for known items
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
        int currentCost = resolveCost(input.getItem());
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

    // Public helper for menus/UI
    public int getCostFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return resolveCost(stack.getItem());
    }

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
    public int resolveCost(Item item) {
        if (item == null) return 0;
        // 1) Direct base cost
        Integer base = BASE_COSTS.get(item);
        if (base != null) return base;

        // 2) Cached derived
        Integer cached = DERIVED_COST_CACHE.get(item);
        if (cached != null) return cached;

        // 3) Known block compressions (fallback if no recipes cover it)
        if (item == Items.DIAMOND_BLOCK) return cache(item, scaleBlock(Items.DIAMOND));
        if (item == Items.EMERALD_BLOCK) return cache(item, scaleBlock(Items.EMERALD));
        if (item == Items.GOLD_BLOCK) return cache(item, scaleBlock(Items.GOLD_INGOT));
        if (item == Items.IRON_BLOCK) return cache(item, scaleBlock(Items.IRON_INGOT));
        if (item == Items.COPPER_BLOCK) return cache(item, scaleBlock(Items.COPPER_INGOT));
        if (item == Items.REDSTONE_BLOCK) return cache(item, scaleBlock(Items.REDSTONE));
        if (item == Items.LAPIS_BLOCK) return cache(item, scaleBlock(Items.LAPIS_LAZULI));
        if (item == Items.COAL_BLOCK) return cache(item, scaleBlock(Items.COAL));
        if (item == Items.QUARTZ_BLOCK) return cache(item, scaleBlock(Items.QUARTZ));
        if (item == Items.NETHERITE_BLOCK) return cache(item, scaleBlock(Items.NETHERITE_INGOT));

        // 4) Derive via recipes (choose cheapest among all applicable recipes)
        int viaRecipe = deriveFromRecipes(item);
        return cache(item, viaRecipe);
    }

    private int cache(Item item, int value) {
        if (value <= 0) return 0;
        DERIVED_COST_CACHE.put(item, value);
        return value;
    }

    private static int scaleBlock(Item baseItem) {
        int base = BASE_COSTS.getOrDefault(baseItem, 0);
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

    private int deriveFromRecipes(Item target) {
        if (level == null || level.isClientSide) return 0;
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return 0;

        // Cycle guard
        Set<Item> stack = COMPUTE_STACK.get();
        if (stack.contains(target)) return 0;
        stack.add(target);
        try {
            var access = serverLevel.registryAccess();
            var manager = serverLevel.getRecipeManager();
            int best = Integer.MAX_VALUE;

            // Iterate all recipes and filter by result item
            for (var holder : manager.getRecipes()) {
                var recipe = holder.value();
                ItemStack result = recipe.getResultItem(access);
                if (result.isEmpty() || result.getItem() != target) continue;

                int outCount = Math.max(1, result.getCount());
                int cost = costForIngredients(recipe);
                cost += processingOverhead(recipe, result);
                if (cost <= 0) continue;
                int perUnit = (int) Math.ceil(cost / (double) outCount);
                if (perUnit > 0 && perUnit < best) best = perUnit;
            }

            return best == Integer.MAX_VALUE ? 0 : best;
        } finally {
            stack.remove(target);
        }
    }

    private int costForIngredients(net.minecraft.world.item.crafting.Recipe<?> recipe) {
        // Generic path: sum minimal cost among item options for each ingredient slot
        int total = 0;
        var ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) return 0;

        for (var ing : ingredients) {
            if (ing.isEmpty()) continue; // shaped recipes include empties
            int best = Integer.MAX_VALUE;
            for (ItemStack option : ing.getItems()) {
                if (option.isEmpty()) continue;
                int c = resolveCost(option.getItem());
                if (c > 0 && c < best) best = c;
            }
            if (best == Integer.MAX_VALUE) return 0; // unknown ingredient => cannot price
            total += best;
        }

        // Special handling for smithing-like recipes (base + addition) already covered via ingredients
        // Cooking/stonecutting are also handled since they expose a single ingredient.
        return total;
    }

    private int processingOverhead(net.minecraft.world.item.crafting.Recipe<?> recipe, ItemStack result) {
        if (recipe == null) return 0;
        var type = recipe.getType();
        int outCount = Math.max(1, result == null ? 1 : result.getCount());

        // Baseline fuel reference: assume coal at 1600 ticks
        int coalCost = BASE_COSTS.getOrDefault(net.minecraft.world.item.Items.COAL, 120);
        double perTickFuelCost = coalCost / 1600.0;

        // Cooking: add fuel proportional to cooking time
        try {
            if (recipe instanceof net.minecraft.world.item.crafting.AbstractCookingRecipe cooking) {
                int t = Math.max(1, cooking.getCookingTime());
                int fuel = (int) Math.ceil(perTickFuelCost * t);
                // minimal handling cost so very cheap inputs still get some cost
                int handling = Math.max(0, (int)Math.ceil(0.02 * BASE_COSTS.getOrDefault(result.getItem(), 0)));
                return Math.max(1, fuel + handling);
            }
        } catch (Throwable ignored) {}

        // Stonecutting: small flat per-operation overhead
        try {
            if (type == net.minecraft.world.item.crafting.RecipeType.STONECUTTING) {
                int overhead = 10; // light processing cost
                return overhead;
            }
        } catch (Throwable ignored) {}

        // Smithing: higher overhead to reflect tool/template usage
        try {
            if (type == net.minecraft.world.item.crafting.RecipeType.SMITHING) {
                int overhead = 100; // significant processing/consumable wear
                return overhead;
            }
        } catch (Throwable ignored) {}

        return 0;
    }
}
