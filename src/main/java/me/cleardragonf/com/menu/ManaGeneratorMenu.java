package me.cleardragonf.com.menu;

import me.cleardragonf.com.blockentity.ManaGeneratorBlockEntity;
import me.cleardragonf.com.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ManaGeneratorMenu extends AbstractContainerMenu {
    private final ManaGeneratorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // CLIENT constructor (from packet)
    public ManaGeneratorMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        this(id, inv,
                pos != null && inv.player.level().getBlockEntity(pos) instanceof ManaGeneratorBlockEntity be
                        ? be
                        : null,
                new SimpleContainerData(2));
    }

    // SERVER constructor (from openMenu)
    public ManaGeneratorMenu(int id, Inventory inv,
                             @Nullable ManaGeneratorBlockEntity blockEntity,
                             ContainerData data) {
        super(ModMenus.MANA_GENERATOR_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.level = inv.player.level();
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
