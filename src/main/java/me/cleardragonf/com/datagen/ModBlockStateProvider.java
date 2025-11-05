package me.cleardragonf.com.datagen;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Asura.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.MANA_GENERATOR);

        // Mana Battery uses a vanilla iron block texture as placeholder
        blockWithVanillaTexture(ModBlocks.MANA_BATTERY, "iron_block");

        // Use vanilla textures for placeholder visuals until custom textures are provided
        oreWithVanillaTexture(ModBlocks.MANA_ORE, "stone");
        oreWithVanillaTexture(ModBlocks.DEEPSLATE_MANA_ORE, "deepslate");
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
}
