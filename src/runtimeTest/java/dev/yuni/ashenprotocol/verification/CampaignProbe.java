package dev.yuni.ashenprotocol.verification;
import dev.yuni.ashenprotocol.expansion.*;
import dev.yuni.ashenprotocol.campaign.*;
import dev.yuni.ashenprotocol.progress.Progression;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
public final class CampaignProbe {
 static void check(boolean b,String detail){RuntimeProbe.check(b,"campaign: "+detail);}
 public static void run(net.minecraft.server.MinecraftServer server){
  var l=server.overworld();var p=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("00000000-1234-1234-1234-123456781234"),"CampaignProbe"));p.setPos(5,231,5);p.getInventory().clearContent();
  check(dev.yuni.ashenprotocol.progress.QuestCatalog.recommend(i->false)==0,"new-player recommendation starts with fragments");
  for(int id=0;id<96;id++)for(int before:dev.yuni.ashenprotocol.progress.QuestCatalog.PREREQUISITES[id])check(before>=0&&before<96&&before!=id,"valid guidance dependency "+id+" -> "+before);
  check(!dev.yuni.ashenprotocol.progress.QuestTracking.select(p,96)&&!dev.yuni.ashenprotocol.progress.QuestTracking.select(p,-3),"invalid tracking IDs rejected");
  check(dev.yuni.ashenprotocol.progress.QuestTracking.select(p,80)&&dev.yuni.ashenprotocol.progress.QuestTracking.selected(p)==80,"selected quest stored for player");
  check(p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).getInt("AshenTrackedQuest")==80,"tracking persists in death-preserved NBT");
  dev.yuni.ashenprotocol.progress.QuestTracking.select(p,-1);check(dev.yuni.ashenprotocol.progress.QuestTracking.selected(p)==-1,"disabled tracking respected");
  for(String name:CampaignContent.CROPS){
   var crop=(CropBlock)ExpansionContent.BLOCKS.get(name+"_crop").get();var pos=new BlockPos(7,231,7);l.setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());l.setBlockAndUpdate(pos,crop.getStateForAge(0));
   check(!crop.isMaxAge(l.getBlockState(pos)),name+" begins immature");
   crop.performBonemeal(l,l.random,pos,l.getBlockState(pos));check(crop.getAge(l.getBlockState(pos))>0,name+" grows with bone meal");
   var grown=crop.getStateForAge(7);l.setBlockAndUpdate(pos,grown);var drops=Block.getDrops(grown,l,pos,null,p,ItemStack.EMPTY);
   check(drops.stream().anyMatch(s->s.is(ExpansionContent.item(name))),name+" ripe crop gives harvest");
   check(drops.stream().anyMatch(s->s.is(ExpansionContent.item(name+"_seeds"))),name+" ripe crop gives renewable seeds");
   var immature=Block.getDrops(crop.getStateForAge(0),l,pos,null,p,ItemStack.EMPTY);check(immature.stream().noneMatch(s->s.is(ExpansionContent.item(name))),name+" immature crop cannot give harvest");
   check(immature.stream().anyMatch(s->s.is(ExpansionContent.item(name+"_seeds"))),name+" immature crop returns seed");
  }
  var at=new BlockPos(8,231,10);var state=ExpansionContent.BLOCKS.get("circuit_assembler").get().defaultBlockState();l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(at.above(),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(at.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(at,state);l.setBlockAndUpdate(at.south(),Blocks.REDSTONE_BLOCK.defaultBlockState());var be=(WorkshopEntity)l.getBlockEntity(at);
  var top=be.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(()->new IllegalStateException("top capability"));var side=be.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.EAST).orElseThrow(()->new IllegalStateException("side capability"));var down=be.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.DOWN).orElseThrow(()->new IllegalStateException("bottom capability"));
  var signal=new ItemStack(ExpansionContent.item("signal_ingot"),2);check(top.insertItem(0,signal,true).isEmpty()&&be.input().isEmpty(),"simulated insert does not mutate input");
  top.insertItem(0,signal,false);check(be.input().getCount()==2,"top accepts primary input");
  check(top.insertItem(1,new ItemStack(ExpansionContent.item("copper_wire"),2),false).getCount()==2,"top refuses secondary input");
  side.insertItem(3,new ItemStack(ExpansionContent.item("protocol_fragment")),false);for(int i=0;i<200;i++)WorkshopEntity.tick(l,at,state,be);
  check(be.input().getCount()==2&&be.progress()==0&&be.fuel()==200,"missing second material pauses without burning fuel");
  side.insertItem(1,new ItemStack(ExpansionContent.item("copper_wire"),4),false);for(int i=0;i<160;i++)WorkshopEntity.tick(l,at,state,be);
  check(be.input().getCount()==1&&be.secondary().getCount()==2&&be.output().getCount()==1,"dual-input production consumes exact materials");
  check(down.extractItem(0,1,false).isEmpty()&&be.input().getCount()==1,"bottom cannot drain input");
  check(down.extractItem(2,1,true).getCount()==1&&be.output().getCount()==1,"simulated extraction does not mutate output");
  check(down.extractItem(2,1,false).is(ExpansionContent.item("signal_circuit"))&&be.output().isEmpty(),"bottom extracts finished product");
  var saved=be.saveWithoutMetadata();var restored=new WorkshopEntity(at,state);restored.load(saved);check(restored.secondary().getCount()==2&&restored.input().getCount()==1,"auxiliary inventory survives save/load");
  ItemStack exposed=top.getStackInSlot(0);exposed.setCount(60);check(be.input().getCount()==1,"handler stack views cannot mutate inventory");
  // Verify the actual vanilla hopper tick takes the Forge capability path.
  l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(at.above()).inflate(3)).forEach(net.minecraft.world.entity.Entity::discard);
  var hopperState=Blocks.HOPPER.defaultBlockState();l.setBlockAndUpdate(at.above(),hopperState);
  var upper=(net.minecraft.world.level.block.entity.HopperBlockEntity)l.getBlockEntity(at.above());upper.setItem(0,new ItemStack(ExpansionContent.item("signal_ingot"),4));
  net.minecraft.world.level.block.entity.HopperBlockEntity.pushItemsTick(l,at.above(),hopperState,upper);
  System.out.println("AP_HOPPER_STATE: enabled="+l.getBlockState(at.above()).getValue(HopperBlock.ENABLED)+" input="+be.input().getCount()+" hopper="+upper.getItem(0).getCount());
  check(be.input().getCount()==2&&upper.getItem(0).getCount()==3,"real hopper feeds machine top");
  for(int i=0;i<160;i++)WorkshopEntity.tick(l,at,state,be);
  l.setBlockAndUpdate(at.below(),hopperState);var lower=(net.minecraft.world.level.block.entity.HopperBlockEntity)l.getBlockEntity(at.below());
  net.minecraft.world.level.block.entity.HopperBlockEntity.pushItemsTick(l,at.below(),hopperState,lower);
  check(be.output().isEmpty()&&lower.getItem(0).is(ExpansionContent.item("signal_circuit")),"real hopper pulls machine output from bottom");
  p.setHealth(10);var bandage=ExpansionContent.item("field_bandage");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(bandage,3));bandage.use(l,p,InteractionHand.MAIN_HAND);check(p.getHealth()==14&&p.getMainHandItem().getCount()==2,"bandage heals and consumes one");bandage.use(l,p,InteractionHand.MAIN_HAND);check(p.getMainHandItem().getCount()==2,"bandage enforces cooldown");p.getCooldowns().removeCooldown(bandage);p.setHealth(20);bandage.use(l,p,InteractionHand.MAIN_HAND);check(p.getMainHandItem().getCount()==2,"full health preserves bandage");
  var purity=ExpansionContent.item("purity_capsule");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(purity,2));purity.use(l,p,InteractionHand.MAIN_HAND);check(p.getMainHandItem().getCount()==2,"no negative effects preserves capsule");p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,200));purity.use(l,p,InteractionHand.MAIN_HAND);check(!p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)&&p.getMainHandItem().getCount()==1,"capsule cures poison and consumes one");
  var terminal=(ContractTerminal)ExpansionContent.BLOCKS.get("contract_terminal").get();l.setBlockAndUpdate(at,terminal.defaultBlockState());var hit=new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false);p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("ash_ingot"),16));terminal.use(terminal.defaultBlockState(),l,at,p,InteractionHand.MAIN_HAND,hit);check(p.getMainHandItem().getCount()==8&&p.getInventory().contains(new ItemStack(ExpansionContent.item("contract_token"))),"contract consumes exact requested materials and gives tokens");check(Progression.completed(p,51),"actual contract advances journal");terminal.use(terminal.defaultBlockState(),l,at,p,InteractionHand.MAIN_HAND,hit);check(p.getMainHandItem().getCount()==8,"contract cooldown prevents duplicate reward");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("contract_token"),2));terminal.use(terminal.defaultBlockState(),l,at,p,InteractionHand.MAIN_HAND,hit);check(!p.getMainHandItem().is(ExpansionContent.item("contract_token"))&&p.getInventory().contains(new ItemStack(ExpansionContent.item("ash_wheat_seeds"))),"contract seeds exchange consumes two tokens");
  Progression.complete(p,95);check(Progression.completed(p,95)&&!Progression.completed(p,31),"96-goal highest bit does not alias bit31");
  for(String food:CampaignContent.FOODS)check(ExpansionContent.item(food).isEdible()&&ExpansionContent.item(food).getFoodProperties().getNutrition()>=5,"food gives real nutrition "+food);
  var persisted=p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);persisted.remove("AshenMilestoneBits");persisted.putInt("AshenMilestones",1<<22);p.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,persisted);check(Progression.completed(p,22)&&!Progression.completed(p,54),"old 24-goal save migrates without aliased goals");Progression.complete(p,75);check(Progression.completed(p,22)&&Progression.completed(p,75),"new late-game goal preserves migrated progress");
 }
}
