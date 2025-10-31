package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.block.ManaGeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Asura.MODID);

    public static final RegistryObject<Block> MANA_GENERATOR = BLOCKS.register(
            "mana_generator",
            () -> new ManaGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .pushReaction(PushReaction.NORMAL))
    );

    public static final RegistryObject<Item> MANA_GENERATOR_ITEM = ModItems.ITEMS.register(
            "mana_generator",
            () -> new BlockItem(MANA_GENERATOR.get(), new Item.Properties())
    );

}
