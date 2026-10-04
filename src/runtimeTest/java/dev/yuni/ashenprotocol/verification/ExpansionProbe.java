package dev.yuni.ashenprotocol.verification;
import dev.yuni.ashenprotocol.expansion.*;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.progress.Progression;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import java.util.UUID;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=dev.yuni.ashenprotocol.AshenProtocol.MOD_ID)
public final class ExpansionProbe {
    static ProtocolMob lastSpawn;
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void spawn(net.minecraftforge.event.entity.EntityJoinLevelEvent e) {
        if(Boolean.getBoolean("ashenprotocol.runtimeProbe") && e.getEntity() instanceof ProtocolMob mob) lastSpawn=mob;
    }
    static void check(boolean b,String s) { RuntimeProbe.check(b,"expansion: "+s); }
    public static void run(net.minecraft.server.MinecraftServer server) {
        var level=server.overworld(); var data=ProtocolSavedData.get(server);
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("00000000-aaaa-bbbb-cccc-444444444444"),"ExpeditionProbe"));
        p.setPos(5,231,5); p.setHealth(20); p.getInventory().clearContent();
        check(ExpansionContent.ITEMS.size()>=230,"large campaign item registry");
        check(server.getLevel(CoordinateGate.REALM)!=null,"original lost-coordinates dimension registered");
        for(String site:ArchiveCompass.SITES) {
            check(server.registryAccess().registryOrThrow(Registries.STRUCTURE).containsKey(new ResourceLocation("ashenprotocol",site)),"structure registry "+site);
            var template=level.getStructureManager().getOrCreate(new ResourceLocation("ashenprotocol",site));
            check(template.getSize().getX()>=19&&template.getSize().getY()>=10,"structure template loads "+site);
        }
        for(String ore:new String[]{"ash_ore","signal_ore","rift_ore","memory_ore","void_ore"})
            check(server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).containsKey(new ResourceLocation("ashenprotocol",ore)),"ore placement registry "+ore);
        for(int x=0;x<22;x++)for(int z=0;z<22;z++) { level.setBlockAndUpdate(new BlockPos(x,230,z),Blocks.STONE.defaultBlockState()); for(int y=231;y<235;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState()); }
        check(server.getLevel(dev.yuni.ashenprotocol.campaign.DeepGate.REALM)!=null,"echo-depths dimension registered");
        int ordinal=0;
        for(String id:ExpansionContent.MACHINES) {
            var pos=new BlockPos(3+ordinal*2,231,3);var state=ExpansionContent.BLOCKS.get(id).get().defaultBlockState(); level.setBlockAndUpdate(pos.south(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,state);
            var be=(WorkshopEntity)level.getBlockEntity(pos); var recipe=WorkshopEntity.recipe(id);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("protocol_fragment"),21));
            for(int n=0;n<21;n++)be.interact(p,InteractionHand.MAIN_HAND);
            check(be.fuel()==4000&&p.getMainHandItem().getCount()==1,id+" full fuel does not consume extra fragment");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item(recipe.input()),2));be.interact(p,InteractionHand.MAIN_HAND);
            for(int n=0;n<recipe.ticks();n++)WorkshopEntity.tick(level,pos,state,be);
            check(be.input().getCount()==2&&be.progress()==0,id+" unpowered pauses production");
            if(recipe.secondaryCount()>0){p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item(recipe.secondary()),recipe.secondaryCount()*2));be.interact(p,InteractionHand.MAIN_HAND);}
            level.setBlockAndUpdate(pos.south(),Blocks.REDSTONE_BLOCK.defaultBlockState());data.setIntegrity(0);data.setEntropy(1800);
            for(int n=0;n<recipe.ticks();n++)WorkshopEntity.tick(level,pos,state,be);
            check(be.input().getCount()==1&&be.fuel()==4000-recipe.ticks()/20,id+" consumes one input and exact energy");
            if(recipe.count()>0)check(be.output().is(ExpansionContent.item(recipe.output()))&&be.output().getCount()==recipe.count(),id+" produces expected output");
            else check(data.getIntegrity()==(id.equals("world_anchor")?500:id.equals("biosphere_restorer")?200:40)&&data.getEntropy()==(id.equals("world_anchor")?800:id.equals("biosphere_restorer")?1500:1740),id+" changes real world state");
            var copy=new WorkshopEntity(pos,state);copy.load(be.saveWithoutMetadata());
            check(copy.input().getCount()==1&&copy.output().getCount()==be.output().getCount()&&copy.fuel()==be.fuel(),id+" inventory and energy persist");
            if(recipe.count()>0) {
                int count=be.output().getCount();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);be.interact(p,InteractionHand.MAIN_HAND);
                check(be.output().isEmpty()&&p.getInventory().contains(new ItemStack(ExpansionContent.item(recipe.output()))),id+" empty hand retrieves output");
            }
            ordinal++;
        }
        var pos=new BlockPos(4,231,15);var state=ExpansionContent.BLOCKS.get("ash_press").get().defaultBlockState();level.setBlockAndUpdate(pos,state);level.setBlockAndUpdate(pos.south(),Blocks.REDSTONE_BLOCK.defaultBlockState());
        var be=(WorkshopEntity)level.getBlockEntity(pos);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("protocol_fragment")));be.interact(p,InteractionHand.MAIN_HAND);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("raw_ash"),64));be.interact(p,InteractionHand.MAIN_HAND);
        for(int n=0;n<80*64;n++)WorkshopEntity.tick(level,pos,state,be);
        // 200 energy is only sufficient for 50 units; charge again and finish.
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("protocol_fragment")));be.interact(p,InteractionHand.MAIN_HAND);
        for(int n=0;n<80*14;n++)WorkshopEntity.tick(level,pos,state,be);
        check(be.output().getCount()==64&&be.input().isEmpty(),"processing stops with 64 outputs and no input");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item("raw_ash")));be.interact(p,InteractionHand.MAIN_HAND);int fuel=be.fuel();
        for(int n=0;n<100;n++)WorkshopEntity.tick(level,pos,state,be);
        check(be.input().getCount()==1&&be.fuel()==fuel,"full output preserves input and energy");
        int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(3)).size();
        level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        check(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(3)).size()>=before+2,"breaking machine drops both inventories");
        for(int i=0;i<16;i++) {
            var mob=ExpansionContent.MOBS.get(ExpansionContent.ENEMIES[i]).get().create(level);
            check(mob!=null&&mob.getMaxHealth()==new int[]{28,36,44,48,160,220,300,420,64,72,84,96,520,620,720,900}[i],"entity attributes "+ExpansionContent.ENEMIES[i]);
        }
        for(int tier=0;tier<8;tier++) {
            var shrineId=new String[]{"cinder_shrine","tide_shrine","rift_shrine","atlas_shrine","abyss_shrine","storm_shrine","root_shrine","dawn_shrine"}[tier];var key=ExpansionContent.KEYS[tier];
            var shrine=ExpansionContent.BLOCKS.get(shrineId).get(); var at=new BlockPos(10,231,10);level.setBlockAndUpdate(at,shrine.defaultBlockState());
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item(key),2));
            shrine.use(shrine.defaultBlockState(),level,at,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
            var bosses=level.getEntitiesOfClass(ProtocolMob.class,new AABB(at).inflate(64),ProtocolMob::isBoss);
            check(bosses.size()==1&&p.getMainHandItem().getCount()==1,"shrine summons one boss and consumes one key "+tier);
            shrine.use(shrine.defaultBlockState(),level,at,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
            check(p.getMainHandItem().getCount()==1,"active boss prevents duplicate summon "+tier);
            var boss=bosses.get(0);boss.hurt(p.damageSources().playerAttack(p),10000);
            check(Progression.completed(p,new int[]{12,15,19,22,61,66,71,75}[tier]),"boss kill advances correct milestone "+tier);
            String core=new String[]{"cinder_core","tide_core","rift_core","atlas_core","abyss_core","storm_core","root_core","dawn_core"}[tier];
            check(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,boss.getBoundingBox().inflate(3),e->e.getItem().is(ExpansionContent.item(core))).size()>0,"boss drops real core "+tier);
            boss.discard();
        }
        check(ExpansionContent.item("ash_pickaxe").isCorrectToolForDrops(ExpansionContent.BLOCKS.get("ash_ore").get().defaultBlockState()),"ash pickaxe harvests first tier ore");
        check(!ExpansionContent.item("ash_pickaxe").isCorrectToolForDrops(ExpansionContent.BLOCKS.get("rift_ore").get().defaultBlockState()),"ash pickaxe cannot skip diamond tier");
        check(ExpansionContent.item("rift_pickaxe").isCorrectToolForDrops(ExpansionContent.BLOCKS.get("rift_ore").get().defaultBlockState()),"rift pickaxe harvests rift ore");
        String[] guns={"resonance_carbine","rift_lance","atlas_caster"};String[] ammo={"signal_shard","rift_dust","void_shard"};
        for(int i=0;i<guns.length;i++) {
            p.getInventory().clearContent();p.setPos(4.5,231,4.5);p.setYRot(0);p.setXRot(0);data.setEntropy(0);
            var gun=ExpansionContent.item(guns[i]);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(gun));p.getInventory().setItem(1,new ItemStack(ExpansionContent.item(ammo[i]),3));
            var target=ExpansionContent.MOBS.get("last_cartographer").get().create(level);target.setNoAi(true);target.moveTo(4.5,231,12.5,0,0);level.addFreshEntity(target);
            gun.use(level,p,InteractionHand.MAIN_HAND);
            check(target.getHealth()<target.getMaxHealth()&&p.getInventory().getItem(1).getCount()==2&&data.getEntropy()==new int[]{4,8,12}[i],"weapon damage/ammunition/entropy "+guns[i]);
            gun.use(level,p,InteractionHand.MAIN_HAND);check(p.getInventory().getItem(1).getCount()==2,"weapon server cooldown "+guns[i]);
            p.getCooldowns().removeCooldown(gun);level.setBlockAndUpdate(new BlockPos(4,232,8),Blocks.STONE.defaultBlockState());target.invulnerableTime=0;float hp=target.getHealth();
            gun.use(level,p,InteractionHand.MAIN_HAND);check(target.getHealth()==hp,"weapon wall collision "+guns[i]);
            level.setBlockAndUpdate(new BlockPos(4,232,8),Blocks.AIR.defaultBlockState());target.discard();
        }
        // Generation uses the actual structure registry and placement, not manual construction.
        var tag=net.minecraft.tags.TagKey.create(Registries.STRUCTURE,new ResourceLocation("ashenprotocol","ash_outpost"));
        var located=level.findNearestMapStructure(tag,BlockPos.ZERO,12,false);
        check(located!=null,"locate naturally placed ash outpost");
        if(located!=null) {
            var chunk=level.getChunkAt(located);
            check(chunk.getAllStarts().values().stream().anyMatch(start->!start.getPieces().isEmpty()),"natural structure has generated pieces");
        }
        for(int region=0;region<ArchiveCompass.SITES.length;region++) {
            var targetLevel=(region==4||region==16) ? server.getLevel(net.minecraft.world.level.Level.NETHER) : (region==5||region==17) ? server.getLevel(net.minecraft.world.level.Level.END) : region>=10&&region<=15 ? server.getLevel(dev.yuni.ashenprotocol.campaign.DeepGate.REALM) : level;
            var siteTag=net.minecraft.tags.TagKey.create(Registries.STRUCTURE,new ResourceLocation("ashenprotocol",ArchiveCompass.SITES[region]));
            var targetPos=targetLevel.findNearestMapStructure(siteTag,BlockPos.ZERO,12,false);
            check(targetPos!=null,"natural generation/locate "+ArchiveCompass.SITES[region]);
            var chunk=targetLevel.getChunkAt(targetPos);
            var structureType=server.registryAccess().registryOrThrow(Registries.STRUCTURE).get(new ResourceLocation("ashenprotocol",ArchiveCompass.SITES[region]));
            var start=chunk.getStartForStructure(structureType);
            check(start!=null && !start.getPieces().isEmpty(),"generated template pieces "+ArchiveCompass.SITES[region]);
            var box=start.getBoundingBox();int chests=0,shrines=0;
            for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++) {
                var block=new BlockPos(x,y,z);var bs=targetLevel.getBlockState(block);
                if(bs.is(Blocks.CHEST))chests++;
                if(bs.getBlock() instanceof ShrineBlock) {
                    shrines++;
                    var shrine=(ShrineBlock)bs.getBlock();
                    String summonKey=ExpansionContent.KEYS[shrine.tier];
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExpansionContent.item(summonKey)));
                    lastSpawn=null;
                    shrine.use(bs,targetLevel,block,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(block),Direction.UP,block,false));
                    var summoned=targetLevel.getEntitiesOfClass(ProtocolMob.class,new AABB(block).inflate(64),ProtocolMob::isBoss);
                    check(p.getMainHandItem().isEmpty() && lastSpawn!=null && lastSpawn.variant()==ExpansionContent.BOSS_VARIANTS[shrine.tier],"natural shrine has safe summon space "+ArchiveCompass.SITES[region]);
                    lastSpawn.discard();
                }
            }
            System.out.println("AP_SITE_COUNTS: "+ArchiveCompass.SITES[region]+" chests="+chests+" shrines="+shrines+" bounds="+box);
            check(chests==(region<6?2:3) && shrines==((region==0||region==1||region==4||region==5||region>=10&&region<=13)?1:0),"natural loot chests and shrine count "+ArchiveCompass.SITES[region]);
        }
        // High-index completion persists independently of the first 32 goals.
        Progression.complete(p,78);check(Progression.completed(p,78)&&!Progression.completed(p,46),"high milestone does not alias low milestone");
        long[] persistedBits=p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).getLongArray("AshenMilestoneBits");
        check(persistedBits.length==2&&(persistedBits[1]&(1L<<14))!=0,"high milestone saved in second word");
        int oreCount=0;
        for(int x=32;x<64;x++)for(int z=32;z<64;z++)for(int y=0;y<=72;y++)if(level.getBlockState(new BlockPos(x,y,z)).is(ExpansionContent.BLOCKS.get("ash_ore").get()))oreCount++;
        check(oreCount>0,"natural terrain contains custom ash ore: "+oreCount);
    }
}
