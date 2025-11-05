package me.cleardragonf.com.item;

import me.cleardragonf.com.blockentity.ManaRelayBlockEntity;
import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import me.cleardragonf.com.blockentity.ManaGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkingWandItem extends Item {
    public LinkingWandItem(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var pos = ctx.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);

        var player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        UUID id = player.getUUID();
        Selection sel = Selection.STATE.computeIfAbsent(id, k -> new Selection());

        if (be instanceof ManaGeneratorBlockEntity) {
            sel.source = pos;
            sel.pendingRelay = null;
            ctx.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Selected Generator"), true);
            return InteractionResult.CONSUME;
        }

        if (be instanceof ManaRelayBlockEntity) {
            // If we have a selected generator, set it as source on this relay
            if (sel.source != null) {
                ((ManaRelayBlockEntity) be).setSource(sel.source);
                sel.source = null;
                sel.pendingRelay = pos;
                ctx.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Linked Source → Relay. Now select a Battery"), true);
                return InteractionResult.CONSUME;
            } else {
                // No source pending: mark this relay as the one to set target
                sel.pendingRelay = pos;
                ctx.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Selected Relay. Now click a Battery"), true);
                return InteractionResult.CONSUME;
            }
        }

        if (be instanceof ManaBatteryBlockEntity) {
            if (sel.pendingRelay != null) {
                BlockEntity rbe = level.getBlockEntity(sel.pendingRelay);
                if (rbe instanceof ManaRelayBlockEntity relay) {
                    relay.setTarget(pos);
                    sel.pendingRelay = null;
                    ctx.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Linked Relay → Battery"), true);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }
}

class Selection {
    public BlockPos source;
    public BlockPos pendingRelay;
    public static final ConcurrentHashMap<UUID, Selection> STATE = new ConcurrentHashMap<>();
}
