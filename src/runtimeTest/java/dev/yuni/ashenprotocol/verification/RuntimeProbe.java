package dev.yuni.ashenprotocol.verification;

import com.mojang.authlib.GameProfile;
import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import dev.yuni.ashenprotocol.registry.ModBlocks;
import dev.yuni.ashenprotocol.block.ProtocolRelayBlock;
import dev.yuni.ashenprotocol.blockentity.ProtocolRelayBlockEntity;
import dev.yuni.ashenprotocol.event.ProtocolEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID)
public final class RuntimeProbe {
    static int checks = 0;
    static void check(boolean success, String description) {
        if (!success) throw new IllegalStateException("AP_PROBE_FAIL: " + description);
        checks++;
        System.out.println("AP_PROBE_PASS: " + description);
    }
    @SubscribeEvent
    public static void start(ServerStartedEvent event) {
        if (!Boolean.getBoolean("ashenprotocol.runtimeProbe")) return;
        var server = event.getServer();
        server.execute(() -> {
            try {
                var level = server.overworld();
                var data = ProtocolSavedData.get(server);
                for (String recipe : new String[]{"protocol_fragment","entropy_crystal","entropy_meter","protocol_relay","phase_pistol","field_guide","entropy_sink","echo_recycling"}) {
                    check(server.getRecipeManager().byKey(new ResourceLocation("ashenprotocol",recipe)).isPresent(), "recipe " + recipe);
                }
                check(ModItems.FIELD_GUIDE.get() != null, "field guide registry");
                var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-1111-2222-3333-444444444444"), "ProtocolProbe"));
                player.setPos(0.5,101,0.5);player.setYRot(0);player.setXRot(0);player.setHealth(20);
                player.getInventory().clearContent();
                var pistol = new ItemStack(ModItems.PHASE_PISTOL.get());
                player.setItemInHand(InteractionHand.MAIN_HAND,pistol);
                player.getInventory().setItem(1,new ItemStack(ModItems.PROTOCOL_FRAGMENT.get(),10));
                Zombie target = EntityType.ZOMBIE.create(level);target.setNoAi(true);target.setPos(0.5,101,8.5);level.addFreshEntity(target);
                data.setEntropy(0);
                var weapon = ModItems.PHASE_PISTOL.get();
                weapon.use(level,player,InteractionHand.MAIN_HAND);
                check(target.getHealth() < 20, "phase pistol damages target");
                check(player.getInventory().getItem(1).getCount()==9, "phase pistol consumes exactly one fragment");
                check(pistol.getDamageValue()==1 && data.getEntropy()==2, "pistol durability and entropy");
                weapon.use(level,player,InteractionHand.MAIN_HAND);
                check(player.getInventory().getItem(1).getCount()==9 && data.getEntropy()==2, "server enforces shot cooldown");
                player.getCooldowns().removeCooldown(weapon);
                target.invulnerableTime=0;float health=target.getHealth();
                level.setBlockAndUpdate(new BlockPos(0,102,4),Blocks.STONE.defaultBlockState());
                weapon.use(level,player,InteractionHand.MAIN_HAND);
                check(target.getHealth()==health, "solid wall blocks phase pistol");
                level.setBlockAndUpdate(new BlockPos(0,102,4),Blocks.AIR.defaultBlockState());
                player.getCooldowns().removeCooldown(weapon);player.getInventory().getItem(1).setCount(0);
                weapon.use(level,player,InteractionHand.MAIN_HAND);
                check(data.getEntropy()==4 && pistol.getDamageValue()==2, "empty ammunition does not fire");
                target.discard();
                var pos = new BlockPos(10,100,0);level.setBlockAndUpdate(pos,ModBlocks.PROTOCOL_RELAY.get().defaultBlockState());
                var relay=(ProtocolRelayBlockEntity)level.getBlockEntity(pos);
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.PROTOCOL_FRAGMENT.get(),3));
                ModBlocks.PROTOCOL_RELAY.get().use(level.getBlockState(pos),level,pos,player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
                check(relay.getCharge()==200 && player.getMainHandItem().getCount()==2, "relay charges by actual block interaction");
                for(int i=0;i<100;i++)ProtocolRelayBlockEntity.serverTick(level,pos,level.getBlockState(pos),relay);
                check(relay.getCharge()==200 && !level.getBlockState(pos).getValue(ProtocolRelayBlock.ACTIVE), "unpowered relay stays inactive");
                level.setBlockAndUpdate(pos.east(),Blocks.REDSTONE_BLOCK.defaultBlockState());data.setIntegrity(0);data.setEntropy(100);
                for(int i=0;i<100;i++)ProtocolRelayBlockEntity.serverTick(level,pos,level.getBlockState(pos),relay);
                check(relay.getCharge()==195 && data.getIntegrity()==1 && data.getEntropy()==99, "powered relay pulse and fuel consumption");
                var relaySaved=relay.saveWithoutMetadata();var restored=new ProtocolRelayBlockEntity(pos,level.getBlockState(pos));restored.load(relaySaved);
                check(restored.getCharge()==195, "relay NBT persists fuel");
                ProtocolEvents.onDeath(new LivingDeathEvent(player,player.damageSources().generic()));
                check(data.getEcho(player.getUUID())!=null,"death event records causal echo");
                var restoredData=ProtocolSavedData.load(data.save(new net.minecraft.nbt.CompoundTag()));
                check(restoredData.getIntegrity()==1 && restoredData.getEntropy()==99 && restoredData.getEcho(player.getUUID())!=null,"world NBT persists integrity entropy and echoes");
                player.tickCount=20;
                ProtocolEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,player));
                check(data.getEcho(player.getUUID())==null,"living player recovers causal echo within eight blocks");
                data.setEntropy(9000);check(data.getPhase().name().equals("CASCADE"),"high entropy selects cascade phase");
                data.setEntropy(0);data.setIntegrity(3000);check(data.getPhase().name().equals("STABLE"),"integrity selects stable phase");
                player.getInventory().clearContent();
                var sink = new ItemStack(ModItems.ENTROPY_SINK.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, sink);
                player.getInventory().setItem(1, new ItemStack(ModItems.ENTROPY_CRYSTAL.get(), 2));
                data.setIntegrity(0); data.setEntropy(600);
                ModItems.ENTROPY_SINK.get().use(level,player,InteractionHand.MAIN_HAND);
                check(data.getEntropy()==350 && data.getIntegrity()==10 && player.getInventory().getItem(1).getCount()==1, "sink consumes one crystal and purifies world");
                check(sink.getDamageValue()==1, "sink consumes durability");
                ModItems.ENTROPY_SINK.get().use(level,player,InteractionHand.MAIN_HAND);
                check(data.getEntropy()==350 && player.getInventory().getItem(1).getCount()==1, "sink enforces cooldown");
                player.getCooldowns().removeCooldown(ModItems.ENTROPY_SINK.get());
                data.setEntropy(0);
                ModItems.ENTROPY_SINK.get().use(level,player,InteractionHand.MAIN_HAND);
                check(player.getInventory().getItem(1).getCount()==1 && sink.getDamageValue()==1, "zero entropy preserves fuel and durability");
                player.getInventory().getItem(1).setCount(0);data.setEntropy(500);
                ModItems.ENTROPY_SINK.get().use(level,player,InteractionHand.MAIN_HAND);
                check(data.getEntropy()==500 && sink.getDamageValue()==1, "missing crystal cannot purify");
                var progress = player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
                check((progress.getInt("AshenMilestones") & (1 << 3))!=0 && (progress.getInt("AshenMilestones") & (1 << 6))!=0,"shooting and purification persist native milestones");
                int xp=player.totalExperience;
                dev.yuni.ashenprotocol.progress.Progression.complete(player,6);
                check(player.totalExperience==xp,"milestone reward cannot be farmed repeatedly");
                var roundTrip=progress.copy();check(roundTrip.getInt("AshenMilestones")==dev.yuni.ashenprotocol.progress.Progression.mask(player),"progress stored in death-persistent player NBT");
                System.out.println("AP_PROBE_SUCCESS: " + checks + " runtime checks passed");
                server.halt(false);
            } catch (Throwable e) {
                e.printStackTrace();System.out.println("AP_PROBE_FAILURE: " + e);server.halt(false);
            }
        });
    }
}
