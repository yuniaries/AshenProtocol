package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.ExpansionContent;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
public record CampaignTier(int stage) implements Tier {
    public int getUses(){return 1000+stage*1000;}
    public float getSpeed(){return 8+stage*2;}
    public float getAttackDamageBonus(){return 3+stage*2;}
    public int getLevel(){return 3;}
    public int getEnchantmentValue(){return 18+stage*3;}
    public Ingredient getRepairIngredient(){return Ingredient.of(ExpansionContent.item(new String[]{"steel_ingot","deep_ingot","harmonic_ingot"}[stage]));}
}
