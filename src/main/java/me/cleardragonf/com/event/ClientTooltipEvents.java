package me.cleardragonf.com.event;

import me.cleardragonf.com.util.RomanNumerals;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.registries.ForgeRegistries;
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

        ItemEnchantments itemEnchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (itemEnchants.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        // Build a set of base names for fast matching
        Map<String, Integer> levelsByBaseName = new HashMap<>();
        for (var e : itemEnchants.entrySet()) {
            @SuppressWarnings("unchecked")
            net.minecraft.core.Holder<Enchantment> holder = (net.minecraft.core.Holder<Enchantment>) e.getKey();
            holder.unwrapKey().ifPresent(key -> {
                var rl = key.location();
                String base = Component.translatable("enchantment." + rl.getNamespace() + "." + rl.getPath()).getString();
                levelsByBaseName.put(base, (Integer) e.getValue());
            });
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

