package dev.yuni.ashenprotocol.campaign;
import dev.yuni.ashenprotocol.expansion.*;
import dev.yuni.ashenprotocol.progress.Progression;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import java.util.UUID;
public final class ExpeditionArena extends BlockEntity {
    private boolean active;private int wave,delay;private UUID owner;
    public ExpeditionArena(BlockPos p,BlockState s){super(ExpansionContent.ARENA.get(),p,s);}
    private String id(){return level.dimension().location()+":"+worldPosition.asLong();}
    public boolean active(){return active;} public int wave(){return wave;}
    public void start(Player p,InteractionHand hand){
        if(active){p.displayClientMessage(Component.literal("远征第 "+wave+" / 6 波。潜行空手右键取消。"),true);if(p.isShiftKeyDown()&&p.getItemInHand(hand).isEmpty()&&p.getUUID().equals(owner))cancel();return;}
        if(level.getDifficulty()==Difficulty.PEACEFUL){p.displayClientMessage(Component.literal("和平难度无法启动远征。"),true);return;}
        if(!p.getItemInHand(hand).is(ExpansionContent.item("expedition_seal"))){p.displayClientMessage(Component.literal("手持远征印记开启六波挑战；建议先整理空旷场地，组队准备补给。"),true);return;}
        active=true;wave=0;delay=40;owner=p.getUUID();if(!p.isCreative())p.getItemInHand(hand).shrink(1);setChanged();
    }
    public void cancel(){
        if(level instanceof ServerLevel sl)sl.getEntitiesOfClass(ProtocolMob.class,new AABB(worldPosition).inflate(96),m->m.getPersistentData().getString("AshenArena").equals(id())).forEach(net.minecraft.world.entity.Entity::discard);
        active=false;wave=0;setChanged();
    }
    public static void tick(Level l,BlockPos pos,BlockState s,ExpeditionArena be){
        if(!(l instanceof ServerLevel sl)||!be.active)return;
        if(l.getDifficulty()==Difficulty.PEACEFUL){be.cancel();return;}
        ServerPlayer p=sl.getServer().getPlayerList().getPlayer(be.owner);
        if(p==null||!p.isAlive()||p.level()!=l||p.distanceToSqr(pos.getX(),pos.getY(),pos.getZ())>48*48){be.cancel();return;}
        if(!sl.getEntitiesOfClass(ProtocolMob.class,new AABB(pos).inflate(96),m->m.isAlive()&&m.getPersistentData().getString("AshenArena").equals(be.id())).isEmpty())return;
        if(be.delay-->0)return;
        if(be.wave>=6){
            p.getInventory().placeItemBackInInventory(new ItemStack(ExpansionContent.item("contract_token"),16));p.getInventory().placeItemBackInInventory(new ItemStack(ExpansionContent.item("expedition_seal")));
            p.getInventory().placeItemBackInInventory(new ItemStack(ExpansionContent.item("precision_gear"),3));Progression.addCounter(p,"AshenArenaWins",1);Progression.complete(p,78);p.giveExperiencePoints(150);
            p.sendSystemMessage(Component.literal("[灰烬协议] 六波远征完成：获得16凭证、精密齿轮与返还印记。"));be.active=false;be.setChanged();return;
        }
        int count=be.wave==5?1:2+be.wave;int spawned=0;
        for(int n=0;n<count;n++){
            int variant=be.wave==5?4:new int[]{0,1,2,3,8,9,10,11}[(be.wave+n)%8];
            var mob=ExpansionContent.MOBS.get(ExpansionContent.ENEMIES[variant]).get().create(sl);
            for(int attempt=0;attempt<12;attempt++){
                double angle=(n+attempt*.2)*Math.PI*2/Math.max(1,count);
                BlockPos feet=pos.offset((int)(Math.cos(angle)*7),0,(int)(Math.sin(angle)*7));
                feet=sl.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,feet);
                if(Math.abs(feet.getY()-pos.getY())>12)continue;
                mob.moveTo(feet.getX()+.5,feet.getY(),feet.getZ()+.5,0,0);
                if(sl.getFluidState(feet.below()).isEmpty()&&sl.getBlockState(feet.below()).isSolid()&&sl.noCollision(mob)){
                    mob.getPersistentData().putString("AshenArena",be.id());mob.setPersistenceRequired();mob.setHome(pos);mob.setTarget(p);
                    if(sl.addFreshEntity(mob)){spawned++;break;}
                }
            }
        }
        if(spawned==0){be.delay=100;p.displayClientMessage(Component.literal("远征场地不足，清理附近地表空地后将自动重试。"),true);return;}
        be.wave++;be.delay=80;be.setChanged();p.sendSystemMessage(Component.literal("[灰烬协议] 远征第 "+be.wave+" / 6 波："+spawned+" 名敌人。"));
    }
    @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);t.putBoolean("Active",active);t.putInt("Wave",wave);t.putInt("Delay",delay);if(owner!=null)t.putUUID("Owner",owner);}
    @Override public void load(CompoundTag t){super.load(t);owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;active=t.getBoolean("Active")&&owner!=null;wave=Math.max(0,Math.min(6,t.getInt("Wave")));delay=Math.max(0,t.getInt("Delay"));}
}
