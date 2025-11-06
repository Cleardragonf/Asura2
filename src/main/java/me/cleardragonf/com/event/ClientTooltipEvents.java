package me.cleardragonf.com.event;

import me.cleardragonf.com.util.RomanNumerals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static me.cleardragonf.com.Asura.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientTooltipEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (enchants.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        // Build a set of base names for fast matching
        Map<String, Integer> levelsByBaseName = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            String base = Component.translatable(e.getKey().getDescriptionId()).getString();
            levelsByBaseName.put(base, e.getValue());
        }

        // Remove existing enchantment lines (basic heuristic: lines starting with the translated base name)
        // This avoids duplicate lines and replaces any decimal formatting.
        tooltip.removeIf(c -> {
            String s = c.getString();
            for (String base : levelsByBaseName.keySet()) {
                if (s.startsWith(base)) return true;
            }
            return false;
        });

        // Append our Roman-formatted lines (sorted for stability)
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(levelsByBaseName.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<String, Integer> e : entries) {
            int lvl = Math.max(1, e.getValue());
            String roman = RomanNumerals.toRoman(lvl);
            tooltip.add(Component.literal(e.getKey() + " " + roman).withStyle(ChatFormatting.GRAY));
        }
    }
}

