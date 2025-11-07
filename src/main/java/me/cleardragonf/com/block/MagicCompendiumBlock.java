package me.cleardragonf.com.block;

import me.cleardragonf.com.item.WandItem;
import me.cleardragonf.com.spell.SpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;

public class MagicCompendiumBlock extends Block {
    public MagicCompendiumBlock(Properties props) { super(props); }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && held.getItem() instanceof WandItem) {
            // Teach all spells and open programming screen
            var all = new ArrayList<String>();
            for (var s : SpellRegistry.all()) {
                all.add(s.id());
                WandItem.setSpellLevel(held, s.id(), 1);
            }
            WandItem.setLearned(held, all);
            WandItem.setSelectedIndex(held, 0);
            ((net.minecraft.server.level.ServerPlayer) player).openMenu(
                    new net.minecraft.world.SimpleMenuProvider(
                            (id, inv, p) -> new me.cleardragonf.com.menu.MagicCompendiumMenu(id, inv),
                            net.minecraft.network.chat.Component.literal("Magic Compendium")
                    )
            );
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
