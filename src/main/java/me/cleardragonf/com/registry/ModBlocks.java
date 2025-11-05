package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.block.ManaBatteryBlock;
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

    public static final RegistryObject<Block> MANA_BATTERY = BLOCKS.register(
            "mana_battery",
            () -> new ManaBatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .pushReaction(PushReaction.NORMAL))
    );

    public static final RegistryObject<Item> MANA_BATTERY_ITEM = ModItems.ITEMS.register(
            "mana_battery",
            () -> new BlockItem(MANA_BATTERY.get(), new Item.Properties())
    );

    // Mana Ore (overworld)
    public static final RegistryObject<Block> MANA_ORE = BLOCKS.register(
            "mana_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.STONE))
    );

    public static final RegistryObject<Item> MANA_ORE_ITEM = ModItems.ITEMS.register(
            "mana_ore",
            () -> new BlockItem(MANA_ORE.get(), new Item.Properties())
    );

    // Deepslate Mana Ore
    public static final RegistryObject<Block> DEEPSLATE_MANA_ORE = BLOCKS.register(
            "deepslate_mana_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5f, 3.0f)
                    .sound(SoundType.DEEPSLATE))
    );

    public static final RegistryObject<Item> DEEPSLATE_MANA_ORE_ITEM = ModItems.ITEMS.register(
            "deepslate_mana_ore",
            () -> new BlockItem(DEEPSLATE_MANA_ORE.get(), new Item.Properties())
    );

}
