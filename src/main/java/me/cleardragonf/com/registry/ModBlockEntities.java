package me.cleardragonf.com.registry;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import me.cleardragonf.com.blockentity.ManaGeneratorBlockEntity;
import me.cleardragonf.com.blockentity.ManaRelayBlockEntity;
import me.cleardragonf.com.blockentity.ManaConverterBlockEntity;
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

    public static final RegistryObject<BlockEntityType<ManaBatteryBlockEntity>> MANA_BATTERY_ENTITY =
            BLOCK_ENTITIES.register("mana_battery_entity",
                    () -> BlockEntityType.Builder.of(
                            ManaBatteryBlockEntity::new,
                            ModBlocks.MANA_BATTERY.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<ManaRelayBlockEntity>> MANA_RELAY_ENTITY =
            BLOCK_ENTITIES.register("mana_relay_entity",
                    () -> BlockEntityType.Builder.of(
                            ManaRelayBlockEntity::new,
                            ModBlocks.MANA_RELAY.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<ManaConverterBlockEntity>> MANA_CONVERTER_ENTITY =
            BLOCK_ENTITIES.register("mana_converter_entity",
                    () -> BlockEntityType.Builder.of(
                            ManaConverterBlockEntity::new,
                            ModBlocks.MANA_CONVERTER.get()
                    ).build(null));
}
