package me.cleardragonf.com.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WardStoneBlock extends Block {

    public static final MapCodec<WardStoneBlock> CODEC = simpleCodec(WardStoneBlock::new);

    public WardStoneBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}

