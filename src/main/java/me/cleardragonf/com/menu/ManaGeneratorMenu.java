package me.cleardragonf.com.menu;

import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ManaGeneratorMenu extends AbstractContainerMenu {
    private final BlockEntity blockEntity;
    private final ContainerData data;

    public ManaGeneratorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.MANA_GENERATOR_MENU.get(), id);
        this.blockEntity = entity;
        this.data = data;

        // Ensure the container has all data fields (mana + type + 6 elements)
        addDataSlots(this.data);
    }

    public ManaGeneratorMenu(int id, Inventory inv) {
        this(id, inv, null, new SimpleContainerData(8)); // ensure it matches the BE’s count
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getMana() {
        return data.get(0);
    }

    public int getManaType() {
        return data.get(1);
    }

    public boolean[] getElements() {
        boolean[] elements = new boolean[6];
        for (int i = 0; i < 6; i++) {
            elements[i] = data.get(2 + i) == 1;
        }
        return elements;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
