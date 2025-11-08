package me.cleardragonf.com.registry;

import me.cleardragonf.com.menu.ManaBatteryMenu;
import me.cleardragonf.com.menu.ManaConverterMenu;
import me.cleardragonf.com.menu.ManaGeneratorMenu;
import me.cleardragonf.com.menu.MagicCompendiumMenu;
import me.cleardragonf.com.menu.MasterWardStoneMenu;
import me.cleardragonf.com.Asura;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Asura.MODID);

    public static final RegistryObject<MenuType<ManaGeneratorMenu>> MANA_GENERATOR_MENU =
            MENUS.register("mana_generator_menu",
                    () -> IForgeMenuType.create((windowId, inv, player) ->
                            new ManaGeneratorMenu(windowId, inv, null, new SimpleContainerData(8))
                    ));

    public static final RegistryObject<MenuType<ManaBatteryMenu>> MANA_BATTERY_MENU =
            MENUS.register("mana_battery_menu",
                    () -> IForgeMenuType.create((windowId, inv, player) ->
                            new ManaBatteryMenu(windowId, inv, null, new SimpleContainerData(2))
                    ));

    public static final RegistryObject<MenuType<ManaConverterMenu>> MANA_CONVERTER_MENU =
            MENUS.register("mana_converter_menu",
                    () -> IForgeMenuType.create((windowId, inv, player) ->
                            new ManaConverterMenu(windowId, inv, null, new SimpleContainerData(2))
                    ));

    public static final RegistryObject<MenuType<MagicCompendiumMenu>> MAGIC_COMPENDIUM_MENU =
            MENUS.register("magic_compendium_menu",
                    () -> IForgeMenuType.create((windowId, inv, player) ->
                            new MagicCompendiumMenu(windowId, inv)
                    ));

    public static final RegistryObject<MenuType<MasterWardStoneMenu>> MASTER_WARD_STONE_MENU =
            MENUS.register("master_ward_stone_menu",
                    () -> IForgeMenuType.create((windowId, inv, buf) ->
                            new MasterWardStoneMenu(windowId, inv, buf.readBlockPos())
                    ));
}
