package dev.yuni.ashenprotocol.registry;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.item.EntropyMeterItem;
import dev.yuni.ashenprotocol.item.PhasePistolItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    AshenProtocol.MOD_ID
            );

    public static final RegistryObject<Item> PROTOCOL_FRAGMENT =
            ITEMS.register(
                    "protocol_fragment",
                    () -> new Item(
                            new Item.Properties()
                                    .rarity(Rarity.UNCOMMON)
                    )
            );

    public static final RegistryObject<Item> ENTROPY_CRYSTAL =
            ITEMS.register(
                    "entropy_crystal",
                    () -> new Item(
                            new Item.Properties()
                                    .rarity(Rarity.RARE)
                    )
            );

    public static final RegistryObject<Item> ECHO_RESIDUE =
            ITEMS.register(
                    "echo_residue",
                    () -> new Item(
                            new Item.Properties()
                                    .rarity(Rarity.UNCOMMON)
                    )
            );

    public static final RegistryObject<Item> ENTROPY_METER =
            ITEMS.register(
                    "entropy_meter",
                    () -> new EntropyMeterItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static final RegistryObject<Item> PHASE_PISTOL =
            ITEMS.register(
                    "phase_pistol",
                    () -> new PhasePistolItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(640)
                                    .rarity(Rarity.RARE)
                    )
            );

    public static final RegistryObject<Item> PROTOCOL_RELAY_ITEM =
            ITEMS.register(
                    "protocol_relay",
                    () -> new BlockItem(
                            ModBlocks.PROTOCOL_RELAY.get(),
                            new Item.Properties()
                                    .rarity(Rarity.UNCOMMON)
                    )
            );

    public static final RegistryObject<Item> FIELD_GUIDE = ITEMS.register(
            "field_guide", () -> new dev.yuni.ashenprotocol.item.FieldGuideItem(
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENTROPY_SINK = ITEMS.register("entropy_sink",
            () -> new dev.yuni.ashenprotocol.item.EntropySinkItem(new Item.Properties().durability(256).rarity(Rarity.RARE)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
