package me.cleardragonf.com.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

        // Gather enchantments from both items/books
        Map<Enchantment, Integer> leftEnchants = EnchantmentHelper.getEnchantments(left);
        Map<Enchantment, Integer> rightEnchants = EnchantmentHelper.getEnchantments(right);

        if (leftEnchants.isEmpty() && rightEnchants.isEmpty()) {
            return; // Let vanilla handle non-enchanting use-cases
        }

        // Start with a copy of the left item
        ItemStack output = left.copy();

        // Merge enchantments with unlimited levels and no compatibility limits
        Map<Enchantment, Integer> merged = new HashMap<>(leftEnchants);
        for (Map.Entry<Enchantment, Integer> e : rightEnchants.entrySet()) {
            Enchantment ench = e.getKey();
            int rightLevel = Math.max(0, e.getValue());
            int leftLevel = merged.getOrDefault(ench, 0);

            // Additive growth so strength obviously increases when combined
            int newLevel = leftLevel + rightLevel;
            if (newLevel <= 0) newLevel = Math.max(leftLevel, rightLevel);
            merged.put(ench, newLevel);
        }

        // Also keep any enchantments that exist only on the right
        for (Map.Entry<Enchantment, Integer> e : rightEnchants.entrySet()) {
            merged.putIfAbsent(e.getKey(), Math.max(0, e.getValue()));
        }

        // Apply merged enchantments forcibly (ignore vanilla caps/compatibility)
        EnchantmentHelper.setEnchantments(merged, output);

        // Apply rename if set in the anvil UI
        if (event.getName() != null && !event.getName().isBlank()) {
            output.setHoverName(net.minecraft.network.chat.Component.literal(event.getName()));
        }

        // Ensure the operation is always permitted and affordable
        event.setCost(1);            // minimal level cost to avoid "Too Expensive!"
        event.setMaterialCost(1);    // consume one from the right slot (book/item)
        event.setOutput(output);
    }
}
