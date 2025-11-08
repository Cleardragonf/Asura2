package me.cleardragonf.com.datagen;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Asura.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.MANA_SHARD.get());
        // Linking wand uses vanilla stick texture for now
        withExistingParent(ModItems.LINKING_WAND.getId().getPath(), mcLoc("item/handheld"))
                .texture("layer0", mcLoc("item/stick"));

        // Block item models to use their block textures
        withExistingParent("mana_ore", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_ore"));
        withExistingParent("deepslate_mana_ore", mcLoc("item/generated")).texture("layer0", modLoc("block/deepslate_mana_ore"));
        withExistingParent("mana_generator", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_generator"));
        withExistingParent("mana_battery", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_battery"));
        withExistingParent("mana_relay", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_relay"));
        withExistingParent("mana_converter", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_generator"));

        // Ward system items
        withExistingParent("master_ward_stone", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_generator"));
        withExistingParent("ward_stone", mcLoc("item/generated")).texture("layer0", modLoc("block/mana_generator"));
        withExistingParent(ModItems.WARD_SPELL_ITEM.getId().getPath(), mcLoc("item/generated")).texture("layer0", mcLoc("item/paper"));
        withExistingParent(ModItems.HEAL_WARD.getId().getPath(), mcLoc("item/generated")).texture("layer0", mcLoc("item/paper"));
    }
}
