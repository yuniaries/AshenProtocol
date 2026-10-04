package dev.yuni.ashenprotocol.expansion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

public final class ProtocolMob extends Monster {
    private final int variant;
    private BlockPos home;
    private int skillTicks;
    private final ServerBossEvent bar;
    public ProtocolMob(EntityType<? extends Monster> type, Level level, int variant) {
        super(type,level); this.variant=variant; xpReward=ExpansionContent.bossVariant(variant) ? 80+variant*20 : 12+variant;
        bar=new ServerBossEvent(getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    }
    public int variant() { return variant; }
    public boolean isBoss() { return ExpansionContent.bossVariant(variant); }
    public void setHome(BlockPos p) { home=p.immutable(); }
    public static AttributeSupplier.Builder attributes(int i) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,new double[]{28,36,44,48,160,220,300,420,64,72,84,96,520,620,720,900}[i]).add(Attributes.MOVEMENT_SPEED,ExpansionContent.bossVariant(i) ? .25 : .28)
            .add(Attributes.ATTACK_DAMAGE,new double[]{4,5,6,7,7,8,10,12,8,9,10,11,13,14,15,17}[i]).add(Attributes.FOLLOW_RANGE,40).add(Attributes.ARMOR,ExpansionContent.bossVariant(i) ? 6 : 2).add(Attributes.KNOCKBACK_RESISTANCE,ExpansionContent.bossVariant(i) ? .65 : .1);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this)); goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this, .8)); goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));
        targetSelector.addGoal(1,new HurtByTargetGoal(this)); targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public boolean removeWhenFarAway(double distance) { return !isBoss(); }
    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); if(isBoss()) bar.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
    @Override public void remove(RemovalReason reason) { super.remove(reason); bar.removeAllPlayers(); }
    @Override public void addAdditionalSaveData(CompoundTag t) { super.addAdditionalSaveData(t); if(home!=null) t.putLong("ProtocolHome",home.asLong()); t.putInt("SkillTicks",skillTicks); }
    @Override public void readAdditionalSaveData(CompoundTag t) { super.readAdditionalSaveData(t); if(t.contains("ProtocolHome")) home=BlockPos.of(t.getLong("ProtocolHome")); skillTicks=Math.max(0,t.getInt("SkillTicks")); }
    @Override public void tick() {
        super.tick(); if(!(level() instanceof ServerLevel sl)||!isAlive()) return;
        bar.setProgress(getHealth()/getMaxHealth());
        boolean empowered=getHealth()<getMaxHealth()*.5;
        if(isBoss()) bar.setName(Component.literal(getDisplayName().getString()+(empowered ? " · 协议过载" : "")));
        if(home!=null && blockPosition().distSqr(home)>96*96 && tickCount%100==0) { teleportTo(home.getX()+3.5,home.getY()+1,home.getZ()+.5); getNavigation().stop(); }
        LivingEntity target=getTarget(); if(target==null||!target.isAlive()) { skillTicks=0; return; }
        int cycle=isBoss() ? (empowered ? 100 : 150) : 180;
        skillTicks++;
        if(skillTicks%cycle==cycle-20 && isBoss()) {
            sl.sendParticles(ParticleTypes.END_ROD,getX(),getY()+1,getZ(),24,1,.4,1,.03);
            if(target instanceof ServerPlayer p) p.displayClientMessage(Component.literal("守卫正在蓄力：远离它并寻找掩体！"),true);
        }
        if(skillTicks%cycle!=0 || !hasLineOfSight(target) || distanceToSqr(target)>24*24) return;
        if(isBoss()) {
            switch(variant) {
                case 4 -> { // a close ring, telegraphed before damage; no block destruction
                    for(Player p:sl.getEntitiesOfClass(Player.class,getBoundingBox().inflate(5), p -> !p.isCreative() && !p.isSpectator() && hasLineOfSight(p))) {
                        p.hurt(damageSources().mobAttack(this), empowered ? 8 : 5); p.setSecondsOnFire(3);
                    }
                    sl.sendParticles(ParticleTypes.FLAME,getX(),getY()+.2,getZ(),70,3,.3,3,.05);
                }
                case 5 -> { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1)); target.hurt(damageSources().mobAttack(this),6); sl.sendParticles(ParticleTypes.SPLASH,target.getX(),target.getY()+1,target.getZ(),30,.4,.6,.4,.1); }
                case 6 -> {
                    var point=target.position().add(target.getLookAngle().scale(-3));
                    double oldX=getX(),oldY=getY(),oldZ=getZ(); setPos(point.x,point.y,point.z);
                    if(!sl.noCollision(this)||!sl.getBlockState(blockPosition().below()).isSolid()) setPos(oldX,oldY,oldZ);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0));
                    sl.sendParticles(ParticleTypes.PORTAL,getX(),getY()+1,getZ(),35,.5,1,.5,.2);
                }
                case 12 -> {
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,100,0));
                    if(distanceToSqr(target)<100)target.hurt(damageSources().mobAttack(this),empowered?12:8);
                    sl.sendParticles(ParticleTypes.SOUL,getX(),getY()+1,getZ(),60,4,1,4,.04);
                }
                case 13 -> {
                    // The pulse requires sight and leaves terrain and nearby buildings intact.
                    target.hurt(damageSources().mobAttack(this),empowered?10:7);
                    var away=target.position().subtract(position()).normalize();target.push(away.x*.8,.25,away.z*.8);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getY()+1,target.getZ(),50,.7,1,.7,.1);
                }
                case 14 -> {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1));
                    if(distanceToSqr(target)<64)target.hurt(damageSources().mobAttack(this),9);
                    heal(empowered?10:6);
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,getX(),getY()+1,getZ(),40,2,1,2,.1);
                }
                case 15 -> {
                    if(distanceToSqr(target)<144){target.hurt(damageSources().mobAttack(this),empowered?14:10);target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0));}
                    sl.sendParticles(ParticleTypes.END_ROD,getX(),getY()+1,getZ(),80,5,1,5,.1);
                    if(sl.getEntitiesOfClass(ProtocolMob.class,getBoundingBox().inflate(32),m->!m.isBoss()).size()<3){
                        var guard=ExpansionContent.MOBS.get("dawn_raider").get().create(sl);guard.moveTo(getX()+3,getY(),getZ()+1,0,0);
                        if(sl.noCollision(guard)&&sl.getBlockState(guard.blockPosition().below()).isSolid()){guard.setTarget(target);sl.addFreshEntity(guard);}
                    }
                }
                case 7 -> { target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,50,0)); target.hurt(damageSources().mobAttack(this),7);
                    if(sl.getEntitiesOfClass(ProtocolMob.class,getBoundingBox().inflate(32),m->!m.isBoss()).size()<4) {
                        var add=ExpansionContent.MOBS.get("rift_hound").get().create(sl); add.moveTo(getX()+2,getY(),getZ()+2,0,0);
                        if(sl.noCollision(add)) { add.setTarget(target); sl.addFreshEntity(add); }
                    }
                }
            }
        } else if(distanceToSqr(target)<8*8) {
            if(variant==1) target.addEffect(new MobEffectInstance(MobEffects.POISON,40,0));
            if(variant==2) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));
            if(variant==8) target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0));
            if(variant==9) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,0));
            if(variant==10) target.addEffect(new MobEffectInstance(MobEffects.POISON,40,0));
            if(variant==11) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));
            if(variant==3) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,0));
        }
    }
    @Override public void die(DamageSource source) {
        super.die(source); if(level().isClientSide||!isBoss()) return;
        if(source.getEntity() instanceof ServerPlayer p) {
            for(int i=0;i<ExpansionContent.BOSS_VARIANTS.length;i++)if(variant==ExpansionContent.BOSS_VARIANTS[i])dev.yuni.ashenprotocol.progress.Progression.markBoss(p,i);
            dev.yuni.ashenprotocol.progress.Progression.complete(p, variant<8?new int[]{12,15,19,22}[variant-4]:new int[]{61,66,71,75}[variant-12]);
            dev.yuni.ashenprotocol.protocol.ProtocolSavedData.get(p.getServer()).addIntegrity(100+(variant-4)*100);
        }
    }
}
