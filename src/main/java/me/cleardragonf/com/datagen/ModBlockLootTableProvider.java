package me.cleardragonf.com.datagen;

import me.cleardragonf.com.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), pRegistries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.MANA_GENERATOR.get());
        add(ModBlocks.MANA_ORE.get(), createOreDrop(ModBlocks.MANA_ORE.get(), me.cleardragonf.com.registry.ModItems.MANA_SHARD.get()));
        add(ModBlocks.DEEPSLATE_MANA_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_MANA_ORE.get(), me.cleardragonf.com.registry.ModItems.MANA_SHARD.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
