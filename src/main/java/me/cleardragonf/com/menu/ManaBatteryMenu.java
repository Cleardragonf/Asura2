package me.cleardragonf.com.menu;

import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ManaBatteryMenu extends AbstractContainerMenu {
    private final BlockEntity blockEntity;
    private final ContainerData data;

    public ManaBatteryMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.MANA_BATTERY_MENU.get(), id);
        this.blockEntity = entity;
        this.data = data;
        addDataSlots(this.data);
    }

    public ManaBatteryMenu(int id, Inventory inv) {
        this(id, inv, null, new SimpleContainerData(2));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStored() {
        return data.get(0);
    }

    public int getCapacity() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}

