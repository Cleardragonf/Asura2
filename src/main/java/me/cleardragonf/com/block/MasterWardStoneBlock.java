package me.cleardragonf.com.block;

import com.mojang.serialization.MapCodec;
import me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import me.cleardragonf.com.menu.MasterWardStoneMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import me.cleardragonf.com.item.WardSpellItem;
import org.jetbrains.annotations.Nullable;

public class MasterWardStoneBlock extends BaseEntityBlock {

    public static final MapCodec<MasterWardStoneBlock> CODEC = simpleCodec(MasterWardStoneBlock::new);

    public MasterWardStoneBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MasterWardStoneBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null; // no ticking for now
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            var mc = Minecraft.getInstance();
            var menu = new MasterWardStoneMenu(0, mc.player.getInventory(), pos);
            mc.setScreen(new me.cleardragonf.com.screen.MasterWardStoneScreen(menu, mc.player.getInventory(), Component.literal("Master Ward Stone")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // If holding a WardSpellItem in either hand, do not open the GUI; allow the item to act
        if (player.getItemInHand(hand).getItem() instanceof WardSpellItem ||
            player.getOffhandItem().getItem() instanceof WardSpellItem) {
            return InteractionResult.PASS;
        }
        // If sneaking, open the GUI explicitly (convenience)
        if (player.isShiftKeyDown()) {
            return useWithoutItem(state, level, pos, player, hit);
        }
        // Open only when empty hand for clarity
        if (player.getItemInHand(hand).isEmpty()) {
            return useWithoutItem(state, level, pos, player, hit);
        }
        return InteractionResult.PASS;
    }
}
