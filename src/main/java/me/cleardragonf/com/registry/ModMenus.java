// ModMenus.java
package me.cleardragonf.com.registry;

import me.cleardragonf.com.menu.ManaGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "asura");

    public static final RegistryObject<MenuType<ManaGeneratorMenu>> MANA_GENERATOR_MENU =
            MENUS.register("mana_generator_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) ->
                            // data may be null, so handle both cases!
                            new ManaGeneratorMenu(windowId, inv,
                                    data != null ? data.readBlockPos() : null)));
}
