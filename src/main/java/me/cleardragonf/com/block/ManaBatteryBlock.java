package me.cleardragonf.com.block;

import com.mojang.serialization.MapCodec;
import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ManaBatteryBlock extends BaseEntityBlock {

    public static final MapCodec<ManaBatteryBlock> CODEC = simpleCodec(ManaBatteryBlock::new);

    public ManaBatteryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaBatteryBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            ManaBatteryBlockEntity.reformCluster(level, pos);
        }
    }

    // (first onRemove removed; merged into the later override below)

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                                   Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof net.minecraft.world.MenuProvider provider) {
                player.openMenu(provider);
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static final ThreadLocal<Boolean> BREAKING_CLUSTER = ThreadLocal.withInitial(() -> false);

    private static float computeExplosionPower(Level level, BlockPos anyPosInCluster) {
        int totalStored = 0;
        int blocks = 1;
        BlockEntity be = level.getBlockEntity(anyPosInCluster);
        if (be instanceof ManaBatteryBlockEntity battery) {
            totalStored = Math.max(0, battery.getStored()); // controller holds total
            // approximate cluster size from capacity to avoid another flood fill
            int cap = Math.max(1, battery.getCapacity());
            blocks = Math.max(1, cap / ManaBatteryBlockEntity.CAPACITY_PER_BLOCK);
        } else {
            var cluster = ManaBatteryBlockEntity.getCluster(level, anyPosInCluster);
            blocks = Math.max(1, cluster.size());
            for (BlockPos p : cluster) {
                BlockEntity be2 = level.getBlockEntity(p);
                if (be2 instanceof ManaBatteryBlockEntity b2) {
                    totalStored = Math.max(0, b2.getStored());
                    break;
                }
            }
        }

        // Explosive power scales with the combination of all blocks: use absolute stored mana
        // Normalize by 1000 mana (one block capacity) and grow sub-exponentially to avoid absurd radii
        float normalized = totalStored / (float) ManaBatteryBlockEntity.CAPACITY_PER_BLOCK; // ~equivalent blocks worth of mana
        // Include slight boost for larger structures regardless of fill
        float sizeBoost = (float) Math.log10(1 + blocks); // 0..~1.3 for very large
        // Base + curved term; clamp to reasonable maximum
        float power = 1.0f + (float) (Math.cbrt(Math.max(0f, normalized)) * 3.5 + sizeBoost);
        return Math.min(power, 24.0f);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) chainExplodeAndBreakCluster(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) chainExplodeAndBreakCluster(level, pos);
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // If this block is being removed/replaced (but not moving), and it's a battery, try chaining
        if (!level.isClientSide && !isMoving && newState.getBlock() != this) {
            chainExplodeAndBreakCluster(level, pos);
        }
        if (!level.isClientSide) {
            ManaBatteryBlockEntity.reformNeighbors(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void chainExplodeAndBreakCluster(Level level, BlockPos origin) {
        if (BREAKING_CLUSTER.get()) return;
        BREAKING_CLUSTER.set(true);
        try {
            float power = computeExplosionPower(level, origin);
            var clusterSet = ManaBatteryBlockEntity.getCluster(level, origin);
            List<BlockPos> cluster = new ArrayList<>(clusterSet);
            // Trigger explosions first
            for (BlockPos p : cluster) {
                try {
                    level.explode(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, power,
                            net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
                } catch (Throwable t) {
                    // ignore; we will ensure break below
                }
            }
            // Ensure every remaining battery block is removed
            for (BlockPos p : cluster) {
                if (level.getBlockState(p).getBlock() instanceof ManaBatteryBlock) {
                    // Avoid double drops; explosion may already have dropped items
                    level.destroyBlock(p, false);
                }
            }
        } finally {
            BREAKING_CLUSTER.set(false);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide ? (lvl, p, st, be) -> {
            if (be instanceof ManaBatteryBlockEntity battery) {
                battery.serverTick();
            }
        } : null;
    }
}
