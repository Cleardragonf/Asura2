package me.cleardragonf.com.menu;

import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MasterWardStoneMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    public MasterWardStoneMenu(int id, Inventory inv, BlockPos pos) {
        super(ModMenus.MASTER_WARD_STONE_MENU.get(), id);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}

