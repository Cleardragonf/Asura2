package me.cleardragonf.com;

import com.mojang.logging.LogUtils;
import me.cleardragonf.com.data.Config;
import me.cleardragonf.com.registry.*;
import me.cleardragonf.com.screen.ManaGeneratorScreen;
import me.cleardragonf.com.client.render.ManaBatteryRenderer;
import me.cleardragonf.com.screen.ManaBatteryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Asura.MODID)
public class Asura {

    public static final String MODID = "asura";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK =
            BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM =
            ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXAMPLE_ITEM =
            ITEMS.register("example_item", () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build())));

    public static final RegistryObject<CreativeModeTab> ASURA_TAB =
            CREATIVE_MODE_TABS.register("asura_tab", () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.asura.asura_tab"))
                    .icon(() -> me.cleardragonf.com.registry.ModItems.MANA_SHARD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(me.cleardragonf.com.registry.ModItems.MANA_SHARD.get());
                        output.accept(me.cleardragonf.com.registry.ModBlocks.MANA_ORE_ITEM.get());
                        output.accept(me.cleardragonf.com.registry.ModBlocks.DEEPSLATE_MANA_ORE_ITEM.get());
                        output.accept(me.cleardragonf.com.registry.ModBlocks.MANA_GENERATOR_ITEM.get());
                        output.accept(me.cleardragonf.com.registry.ModBlocks.MANA_BATTERY_ITEM.get());
                        output.accept(me.cleardragonf.com.registry.ModBlocks.MANA_RELAY_ITEM.get());
                        output.accept(me.cleardragonf.com.registry.ModItems.LINKING_WAND.get());
                    })
                    .build());

    public Asura() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Keep example block in Building Blocks for now
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    // ✅ Proper Client-only setup
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.MANA_GENERATOR_MENU.get(), ManaGeneratorScreen::new);
                MenuScreens.register(ModMenus.MANA_BATTERY_MENU.get(), ManaBatteryScreen::new);
                net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                        ModBlockEntities.MANA_BATTERY_ENTITY.get(), ManaBatteryRenderer::new);
            });

            LOGGER.info("Mana Generator/Battery Screens registered successfully");
            LOGGER.info("Minecraft user: {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
