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
        blockWithItem(ModBlocks.MANA_GENERATOR);

        // Mana Battery uses a vanilla iron block texture as placeholder
        blockWithVanillaTexture(ModBlocks.MANA_BATTERY, "iron_block");

        // Mana Relay: example using a single sprite-sheet texture with per-face UVs
        // and a smaller cube element to appear "floating" within the block space.
        relayFloatingFromSpriteSheet(ModBlocks.MANA_RELAY,
                modLoc("block/mana_relay"),
                96, 16 // assumed texture size (6 faces x 16px wide, 1 row of 16px height)
        );

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

    private void relayFloatingFromSpriteSheet(RegistryObject<Block> blockRegistryObject,
                                              net.minecraft.resources.ResourceLocation texture,
                                              int texW, int texH) {
        String name = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();

        var builder = models().getBuilder(name)
                .texture("tex", texture)
                .texture("particle", texture)
                .renderType("cutout");

        // Create a smaller cube centered in the block to look like it's floating.
        // Coordinates are in 0..16 block space.
        var elem = builder.element()
                .from(4.0f, 6.0f, 4.0f)
                .to(12.0f, 14.0f, 12.0f);

        // Assume a 96x16 sprite sheet laid out as:
        // [0-16): top, [16-32): bottom, [32-48): north, [48-64): south, [64-80): west, [80-96): east
        // Adjust these UVs if your layout differs.
        elem.face(Direction.UP)   .uvs( 0, 0, 16, 16).texture("#tex").end();
        elem.face(Direction.DOWN) .uvs(16, 0, 32, 16).texture("#tex").end();
        elem.face(Direction.NORTH).uvs(32, 0, 48, 16).texture("#tex").end();
        elem.face(Direction.SOUTH).uvs(48, 0, 64, 16).texture("#tex").end();
        elem.face(Direction.WEST) .uvs(64, 0, 80, 16).texture("#tex").end();
        elem.face(Direction.EAST) .uvs(80, 0, 96, 16).texture("#tex").end();
        elem.end();

        ModelFile modelFile = builder;
        simpleBlockWithItem(blockRegistryObject.get(), modelFile);
    }
}
