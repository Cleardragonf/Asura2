package me.cleardragonf.com.item;

import me.cleardragonf.com.data.Config;
import me.cleardragonf.com.vitals.PlayerVitalsProvider;
import me.cleardragonf.com.vitals.net.ModNetwork;
import me.cleardragonf.com.vitals.net.VitalsSyncPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CanteenItem extends Item {
    private static final String TAG_FILLED = "Filled";

    public CanteenItem(Properties props) {
        super(props);
    }

    public static boolean isFilled(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = me.cleardragonf.com.util.ItemData.getOrCreate(stack);
        return tag.getBoolean(TAG_FILLED);
    }

    public static void setFilled(ItemStack stack, boolean filled) {
        net.minecraft.nbt.CompoundTag tag = me.cleardragonf.com.util.ItemData.getOrCreate(stack);
        tag.putBoolean(TAG_FILLED, filled);
        me.cleardragonf.com.util.ItemData.set(stack, tag);
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 24;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isFilled(stack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        // Try to fill from a water source block where the player is looking
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            var pos = hit.getBlockPos();
            if (level.getBlockState(pos).is(Blocks.WATER)) {
                if (!level.isClientSide) {
                    setFilled(stack, true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (isFilled(stack)) {
                player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
                    v.addThirst(+Config.thirstGainCanteen);
                    if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                        ModNetwork.sendToPlayer(sp, new VitalsSyncPacket(v.getTemperature(), v.getThirst()));
                    }
                });
                setFilled(stack, false);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        return stack;
    }
}
