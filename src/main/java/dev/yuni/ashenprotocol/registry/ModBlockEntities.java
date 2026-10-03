package dev.yuni.ashenprotocol.registry;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.blockentity.ProtocolRelayBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.BLOCK_ENTITY_TYPES,
                    AshenProtocol.MOD_ID
            );

    public static final RegistryObject<BlockEntityType<ProtocolRelayBlockEntity>>
            PROTOCOL_RELAY =
            BLOCK_ENTITIES.register(
                    "protocol_relay",
                    () -> BlockEntityType.Builder.of(
                            ProtocolRelayBlockEntity::new,
                            ModBlocks.PROTOCOL_RELAY.get()
                    ).build(null)
            );

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private ModBlockEntities() {}
}
