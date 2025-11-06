package me.cleardragonf.com.vitals;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.vitals.net.ModNetwork;
import me.cleardragonf.com.vitals.net.VitalsSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Asura.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VitalsEvents {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(PlayerVitalsProvider.KEY, new PlayerVitalsProvider(player));
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        // Preserve vitals on death/respawn
        Player original = event.getOriginal();
        Player target = event.getEntity();
        original.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(oldCap ->
            target.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(newCap -> {
                newCap.setTemperature(oldCap.getTemperature());
                newCap.setThirst(oldCap.getThirst());
            })
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (level.isClientSide) return;

        player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
            // Environmental temperature target from biome + exposure
            int target = computeTargetTemperature(player);
            int cur = v.getTemperature();
            // Move temperature slowly towards target
            int step = Integer.compare(target, cur); // -1,0,1
            v.addTemperature(step);

            // Thirst passive drain, more when sprinting
            if (player.tickCount % Math.max(1, me.cleardragonf.com.data.Config.thirstDrainInterval) == 0) {
                int drain = player.isSprinting() ? me.cleardragonf.com.data.Config.thirstDrainSprinting : me.cleardragonf.com.data.Config.thirstDrainIdle;
                v.addThirst(-drain);
            }

            // Effects at extremes
            applyEffects(player, v);

            // Sync to client every second or on thresholds
            if (player.tickCount % 20 == 0 || player.tickCount % 5 == 0 && (Math.abs(cur) >= 75 || v.getThirst() <= 10)) {
                ModNetwork.sendToPlayer((ServerPlayer) player, new VitalsSyncPacket(v.getTemperature(), v.getThirst()));
            }
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        sp.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v ->
                me.cleardragonf.com.vitals.net.ModNetwork.sendToPlayer(sp,
                        new me.cleardragonf.com.vitals.net.VitalsSyncPacket(v.getTemperature(), v.getThirst()))
        );
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        sp.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v ->
                me.cleardragonf.com.vitals.net.ModNetwork.sendToPlayer(sp,
                        new me.cleardragonf.com.vitals.net.VitalsSyncPacket(v.getTemperature(), v.getThirst()))
        );
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> v.addThirst(-1));
    }

    @SubscribeEvent
    public static void onItemConsumed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack stack = event.getItem();
        player.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
            if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER) {
                v.addThirst(+me.cleardragonf.com.data.Config.thirstGainWater);
            } else if (stack.is(Items.MILK_BUCKET)) {
                v.addThirst(+me.cleardragonf.com.data.Config.thirstGainMilk);
            } else if (stack.is(Items.BEETROOT_SOUP) || stack.is(Items.MUSHROOM_STEW) || stack.is(Items.RABBIT_STEW)) {
                v.addThirst(+me.cleardragonf.com.data.Config.thirstGainSoup);
            }
        });
    }

    private static void applyEffects(Player player, IPlayerVitals v) {
        if (v.isDehydrated()) {
            if (player.tickCount % 40 == 0) {
                player.hurt(player.damageSources().starve(), 1.0f);
            }
        }
        if (v.getTemperature() <= me.cleardragonf.com.data.Config.tempHypoThreshold) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false, true));
            if (player.tickCount % 60 == 0) {
                player.hurt(player.damageSources().freeze(), 1.0f);
            }
        }
        if (v.getTemperature() >= me.cleardragonf.com.data.Config.tempHyperThreshold) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false, true));
            if (player.tickCount % 60 == 0) {
                player.hurt(player.damageSources().onFire(), 1.0f);
            }
        }
    }

    private static int computeTargetTemperature(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // Base from biome (scaled -20..+20 around 0)
        Biome biome = level.getBiome(pos).value();
        float baseTemp = biome.getBaseTemperature(); // around 0..2, 0.15 cold, 1.0 temperate, >1 hot
        int target = Math.round((baseTemp - 1.0f) * 40f);

        // Time of day: night cooler
        boolean isDay = level.isDay();
        target += isDay ? 0 : -5;

        // Weather: rain/snow cools
        if (level.isRainingAt(pos)) target -= 8;

        // Exposure: shade vs. sun
        boolean skyVisible = level.canSeeSky(pos.above());
        if (skyVisible && isDay && !level.isRainingAt(pos)) target += 6; // sun heats
        if (!skyVisible) target -= 3; // shade cools a bit

        // Block adjacency: near fire/lava heats, water cools
        if (isNearBlock(player, Blocks.LAVA, 4) || player.isOnFire()) target += 20;
        if (player.isInWaterRainOrBubble()) target -= 10;

        // Altitude: high is colder
        target += Mth.clamp(64 - pos.getY(), -10, 10);

        // Armor insulation: leather cool climates warm you, heavy armor warms in cold but heats in hot
        int insulation = 0;
        var head = player.getItemBySlot(EquipmentSlot.HEAD);
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        var legs = player.getItemBySlot(EquipmentSlot.LEGS);
        var feet = player.getItemBySlot(EquipmentSlot.FEET);
        insulation += armorInsulation(head);
        insulation += armorInsulation(chest);
        insulation += armorInsulation(legs);
        insulation += armorInsulation(feet);
        target += insulation;

        return Mth.clamp(target, -100, 100);
    }

    private static boolean isNearBlock(Player player, net.minecraft.world.level.block.Block block, int radius) {
        BlockPos p = player.blockPosition();
        Level level = player.level();
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++)
                for (int dz = -radius; dz <= radius; dz++) {
                    if (level.getBlockState(p.offset(dx, dy, dz)).is(block)) return true;
                }
        return false;
    }

    private static int armorInsulation(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        // Granular tags take precedence, pick the largest magnitude that applies
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_P4)) return 4;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_M4)) return -4;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_P3)) return 3;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_M3)) return -3;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_P2)) return 2;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_M2)) return -2;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_P1)) return 1;
        if (stack.is(me.cleardragonf.com.registry.ModTags.Items.INSULATION_M1)) return -1;
        return 0;
    }
}
