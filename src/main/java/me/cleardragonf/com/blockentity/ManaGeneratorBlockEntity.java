package me.cleardragonf.com.blockentity;

import me.cleardragonf.com.menu.ManaGeneratorMenu;
import me.cleardragonf.com.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    private final ContainerData data = new SimpleContainerData(8); // mana + manaType + 6 elements

    public ManaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_GENERATOR_ENTITY.get(), pos, state);
    }

    private final int[] elementCounts = new int[6]; // Light, Dark, Fire, Water, Earth, Air

    public void tickServer() {
        if (level == null || level.isClientSide) return;

        updateElements(level, worldPosition);
        manaType = getDominantElement();
        mana += getManaPerTick(manaType);
        mana = Math.min(mana, 100);

        if (mana > 0) {
            mana -= pushToAdjacentBatteries(level, worldPosition, mana);
        }

        data.set(0, mana);
        data.set(1, getManaTypeIndex());
        for (int i = 0; i < 6; i++) data.set(2 + i, elementCounts[i]);
    }

    private int pushToAdjacentBatteries(Level level, BlockPos pos, int available) {
        int sent = 0;
        for (Direction d : Direction.values()) {
            if (available - sent <= 0) break;
            BlockPos adj = pos.relative(d);
            BlockEntity be = level.getBlockEntity(adj);
            if (be instanceof ManaBatteryBlockEntity battery) {
                int accepted = battery.addMana(available - sent);
                sent += accepted;
            }
        }
        return sent;
    }

    private void updateElements(Level level, BlockPos pos) {
        // Reset
        for (int i = 0; i < 6; i++) elementCounts[i] = 0;

        // Light/Dark as conditions (presence 0/1)
        elementCounts[0] = (level.canSeeSky(pos.above()) && level.isDay()) ? 1 : 0; // Light
        elementCounts[1] = (!level.canSeeSky(pos) && !level.isDay()) ? 1 : 0;        // Dark

        // Count blocks in a 13x13x13 cube
        elementCounts[2] = countBlocks(level, pos, Blocks.FIRE, Blocks.LAVA); // Fire
        elementCounts[3] = countBlocks(level, pos, Blocks.WATER);              // Water
        elementCounts[4] = countBlocks(level, pos, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE); // Earth
        elementCounts[5] = countBlocks(level, pos, Blocks.AIR);                // Air
    }

    private int countBlocks(Level level, BlockPos origin, Block... targets) {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-6, -6, -6), origin.offset(6, 6, 6))) {
            Block b = level.getBlockState(p).getBlock();
            for (Block t : targets) {
                if (b == t) { count++; break; }
            }
        }
        return count;
    }

    private String getDominantElement() {
        if (elementCounts[2] > 0) return "Fire";
        if (elementCounts[3] > 0) return "Water";
        if (elementCounts[0] > 0) return "Light";
        if (elementCounts[1] > 0) return "Dark";
        if (elementCounts[4] > 0) return "Earth";
        if (elementCounts[5] > 0) return "Air";
        return "None";
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
