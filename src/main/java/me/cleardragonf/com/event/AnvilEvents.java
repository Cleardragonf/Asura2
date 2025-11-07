package me.cleardragonf.com.event;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static me.cleardragonf.com.Asura.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class AnvilEvents {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.isEmpty() || right.isEmpty()) {
            return;
        }

        // Gather enchantments from both items/books (1.21+: use DataComponents / ItemEnchantments)
        Map<Holder<Enchantment>, Integer> leftEnchants = getEnchantments(left);
        Map<Holder<Enchantment>, Integer> rightEnchants = getEnchantments(right);

        if (leftEnchants.isEmpty() && rightEnchants.isEmpty()) {
            return; // Let vanilla handle non-enchanting use-cases
        }

        // Start with a copy of the left item
        ItemStack output = left.copy();

        // Merge enchantments: vanilla-like rule (V + V -> VI, else max)
        Map<Holder<Enchantment>, Integer> merged = new HashMap<>(leftEnchants);
        for (Map.Entry<Holder<Enchantment>, Integer> e : rightEnchants.entrySet()) {
            Holder<Enchantment> ench = e.getKey();
            int rightLevel = Math.max(0, e.getValue());
            int leftLevel = merged.getOrDefault(ench, 0);

            int newLevel = leftLevel == rightLevel && leftLevel > 0
                    ? leftLevel + 1
                    : Math.max(leftLevel, rightLevel);
            merged.put(ench, newLevel);
        }

        // Apply merged enchantments forcibly (ignore vanilla caps/compatibility)
        // 1.21+: Build ItemEnchantments via Mutable and write to DataComponents
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<Holder<Enchantment>, Integer> e : merged.entrySet()) {
            int lvl = Math.max(0, e.getValue());
            if (lvl > 0) {
                mutable.set(e.getKey(), lvl);
            }
        }
        output.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

        // Apply rename if set in the anvil UI
        if (event.getName() != null && !event.getName().isBlank()) {
            // 1.21+: set custom name via DataComponents
            output.set(DataComponents.CUSTOM_NAME, Component.literal(event.getName()));
        }

        // Ensure the operation is always permitted and affordable
        event.setCost(1);            // minimal level cost to avoid "Too Expensive!"
        event.setMaterialCost(1);    // consume one from the right slot (book/item)
        event.setOutput(output);
    }

    private static Map<Holder<Enchantment>, Integer> getEnchantments(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> map = new HashMap<>();

        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var e : direct.entrySet()) {
            map.merge((Holder<Enchantment>) e.getKey(), (Integer) e.getValue(), Math::max);
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var e : stored.entrySet()) {
            map.merge((Holder<Enchantment>) e.getKey(), (Integer) e.getValue(), Math::max);
        }

        return map;
    }
}
