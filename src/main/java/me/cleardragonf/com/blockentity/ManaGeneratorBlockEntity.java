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

    private final boolean[] elements = new boolean[6]; // Light, Dark, Fire, Water, Earth, Air

    public void tickServer() {
        if (level == null || level.isClientSide) return;

        updateElements(level, worldPosition);
        manaType = getDominantElement();
        mana += getManaPerTick(manaType);
        mana = Math.min(mana, 100);

        data.set(0, mana);
        data.set(1, getManaTypeIndex());
        for (int i = 0; i < 6; i++) data.set(2 + i, elements[i] ? 1 : 0);
    }

    private void updateElements(Level level, BlockPos pos) {
        boolean foundLight = level.canSeeSky(pos.above()) && level.isDay();
        boolean foundDark = !level.canSeeSky(pos) && !level.isDay();
        boolean foundFire = scanForBlock(level, pos, Blocks.FIRE, Blocks.LAVA);
        boolean foundWater = scanForBlock(level, pos, Blocks.WATER);
        boolean foundEarth = scanForBlock(level, pos, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE);
        boolean foundAir = scanForBlock(level, pos, Blocks.AIR);

        elements[0] = foundLight;
        elements[1] = foundDark;
        elements[2] = foundFire;
        elements[3] = foundWater;
        elements[4] = foundEarth;
        elements[5] = foundAir;
    }

    private boolean scanForBlock(Level level, BlockPos origin, Block... targets) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -6, -6), origin.offset(6, 6, 6))) {
            Block b = level.getBlockState(pos).getBlock();
            for (Block t : targets) if (b == t) return true;
        }
        return false;
    }

    private String getDominantElement() {
        if (elements[2]) return "Fire";
        if (elements[3]) return "Water";
        if (elements[0]) return "Light";
        if (elements[1]) return "Dark";
        if (elements[4]) return "Earth";
        if (elements[5]) return "Air";
        return "None";
    }

    public boolean[] getElements() { return elements; }

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
