package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.menu.ManaGeneratorMenu;
import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Handles mana generation based on environmental conditions,
 * and synchronizes data with the client-side GUI.
 */
public class ManaGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    private int mana = 0;
    private String manaType = "None";

    // Sync container (2 slots: mana amount + mana type)
    private final ContainerData data = new SimpleContainerData(2);

    public ManaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_GENERATOR_ENTITY.get(), pos, state);
    }

    /** Called every tick on the server side to generate mana */
    public void tickServer() {
        if (level == null || level.isClientSide) return;

        manaType = detectManaType(level, worldPosition);
        mana += getManaPerTick(manaType);
        mana = Math.min(mana, 100); // clamp

        // Sync to client via container
        data.set(0, mana);
        data.set(1, getManaTypeIndex());
    }

    /** Determines the environment-based mana type */
    private String detectManaType(Level level, BlockPos pos) {
        if (level.getBlockState(pos.below()).is(Blocks.WATER)) {
            return "Water";
        }
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(Blocks.FIRE)) {
                return "Fire";
            }
        }
        if (level.canSeeSky(pos.above()) && level.isDay()) {
            return "Light";
        }
        return "None";
    }

    /** Determines mana generation rate based on environment */
    private int getManaPerTick(String type) {
        return switch (type) {
            case "Water" -> 2;
            case "Fire" -> 4;
            case "Light" -> 3;
            default -> 0;
        };
    }

    /** Converts mana type string → index for network sync */
    private int getManaTypeIndex() {
        return switch (manaType) {
            case "Water" -> 1;
            case "Fire" -> 2;
            case "Light" -> 3;
            default -> 0;
        };
    }

    public int getMana() {
        return mana;
    }

    public String getManaType() {
        return manaType;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ManaGeneratorMenu(id, inv, this, this.data);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Mana Generator");
    }

}
