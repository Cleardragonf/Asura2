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
    }
}
