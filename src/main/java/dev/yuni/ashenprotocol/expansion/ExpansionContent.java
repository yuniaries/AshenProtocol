package dev.yuni.ashenprotocol.expansion;

import dev.yuni.ashenprotocol.registry.*;
import dev.yuni.ashenprotocol.AshenProtocol;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.*;

public final class ExpansionContent {
    public static final Map<String, RegistryObject<Item>> ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Block>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<ProtocolMob>>> MOBS = new LinkedHashMap<>();
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AshenProtocol.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.StructureType<?>> STRUCTURES = DeferredRegister.create(net.minecraft.core.registries.Registries.STRUCTURE_TYPE,AshenProtocol.MOD_ID);
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.StructureType<ArchiveStructure>> ARCHIVE_STRUCTURE = STRUCTURES.register("island_archive", () -> () -> ArchiveStructure.CODEC);
    public static final String[] ENEMIES = {"ash_stalker", "marsh_lurker", "frost_sentinel", "rift_hound", "cinder_warden", "tide_weaver", "discord_archon", "last_cartographer", "deep_scavenger", "storm_drone", "root_keeper", "dawn_raider", "abyss_cantor", "storm_sovereign", "root_matriarch", "dawn_regent"};
    public static final String[] MACHINES = {"ash_press", "crystal_refinery", "echo_loom", "alloy_forge", "restoration_array", "world_anchor", "carbon_kiln", "wire_drawer", "circuit_assembler", "steel_foundry", "compost_processor", "bio_refinery", "field_kitchen", "herbal_extractor", "fiber_spinner", "matrix_compressor", "archive_decoder", "precision_lathe", "phase_assembler", "biosphere_restorer"};
    public static final int[] BOSS_VARIANTS={4,5,6,7,12,13,14,15};
    public static final String[] KEYS={"cinder_key","tide_key","rift_key","atlas_key","abyss_key","storm_key","root_key","dawn_key"};
    public static boolean bossVariant(int n){return n>=4&&n<8||n>=12;}
    public static final RegistryObject<BlockEntityType<dev.yuni.ashenprotocol.campaign.ExpeditionArena>> ARENA;
    public static final RegistryObject<BlockEntityType<WorkshopEntity>> WORKSHOP;
    static {
        for (String id : new String[]{"raw_ash", "signal_shard", "rift_dust", "memory_shard", "void_shard", "ash_ingot", "signal_ingot", "rift_ingot", "woven_echo", "tempered_alloy", "restoration_cell", "cinder_core", "tide_core", "rift_core", "atlas_core", "frost_lens", "sun_disk", "archive_chip", "cinder_key", "tide_key", "rift_key", "atlas_key"}) {
            ITEMS.put(id, ModItems.ITEMS.register(id, () -> new Item(new Item.Properties().rarity(id.endsWith("core") ? Rarity.EPIC : Rarity.UNCOMMON))));
        }
        String[] ores = {"ash_ore", "signal_ore", "rift_ore", "memory_ore", "void_ore"};
        for (String id : ores) addBlock(id, () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4, 6).requiresCorrectToolForDrops(), UniformInt.of(2, 5)));
        for (String id : MACHINES) addBlock(id, () -> new WorkshopBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(5, 9).requiresCorrectToolForDrops().sound(SoundType.METAL), id));
        for (int i = 0; i < 4; i++) { final int tier = i; addBlock(new String[]{"cinder_shrine", "tide_shrine", "rift_shrine", "atlas_shrine"}[i], () -> new ShrineBlock(BlockBehaviour.Properties.of().strength(8, 1200).lightLevel(s -> 7), tier)); }
        for (String id : new String[]{"archive_bricks", "signal_glass", "ash_tiles", "rift_tiles"}) addBlock(id, () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3, 6).sound(SoundType.STONE)));
        ITEMS.put("coordinate_gate", ModItems.ITEMS.register("coordinate_gate", () -> new CoordinateGate(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))));
        ITEMS.put("archive_compass", ModItems.ITEMS.register("archive_compass", () -> new ArchiveCompass(new Item.Properties().stacksTo(1))));
        ITEMS.put("resonance_carbine", ModItems.ITEMS.register("resonance_carbine", () -> new ResonanceWeapon(new Item.Properties().durability(1100).rarity(Rarity.RARE), 14, 48, 14, "signal_shard", 4)));
        ITEMS.put("rift_lance", ModItems.ITEMS.register("rift_lance", () -> new ResonanceWeapon(new Item.Properties().durability(1600).rarity(Rarity.EPIC), 24, 56, 24, "rift_dust", 8)));
        ITEMS.put("atlas_caster", ModItems.ITEMS.register("atlas_caster", () -> new ResonanceWeapon(new Item.Properties().durability(2400).rarity(Rarity.EPIC), 32, 64, 30, "void_shard", 12)));
        for (int i = 0; i < 3; i++) {
            String id = new String[]{"ash", "tide", "rift"}[i]; final int n = i;
            Tier tier = new SimpleTier(i);
            ITEMS.put(id + "_blade", ModItems.ITEMS.register(id + "_blade", () -> new SwordItem(tier, 3, -2.4f, new Item.Properties())));
            ITEMS.put(id + "_pickaxe", ModItems.ITEMS.register(id + "_pickaxe", () -> new PickaxeItem(tier, 1, -2.8f, new Item.Properties())));
            for (ArmorItem.Type type : ArmorItem.Type.values()) {
                String suffix = switch(type) { case HELMET -> "helmet"; case CHESTPLATE -> "chestplate"; case LEGGINGS -> "leggings"; case BOOTS -> "boots"; };
                ITEMS.put(id + "_" + suffix, ModItems.ITEMS.register(id + "_" + suffix, () -> new ArmorItem(new ProtocolArmor(n), type, new Item.Properties())));
            }
        }
        for (int i = 0; i < ENEMIES.length; i++) {
            final int variant = i; String id = ENEMIES[i];
            var mob = ENTITIES.register(id, () -> EntityType.Builder.<ProtocolMob>of((type, level) -> new ProtocolMob(type, level, variant), MobCategory.MONSTER).sized(iSize(variant), bossVariant(variant) ? 2.6f : 1.95f).clientTrackingRange(10).build("ashenprotocol:" + id));
            MOBS.put(id, mob);
            ITEMS.put(id + "_spawn_egg", ModItems.ITEMS.register(id + "_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(mob, 0x142531, 0x63e9db + variant * 900, new Item.Properties())));
        }
        dev.yuni.ashenprotocol.campaign.CampaignContent.register();
        ARENA=ModBlockEntities.BLOCK_ENTITIES.register("expedition_arena",()->BlockEntityType.Builder.of(dev.yuni.ashenprotocol.campaign.ExpeditionArena::new,BLOCKS.get("expedition_beacon").get()).build(null));
        WORKSHOP = ModBlockEntities.BLOCK_ENTITIES.register("workshop", () -> BlockEntityType.Builder.of(WorkshopEntity::new, Arrays.stream(MACHINES).map(id -> BLOCKS.get(id).get()).toArray(Block[]::new)).build(null));
    }
    private static float iSize(int i) { return bossVariant(i) ? 0.9f : 0.6f; }
    private static void addBlock(String id, java.util.function.Supplier<Block> factory) {
        var block = ModBlocks.BLOCKS.register(id, factory); BLOCKS.put(id, block);
        ITEMS.put(id, ModItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
    }
    public static Item item(String id) { if(id.contains(":"))return java.util.Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id))); if (id.equals("protocol_fragment")) return ModItems.PROTOCOL_FRAGMENT.get(); if (id.equals("entropy_crystal")) return ModItems.ENTROPY_CRYSTAL.get(); if (id.equals("echo_residue")) return ModItems.ECHO_RESIDUE.get(); return ITEMS.get(id).get(); }
    public static void register(IEventBus bus) { ENTITIES.register(bus); STRUCTURES.register(bus); }
    private record SimpleTier(int index) implements Tier {
        public int getUses() { return new int[]{500, 1100, 1900}[index]; }
        public float getSpeed() { return new float[]{6, 8, 10}[index]; }
        public float getAttackDamageBonus() { return new float[]{2, 3, 5}[index]; }
        public int getLevel() { return index == 0 ? 2 : 3; }
        public int getEnchantmentValue() { return 14 + index * 3; }
        public Ingredient getRepairIngredient() { return Ingredient.of(item(new String[]{"ash_ingot", "signal_ingot", "rift_ingot"}[index])); }
    }
    private record ProtocolArmor(int index) implements ArmorMaterial {
        public int getDurabilityForType(ArmorItem.Type type) { return switch(type) { case HELMET -> 11; case CHESTPLATE -> 16; case LEGGINGS -> 15; case BOOTS -> 13; } * (20 + index * 12); }
        public int getDefenseForType(ArmorItem.Type type) { return switch(type) { case HELMET -> 2 + index / 2; case CHESTPLATE -> 6 + index; case LEGGINGS -> 5 + index; case BOOTS -> 2 + index / 2; }; }
        public int getEnchantmentValue() { return 14 + index * 3; }
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
        public Ingredient getRepairIngredient() { return Ingredient.of(item(new String[]{"ash_ingot", "signal_ingot", "rift_ingot"}[index])); }
        public String getName() { return "ashenprotocol:" + new String[]{"ash", "tide", "rift"}[index]; }
        public float getToughness() { return index; }
        public float getKnockbackResistance() { return index * 0.05f; }
    }
    private ExpansionContent() {}
}
