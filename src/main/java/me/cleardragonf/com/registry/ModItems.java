package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Asura.MODID);

    public static final net.minecraftforge.registries.RegistryObject<Item> MANA_SHARD =
            ITEMS.register("mana_shard", () -> new Item(new Item.Properties()));

    public static final net.minecraftforge.registries.RegistryObject<Item> LINKING_WAND =
            ITEMS.register("linking_wand", () -> new me.cleardragonf.com.item.LinkingWandItem(new Item.Properties().stacksTo(1)));
}
