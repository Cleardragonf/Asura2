package me.cleardragonf.com.spell;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.item.WandItem;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Asura.MODID)
public class WandInputHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var player = event.getEntity();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof WandItem && player.level().isClientSide) {
            me.cleardragonf.com.network.AsuraNetwork.CHANNEL.send(new me.cleardragonf.com.network.msg.C2SLeftClickCast(), net.minecraftforge.network.PacketDistributor.SERVER.noArg());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var player = event.getEntity();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof WandItem && player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            WandItem.castAway(sl, player, stack);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        var player = event.getEntity();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof WandItem && player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            // Cast away when trying to attack with wand
            if (WandItem.castAway(sl, player, stack)) {
                event.setCanceled(true); // prevent melee damage with wand
            }
        }
    }
}
