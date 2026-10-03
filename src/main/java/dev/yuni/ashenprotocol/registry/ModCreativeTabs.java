package dev.yuni.ashenprotocol.registry;

import dev.yuni.ashenprotocol.AshenProtocol;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AshenProtocol.MOD_ID);
    static {
        TABS.register("protocol", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ashenprotocol"))
            .icon(() -> new ItemStack(ModItems.ENTROPY_METER.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.FIELD_GUIDE.get());
                output.accept(ModItems.PROTOCOL_FRAGMENT.get());
                output.accept(ModItems.ENTROPY_CRYSTAL.get());
                output.accept(ModItems.ECHO_RESIDUE.get());
                output.accept(ModItems.ENTROPY_METER.get());
                output.accept(ModItems.PROTOCOL_RELAY_ITEM.get());
                output.accept(ModItems.PHASE_PISTOL.get());
                output.accept(ModItems.ENTROPY_SINK.get());
            }).build());
    }
    public static void register(IEventBus bus) { TABS.register(bus); }
}

