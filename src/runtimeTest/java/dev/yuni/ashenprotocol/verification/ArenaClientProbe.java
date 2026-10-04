package dev.yuni.ashenprotocol.verification;
import dev.yuni.ashenprotocol.campaign.*;
import dev.yuni.ashenprotocol.expansion.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import java.util.UUID;
public final class ArenaClientProbe {
 static BlockPos position;static ExpeditionArena arena;
 static void check(boolean success,String detail){if(!success)throw new IllegalStateException("AP_CLIENT_ARENA_FAIL: "+detail);System.out.println("AP_CLIENT_ARENA_PASS: "+detail);}
 public static void prepare(net.minecraft.server.MinecraftServer server,UUID owner){
  var p=server.getPlayerList().getPlayer(owner);var l=p.serverLevel();position=new BlockPos(p.getBlockX(),231,p.getBlockZ());
  for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){var floor=position.offset(x,-1,z);l.setBlockAndUpdate(floor,Blocks.STONE.defaultBlockState());for(int y=0;y<=4;y++)l.setBlockAndUpdate(floor.above(y+1),Blocks.AIR.defaultBlockState());}
  l.getEntitiesOfClass(ProtocolMob.class,new AABB(position).inflate(96)).forEach(net.minecraft.world.entity.Entity::discard);
  p.teleportTo(l,position.getX()+.5,231,position.getZ()+.5,0,0);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.setHealth(20);
  l.setBlockAndUpdate(position,ExpansionContent.BLOCKS.get("expedition_beacon").get().defaultBlockState());arena=(ExpeditionArena)l.getBlockEntity(position);
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("expedition_seal")));arena.start(p,InteractionHand.MAIN_HAND);check(arena.active()&&p.getMainHandItem().isEmpty(),"starting consumes one seal");
 }
 public static void verify(net.minecraft.server.MinecraftServer server,UUID owner){
  var p=server.getPlayerList().getPlayer(owner);var l=p.serverLevel();int killed=0;
  for(int wave=0;wave<6;wave++){
   for(int tick=0;tick<90;tick++)ExpeditionArena.tick(l,position,l.getBlockState(position),arena);
   var enemies=l.getEntitiesOfClass(ProtocolMob.class,new AABB(position).inflate(96),m->m.isAlive()&&!m.getPersistentData().getString("AshenArena").isEmpty());check(!enemies.isEmpty(),"wave "+(wave+1)+" has real enemies");
   int current=arena.wave();for(int tick=0;tick<100;tick++)ExpeditionArena.tick(l,position,l.getBlockState(position),arena);check(arena.wave()==current,"wave waits for living enemies");
   for(var mob:enemies){mob.hurt(p.damageSources().playerAttack(p),10000);mob.discard();killed++;}
  }
  for(int tick=0;tick<100;tick++)ExpeditionArena.tick(l,position,l.getBlockState(position),arena);
  check(!arena.active()&&dev.yuni.ashenprotocol.progress.Progression.completed(p,78),"six cleared waves award completed goal");
  check(p.getInventory().contains(new ItemStack(ExpansionContent.item("expedition_seal")))&&p.getInventory().contains(new ItemStack(ExpansionContent.item("precision_gear"))),"victory returns seal and actual rewards");check(killed>=21,"six waves contain expected enemy count");
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("expedition_seal")));arena.start(p,InteractionHand.MAIN_HAND);p.teleportTo(l,position.getX()+60,231,position.getZ(),0,0);ExpeditionArena.tick(l,position,l.getBlockState(position),arena);check(!arena.active(),"leaving area cancels challenge");
  p.teleportTo(l,position.getX()+2.5,231,position.getZ()+.5,0,0);
  var machinePos=position.offset(4,0,4);var machineState=ExpansionContent.BLOCKS.get("ash_press").get().defaultBlockState();l.setBlockAndUpdate(machinePos,machineState);l.setBlockAndUpdate(machinePos.south(),Blocks.REDSTONE_BLOCK.defaultBlockState());var machine=(WorkshopEntity)l.getBlockEntity(machinePos);machine.setOwner(p);
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("protocol_fragment"),3));for(int i=0;i<3;i++)machine.interact(p,InteractionHand.MAIN_HAND);
  var top=machine.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,net.minecraft.core.Direction.UP).orElseThrow(()->new IllegalStateException("top capability"));var bottom=machine.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,net.minecraft.core.Direction.DOWN).orElseThrow(()->new IllegalStateException("bottom capability"));
  top.insertItem(0,new ItemStack(ExpansionContent.item("raw_ash"),64),false);for(int tick=0;tick<80*64;tick++)WorkshopEntity.tick(l,machinePos,machineState,machine);bottom.extractItem(2,64,false);
  top.insertItem(0,new ItemStack(ExpansionContent.item("raw_ash"),36),false);for(int tick=0;tick<80*36;tick++)WorkshopEntity.tick(l,machinePos,machineState,machine);bottom.extractItem(2,36,false);
  check(dev.yuni.ashenprotocol.progress.Progression.counter(p,"AshenJobs")>=100&&dev.yuni.ashenprotocol.progress.Progression.counter(p,"AshenAutoOutputs")>=100,"real production and extraction update owner mastery counters");
  for(int n=0;n<32;n++){String name=CampaignContent.CROPS[n%8];var crop=(net.minecraft.world.level.block.CropBlock)ExpansionContent.BLOCKS.get(name+"_crop").get();var at=position.offset(3,0,-3);l.setBlockAndUpdate(at.below(),Blocks.FARMLAND.defaultBlockState());l.setBlockAndUpdate(at,crop.getStateForAge(7));check(p.gameMode.destroyBlock(at),"actual mature crop break "+n);}
  check(dev.yuni.ashenprotocol.progress.Progression.counter(p,"AshenHarvests")>=32,"real crop break events update harvest statistics");
  var bandage=ExpansionContent.item("field_bandage");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(bandage,10));for(int i=0;i<10;i++){p.setHealth(10);p.getCooldowns().removeCooldown(bandage);bandage.use(l,p,InteractionHand.MAIN_HAND);}p.setHealth(20);
  var data=dev.yuni.ashenprotocol.protocol.ProtocolSavedData.get(server);data.setIntegrity(10000);data.setEntropy(0);int originalTicks=p.tickCount;p.tickCount=20;dev.yuni.ashenprotocol.progress.Progression.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));p.tickCount=originalTicks;
  check(dev.yuni.ashenprotocol.progress.Progression.completed(p,80)&&dev.yuni.ashenprotocol.progress.Progression.completed(p,81)&&dev.yuni.ashenprotocol.progress.Progression.completed(p,82)&&dev.yuni.ashenprotocol.progress.Progression.completed(p,84)&&dev.yuni.ashenprotocol.progress.Progression.completed(p,94),"measured production farming and healing unlock mastery goals");
  check(dev.yuni.ashenprotocol.progress.Progression.completed(p,79)&&dev.yuni.ashenprotocol.progress.Progression.completed(p,95),"final campaign and highest mastery goal use real world conditions");p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
 }
}
