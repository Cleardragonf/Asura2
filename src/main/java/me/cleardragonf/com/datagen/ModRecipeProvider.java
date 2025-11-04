package me.cleardragonf.com.datagen;

import me.cleardragonf.com.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput p_248933_, CompletableFuture<HolderLookup.Provider> p_333797_) {
        super(p_248933_, p_333797_);
    }

    @Override
    protected void buildRecipes(RecipeOutput p_297267_) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MANA_GENERATOR.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', Items.DIAMOND);
    }
}
