package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.ExpansionContent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.*;
public record CampaignArmor(int stage) implements ArmorMaterial {
    public int getDurabilityForType(ArmorItem.Type t){return switch(t){case HELMET->11;case CHESTPLATE->16;case LEGGINGS->15;case BOOTS->13;}*(30+stage*15);}
    public int getDefenseForType(ArmorItem.Type t){return switch(t){case HELMET,BOOTS->3;case CHESTPLATE->8;case LEGGINGS->6;};}
    public int getEnchantmentValue(){return 18+stage*3;}
    public SoundEvent getEquipSound(){return SoundEvents.ARMOR_EQUIP_NETHERITE;}
    public Ingredient getRepairIngredient(){return Ingredient.of(ExpansionContent.item(new String[]{"steel_ingot","deep_ingot","harmonic_ingot"}[stage]));}
    public String getName(){return "ashenprotocol:"+CampaignContent.EQUIPMENT[stage];}
    public float getToughness(){return 2+stage;}
    public float getKnockbackResistance(){return .05f+stage*.05f;}
}
