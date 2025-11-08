package me.cleardragonf.com.item;

import me.cleardragonf.com.blockentity.MasterWardStoneBlockEntity;
import me.cleardragonf.com.ward.WardSpec;
import me.cleardragonf.com.ward.WardSpecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WardSpellItem extends Item {
    private final @Nullable String fixedWardName;
    private final @Nullable WardSpec wardSpec;

    public WardSpellItem(Properties properties) { this(properties, null); }
    public WardSpellItem(Properties properties, @Nullable String fixedWardName) { this(properties, fixedWardName, null); }
    public WardSpellItem(Properties properties, @Nullable String fixedWardName, @Nullable WardSpec spec) {
        super(properties);
        this.fixedWardName = fixedWardName;
        this.wardSpec = spec;
        if (spec != null) {
            WardSpecs.register(spec);
        }
    }

    protected String resolveWardName(UseOnContext ctx) {
        if (fixedWardName != null && !fixedWardName.isBlank()) return fixedWardName;
        // Fallback: use the stack display name (anvil-renamable)
        return ctx.getItemInHand().getHoverName().getString();
    }

    @Nullable
    public WardSpec getWardSpec() { return wardSpec; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();

        BlockEntity be = level.getBlockEntity(ctx.getClickedPos());
        if (be instanceof MasterWardStoneBlockEntity && !level.isClientSide) {
            MasterWardStoneBlockEntity master = (MasterWardStoneBlockEntity) be;
            String name = resolveWardName(ctx);
            master.addWardName(name);
            if (ctx.getPlayer() != null) {
                ctx.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Added ward '" + name + "'"), true);
            }
            // Ensure schema is registered so defaults can be applied on server
            WardSpec spec = this.getWardSpec();
            if (spec != null) WardSpecs.register(spec);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
