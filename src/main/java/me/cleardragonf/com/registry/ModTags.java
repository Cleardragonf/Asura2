package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;

public class ModTags {
    public static class Items {
        // Granular tags (-4..+4). Multiple can apply; largest magnitude wins per item.
        public static final TagKey<Item> INSULATION_M4 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_m4");
        public static final TagKey<Item> INSULATION_M3 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_m3");
        public static final TagKey<Item> INSULATION_M2 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_m2");
        public static final TagKey<Item> INSULATION_M1 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_m1");
        public static final TagKey<Item> INSULATION_P1 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_p1");
        public static final TagKey<Item> INSULATION_P2 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_p2");
        public static final TagKey<Item> INSULATION_P3 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_p3");
        public static final TagKey<Item> INSULATION_P4 = TagKey.create(Registries.ITEM, Asura.MODID, "insulation_p4");
    }
}
