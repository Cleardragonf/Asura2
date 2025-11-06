package me.cleardragonf.com.menu;

import me.cleardragonf.com.blockentity.ManaConverterBlockEntity;
import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ManaConverterMenu extends AbstractContainerMenu {
    private final ManaConverterBlockEntity be;
    private final ContainerData data;

    public ManaConverterMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.MANA_CONVERTER_MENU.get(), id);
        this.be = entity instanceof ManaConverterBlockEntity c ? c : null;
        this.data = data;

        if (be != null) {
            Container cont = be.getInventory();
            // Input slot (0)
            this.addSlot(new Slot(cont, 0, 44, 35) {
                @Override
                public int getMaxStackSize(ItemStack stack) { return 1; }
                @Override
                public boolean mayPlace(ItemStack stack) { return be.getCostFor(stack) > 0; }
            });
            // Output slot (1)
            this.addSlot(new Slot(cont, 1, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
            });
        } else {
            // Fallback dummy container to keep client from crashing
            SimpleContainer dummy = new SimpleContainer(2);
            this.addSlot(new Slot(dummy, 0, 44, 35));
            this.addSlot(new Slot(dummy, 1, 116, 35));
        }

        // Player inventory
        int startY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }
        for (int hotbar = 0; hotbar < 9; ++hotbar) {
            this.addSlot(new Slot(inv, hotbar, 8 + hotbar * 18, startY + 58));
        }

        addDataSlots(this.data);
    }

    public ManaConverterMenu(int id, Inventory inv) {
        this(id, inv, null, new SimpleContainerData(2));
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public int getBuffer() { return data.get(0); }
    public int getCost() { return data.get(1); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Basic shift-click: move from player to input if valid, otherwise from output to player
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();
            if (index == 1) { // output slot index
                if (!this.moveItemStackTo(stack, 2, 2 + 36, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, ret);
            } else if (index >= 2) { // player inv -> input
                if (be != null && be.getCostFor(stack) > 0) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
                } else if (index < 2 + 27) {
                    if (!this.moveItemStackTo(stack, 2 + 27, 2 + 36, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(stack, 2, 2 + 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 0) { // input -> player
                if (!this.moveItemStackTo(stack, 2, 2 + 36, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == ret.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return ret;
    }
}
