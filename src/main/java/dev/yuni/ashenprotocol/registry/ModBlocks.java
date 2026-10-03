package dev.yuni.ashenprotocol.registry;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.block.ProtocolRelayBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    AshenProtocol.MOD_ID
            );

    public static final RegistryObject<Block> PROTOCOL_RELAY =
            BLOCKS.register(
                    "protocol_relay",
                    () -> new ProtocolRelayBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_CYAN)
                                    .strength(4.5F, 8.0F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                                    .lightLevel(state ->
                                            state.getValue(
                                                    ProtocolRelayBlock.ACTIVE
                                            ) ? 12 : 2
                                    )
                    )
            );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private ModBlocks() {}
}
