package dev.yuni.ashenprotocol;

import dev.yuni.ashenprotocol.config.APConfig;
import dev.yuni.ashenprotocol.registry.ModBlockEntities;
import dev.yuni.ashenprotocol.registry.ModBlocks;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AshenProtocol.MOD_ID)
public final class AshenProtocol {

    public static final String MOD_ID = "ashenprotocol";

    public AshenProtocol() {
        dev.yuni.ashenprotocol.network.ProtocolNetwork.register();
        IEventBus modBus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        dev.yuni.ashenprotocol.registry.ModCreativeTabs.register(modBus);
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                APConfig.SPEC
        );
    }
}
