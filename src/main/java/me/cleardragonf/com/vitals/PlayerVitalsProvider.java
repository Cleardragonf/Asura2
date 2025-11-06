package me.cleardragonf.com.vitals;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerVitalsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<IPlayerVitals> VITALS_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    public static final ResourceLocation KEY = new ResourceLocation("asura", "vitals");

    private final Player owner;
    private final PlayerVitals backend = new PlayerVitals();
    private final LazyOptional<IPlayerVitals> optional = LazyOptional.of(() -> backend);

    public PlayerVitalsProvider(Player owner) {
        this.owner = owner;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("temperature", backend.getTemperature());
        tag.putInt("thirst", backend.getThirst());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag == null) return;
        backend.setTemperature(tag.getInt("temperature"));
        backend.setThirst(tag.getInt("thirst"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == VITALS_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }
}
