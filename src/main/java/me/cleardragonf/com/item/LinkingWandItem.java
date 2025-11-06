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

        // Generators clicked: select as source or add to pending relay inputs
        if (be instanceof ManaGeneratorBlockEntity) {
            if (sel.pendingRelay != null) {
                BlockEntity rbe = level.getBlockEntity(sel.pendingRelay);
                if (rbe instanceof ManaRelayBlockEntity relay) {
                    relay.addInput(pos);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Added Generator → Relay input"), true);
                    return InteractionResult.CONSUME;
                }
            }
            sel.source = pos;
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Selected Generator as Source"), true);
            return InteractionResult.CONSUME;
        }

        // Relays clicked: can be input source, or output target, or selection
        if (be instanceof ManaRelayBlockEntity) {
            ManaRelayBlockEntity clickedRelay = (ManaRelayBlockEntity) be;
            // If we have a selected source (generator or relay), add it as input
            if (sel.source != null) {
                clickedRelay.addInput(sel.source);
                sel.source = null;
                sel.pendingRelay = pos;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Linked Input → Relay. Now select Output (Relay or Battery)"), true);
                return InteractionResult.CONSUME;
            }
            // If a different relay is pending, set that relay's output to this relay
            if (sel.pendingRelay != null && !sel.pendingRelay.equals(pos)) {
                BlockEntity from = level.getBlockEntity(sel.pendingRelay);
                if (from instanceof ManaRelayBlockEntity r) {
                    r.setOutput(pos);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Set Relay Output → Relay"), true);
                    sel.pendingRelay = null;
                    return InteractionResult.CONSUME;
                }
            }
            // Otherwise, select this relay to modify
            sel.pendingRelay = pos;
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Selected Relay. Click Generator/Relay to add input, or Battery/Relay to set output."), true);
            return InteractionResult.CONSUME;
        }

        // Batteries clicked: set output on pending relay
        if (be instanceof ManaBatteryBlockEntity) {
            if (sel.pendingRelay != null) {
                BlockEntity rbe = level.getBlockEntity(sel.pendingRelay);
                if (rbe instanceof ManaRelayBlockEntity relay) {
                    relay.setOutput(pos);
                    sel.pendingRelay = null;
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Set Relay Output → Battery"), true);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}

class Selection {
    public BlockPos source;
    public BlockPos pendingRelay;
    public static final ConcurrentHashMap<UUID, Selection> STATE = new ConcurrentHashMap<>();
}

