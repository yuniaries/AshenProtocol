package dev.yuni.ashenprotocol.campaign;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
public final class SupplyItem extends Item {
    private final int kind;
    public SupplyItem(Properties p,int kind){super(p);this.kind=kind;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player user,InteractionHand hand){
        var s=user.getItemInHand(hand);if(l.isClientSide)return InteractionResultHolder.success(s);
        if(!(user instanceof ServerPlayer p)||p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);
        switch(kind){
            case 0->{if(p.getHealth()>=p.getMaxHealth())return InteractionResultHolder.fail(s);p.heal(4);}
            case 1->{if(p.getHealth()>=p.getMaxHealth())return InteractionResultHolder.fail(s);p.heal(8);p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));}
            case 2->{if(!p.hasEffect(MobEffects.POISON)&&!p.hasEffect(MobEffects.WEAKNESS)&&!p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))return InteractionResultHolder.fail(s);p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WEAKNESS);p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);}
            case 3->p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,1200,0));
            case 4->p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,600,0));
            case 5->p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,2400,0));
        }
        if(kind<2)dev.yuni.ashenprotocol.progress.Progression.addCounter(p,"AshenMedicalUses",1);
        if(!p.isCreative())s.shrink(1);p.getCooldowns().addCooldown(this,kind<2?100:200);
        return InteractionResultHolder.consume(s);
    }
}
