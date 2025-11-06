package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.entity.SpellProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Asura.MODID);

    public static final RegistryObject<EntityType<SpellProjectile>> SPELL_PROJECTILE = ENTITY_TYPES.register(
            "spell_projectile",
            () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(new ResourceLocation(Asura.MODID, "spell_projectile").toString())
    );
}

