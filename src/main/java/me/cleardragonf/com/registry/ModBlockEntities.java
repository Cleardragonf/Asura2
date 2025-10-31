package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.blockentity.ManaGeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Asura.MODID);

    public static final RegistryObject<BlockEntityType<ManaGeneratorBlockEntity>> MANA_GENERATOR_ENTITY =
            BLOCK_ENTITIES.register("mana_generator_entity",
                    () -> BlockEntityType.Builder.of(
                            ManaGeneratorBlockEntity::new,
                            ModBlocks.MANA_GENERATOR.get()
                    ).build(null));
}
