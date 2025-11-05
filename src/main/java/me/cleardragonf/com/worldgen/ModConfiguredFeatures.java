package me.cleardragonf.com.worldgen;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MANA_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "mana_ore"));

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        BlockState ore = ModBlocks.MANA_ORE.get().defaultBlockState();
        BlockState deepslateOre = ModBlocks.DEEPSLATE_MANA_ORE.get().defaultBlockState();
        var targets = List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ore),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), deepslateOre)
        );
        ctx.register(MANA_ORE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, 9)));
    }
}
