package dev.yuni.ashenprotocol.expansion;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
public final class ResonanceWeapon extends Item {
    private final int damage,range,cooldown,debt; private final String ammo;
    public ResonanceWeapon(Properties p,int damage,int range,int cooldown,String ammo,int debt) { super(p);this.damage=damage;this.range=range;this.cooldown=cooldown;this.ammo=ammo;this.debt=debt; }
    @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
        var weapon=player.getItemInHand(hand); if(l.isClientSide) return InteractionResultHolder.success(weapon);
        if(!(player instanceof ServerPlayer p)||p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(weapon);
        ItemStack supply=ItemStack.EMPTY; for(ItemStack s:p.getInventory().items) if(s.is(ExpansionContent.item(ammo))) { supply=s;break; }
        if(supply.isEmpty()&&!p.isCreative()) { p.displayClientMessage(Component.literal("缺少弹药："+new ItemStack(ExpansionContent.item(ammo)).getHoverName().getString()),true); return InteractionResultHolder.fail(weapon); }
        var start=p.getEyePosition(); var end=start.add(p.getLookAngle().scale(range));
        var block=l.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)); if(block.getType()!=HitResult.Type.MISS) end=block.getLocation();
        LivingEntity closest=null; double distance=Double.MAX_VALUE;
        for(var target:l.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(range)).inflate(1),e->e!=p&&e.isAlive()&&!e.isSpectator())) {
            var hit=target.getBoundingBox().inflate(.2).clip(start,end); if(hit.isPresent()&&start.distanceToSqr(hit.get())<distance) { closest=target;distance=start.distanceToSqr(hit.get()); }
        }
        if(closest!=null) { closest.hurt(p.damageSources().playerAttack(p),damage); end=closest.getBoundingBox().getCenter(); }
        if(!p.isCreative()) supply.shrink(1);
        var sl=(ServerLevel)l; var ray=end.subtract(start);
        for(int i=1;i<=24;i++) { var point=start.add(ray.scale(i/24d)); sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,point.x,point.y,point.z,1,0,0,0,0); }
        p.getCooldowns().addCooldown(this,cooldown); weapon.hurtAndBreak(1,p,who->who.broadcastBreakEvent(hand)); ProtocolSavedData.get(p.getServer()).addEntropy(debt);
        return InteractionResultHolder.consume(weapon);
    }
}
