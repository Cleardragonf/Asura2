package me.cleardragonf.com.datagen;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Asura.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Use each block's own texture
        blockWithItem(ModBlocks.MANA_GENERATOR);
        blockWithModTexture(ModBlocks.MANA_BATTERY);

        // In-world model is empty (rendered by BER). Keep particle for break effects.
        emptyBlockModelWithParticle(ModBlocks.MANA_RELAY, modLoc("block/mana_relay"));

        // Ores: use mod textures
        blockWithModTexture(ModBlocks.MANA_ORE);
        blockWithModTexture(ModBlocks.DEEPSLATE_MANA_ORE);

        // Mana Converter: use generator texture until a dedicated one is provided
        blockWithExplicitTexture(ModBlocks.MANA_CONVERTER, modLoc("block/mana_generator"));

        // Ward system blocks: use generator texture as placeholder
        blockWithExplicitTexture(ModBlocks.MASTER_WARD_STONE, modLoc("block/mana_generator"));
        blockWithExplicitTexture(ModBlocks.WARD_STONE, modLoc("block/mana_generator"));
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void oreWithVanillaTexture(RegistryObject<Block> blockRegistryObject, String vanillaTextureName) {
        String path = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        var model = models().cubeAll(path, mcLoc("block/" + vanillaTextureName));
        simpleBlockWithItem(blockRegistryObject.get(), model);
    }

    private void blockWithVanillaTexture(RegistryObject<Block> blockRegistryObject, String vanillaTextureName) {
        String path = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        var model = models().cubeAll(path, mcLoc("block/" + vanillaTextureName));
        simpleBlockWithItem(blockRegistryObject.get(), model);
    }

    private void blockWithModTexture(RegistryObject<Block> blockRegistryObject) {
        String path = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        var model = models().cubeAll(path, modLoc("block/" + path));
        simpleBlockWithItem(blockRegistryObject.get(), model);
    }

    private void emptyBlockModelWithParticle(RegistryObject<Block> blockRegistryObject, net.minecraft.resources.ResourceLocation particleTex) {
        String name = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        ModelFile empty = models().getBuilder(name)
                .texture("particle", particleTex);
        simpleBlock(blockRegistryObject.get(), empty);
    }

    private void blockWithExplicitTexture(RegistryObject<Block> blockRegistryObject, net.minecraft.resources.ResourceLocation texture) {
        String path = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        var model = models().cubeAll(path, texture);
        simpleBlockWithItem(blockRegistryObject.get(), model);
    }
}
