package me.cleardragonf.com.datagen;

import me.cleardragonf.com.registry.ModBlocks;
import me.cleardragonf.com.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Mana Generator crafted from Mana Shards (placeholder recipe)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MANA_GENERATOR.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.MANA_SHARD.get())
                .unlockedBy(getHasName(ModItems.MANA_SHARD.get()), has(ModItems.MANA_SHARD.get()))
                .save(output);
    }
}
