package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.*;
import dev.yuni.ashenprotocol.registry.*;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;

public final class CampaignContent {
    public static final String[] CROPS={"ash_wheat","signal_berry","tide_rice","frost_root","sun_pepper","echo_bean","rift_melon","memory_herb"};
    public static final String[] MATERIALS={"carbon_dust","carbon_plate","copper_wire","signal_circuit","echo_circuit","rift_circuit","atlas_circuit","steel_ingot","ceramic_plate","lattice_frame","rotor","pump_head","filter_mesh","fertile_compost","bio_pulp","bio_fuel","nutrient_gel","healing_extract","frost_extract","thermal_extract","echo_fiber","conductive_thread","crystal_matrix","memory_matrix","void_matrix","sealed_archive","phase_coil","navigation_array","restoration_heart","biosphere_core","abyss_core","storm_core","root_core","dawn_core","abyss_key","storm_key","root_key","dawn_key","contract_token","expedition_seal","deep_ore_fragment","deep_ingot","storm_shard","root_resin","dawn_fragment","harmonic_ingot","anchor_module","solar_cell","water_filter","precision_gear","reinforced_plate","pressure_valve"};
    public static final String[] MACHINES={"carbon_kiln","wire_drawer","circuit_assembler","steel_foundry","compost_processor","bio_refinery","field_kitchen","herbal_extractor","fiber_spinner","matrix_compressor","archive_decoder","precision_lathe","phase_assembler","biosphere_restorer"};
    public static final String[] FOODS={"field_bread","berry_jam","rice_bowl","root_stew","pepper_ration","bean_soup","melon_slice","herbal_tea","expedition_meal","frost_ration","thermal_ration","echo_ration","healing_broth","root_pie","rice_cake","bean_bread","berry_pie","preserved_meat","spiced_stew","signal_salad","memory_cookie","fortified_ration","guardian_feast","dawn_feast"};
    public static final String[] SUPPLIES={"field_bandage","restoration_salve","purity_capsule","thermal_capsule","frost_capsule","echo_capsule"};
    public static final String[] DECOR={"carbon_bricks","steel_tiles","ceramic_tiles","circuit_panel","echo_panel","root_bricks","dawn_bricks","abyss_bricks","storm_bricks","memory_tiles","copper_grate","reinforced_glass","archive_lamp","signal_lamp","root_lamp","dawn_lamp"};
    public static final String[] EQUIPMENT={"steel","echo","harmonic"};
    public static void register() {
        for(String id:MATERIALS) ordinary(id);
        for(String id:CROPS) {
            ordinary(id);
            RegistryObject<Block> crop=ModBlocks.BLOCKS.register(id+"_crop",()->new ProtocolCrop(BlockBehaviour.Properties.copy(Blocks.WHEAT),id));
            ExpansionContent.BLOCKS.put(id+"_crop",crop);
            ExpansionContent.ITEMS.put(id+"_seeds",ModItems.ITEMS.register(id+"_seeds",()->new ItemNameBlockItem(crop.get(),new Item.Properties())));
        }
        for(int i=0;i<FOODS.length;i++) {
            final int index=i;String id=FOODS[i];
            ExpansionContent.ITEMS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().food(food(index)))));
        }
        for(int i=0;i<SUPPLIES.length;i++) {
            final int index=i;String id=SUPPLIES[i];
            ExpansionContent.ITEMS.put(id,ModItems.ITEMS.register(id,()->new SupplyItem(new Item.Properties().stacksTo(16),index)));
        }
        for(int i=0;i<4;i++){final int tier=i+4;block(new String[]{"abyss_shrine","storm_shrine","root_shrine","dawn_shrine"}[i],()->new ShrineBlock(BlockBehaviour.Properties.of().strength(8,1200).lightLevel(s->7),tier));}
        for(String id:DECOR) block(id,()->new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).lightLevel(s->id.endsWith("lamp")?12:0)));
        for(int i=0;i<EQUIPMENT.length;i++) {
            String prefix=EQUIPMENT[i];final int tier=i;
            var toolTier=new CampaignTier(tier);
            register(prefix+"_blade",()->new SwordItem(toolTier,3,-2.4f,new Item.Properties()));
            register(prefix+"_pickaxe",()->new PickaxeItem(toolTier,1,-2.8f,new Item.Properties()));
            register(prefix+"_axe",()->new AxeItem(toolTier,5,-3,new Item.Properties()));
            register(prefix+"_shovel",()->new ShovelItem(toolTier,1,-3,new Item.Properties()));
            for(ArmorItem.Type type:ArmorItem.Type.values())register(prefix+"_"+type.getName(),()->new ArmorItem(new CampaignArmor(tier),type,new Item.Properties()));
        }
        register("deep_gate",()->new DeepGate(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        register("storm_repeater",()->new ResonanceWeapon(new Item.Properties().durability(2200).rarity(Rarity.EPIC),18,48,6,"storm_shard",6));
        register("dawn_projector",()->new ResonanceWeapon(new Item.Properties().durability(3600).rarity(Rarity.EPIC),40,64,24,"dawn_fragment",14));
        block("contract_terminal",()->new ContractTerminal(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
        block("expedition_beacon",()->new ExpeditionBeacon(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).lightLevel(s->10)));
    }
    private static FoodProperties food(int i) {
        var b=new FoodProperties.Builder().nutrition(i>=20?12:i>=8?8:5).saturationMod(i>=20?1f:.65f);
        if(i==9)b.effect(()->new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,600,0),1);
        if(i==10)b.effect(()->new MobEffectInstance(MobEffects.FIRE_RESISTANCE,600,0),1);
        if(i==11)b.effect(()->new MobEffectInstance(MobEffects.NIGHT_VISION,1200,0),1);
        if(i==12)b.effect(()->new MobEffectInstance(MobEffects.REGENERATION,160,0),1);
        if(i==22)b.effect(()->new MobEffectInstance(MobEffects.ABSORPTION,1200,1),1);
        if(i==23)b.effect(()->new MobEffectInstance(MobEffects.REGENERATION,240,1),1);
        return b.build();
    }
    static void register(String id,java.util.function.Supplier<Item> factory) { ExpansionContent.ITEMS.put(id,ModItems.ITEMS.register(id,factory)); }
    static void ordinary(String id) { register(id,()->new Item(new Item.Properties().rarity(id.endsWith("core")?Rarity.EPIC:Rarity.UNCOMMON))); }
    static void block(String id,java.util.function.Supplier<Block> factory) {
        var b=ModBlocks.BLOCKS.register(id,factory);ExpansionContent.BLOCKS.put(id,b);register(id,()->new BlockItem(b.get(),new Item.Properties()));
    }
}
