package me.cleardragonf.com.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.cleardragonf.com.Asura;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModBiomeModifierJsonProvider implements DataProvider {
    private final PackOutput output;

    public ModBiomeModifierJsonProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "forge:add_features");
        root.addProperty("biomes", "#minecraft:is_overworld");

        JsonArray features = new JsonArray();
        features.add(Asura.MODID + ":mana_ore_placed");
        root.add("features", features);
        root.addProperty("step", "underground_ores");

        Path target = this.output.getOutputFolder()
                .resolve("data/" + Asura.MODID + "/forge/biome_modifier/add_mana_ore.json");

        return DataProvider.saveStable(cache, root, target);
    }

    @Override
    public String getName() {
        return "Asura Biome Modifiers";
    }
}

