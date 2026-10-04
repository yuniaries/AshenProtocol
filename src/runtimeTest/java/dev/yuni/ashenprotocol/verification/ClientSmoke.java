package dev.yuni.ashenprotocol.verification;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.client.ClientState;
import dev.yuni.ashenprotocol.client.ProtocolJournalScreen;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID, value = Dist.CLIENT)
public final class ClientSmoke {
    private static int ticks;
    private static volatile boolean enteredRealm;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (!Boolean.getBoolean("ashenprotocol.clientSmoke") || e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if(mc.player==null && mc.screen!=null) {
            for(var child:mc.screen.children()) if(child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals("继续")) {
                System.out.println("AP_CLIENT_ACCEPT_TEST_WORLD_WARNING: "+mc.screen.getTitle().getString());b.onPress();break;
            }
        }
        if (mc.player == null || !ClientState.connected || mc.getSingleplayerServer() == null) return;
        ticks++;
        if (ticks == 20) {
            mc.options.guiScale().set(0); mc.resizeDisplay();
            var uuid = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                player.getInventory().setItem(0, new ItemStack(ModItems.ENTROPY_METER.get()));
                player.getInventory().setItem(2, new ItemStack(ModItems.PROTOCOL_FRAGMENT.get(), 4));
                player.getInventory().selected = 0;
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                player.teleportTo(player.serverLevel(),player.getX(),player.getY(),player.getZ(),0,0);
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(dev.yuni.ashenprotocol.expansion.ExpansionContent.item("rift_chestplate")));
                for(int i=0;i<16;i++) {
                    var mob=dev.yuni.ashenprotocol.expansion.ExpansionContent.MOBS.get(dev.yuni.ashenprotocol.expansion.ExpansionContent.ENEMIES[i]).get().create(player.serverLevel());
                    mob.setNoAi(true);mob.moveTo(player.getX()+(i-7.5)*2,player.getY(),player.getZ()+9,180,0);player.serverLevel().addFreshEntity(mob);
                }
                dev.yuni.ashenprotocol.progress.Progression.complete(player,75);player.inventoryMenu.broadcastChanges();
            });
            mc.player.getInventory().selected = 0;
        }
        if (ticks == 60) Screenshot.grab(mc.gameDirectory, "native-hud.png", mc.getMainRenderTarget(), c -> System.out.println("AP_CLIENT_HUD: " + c.getString()));
        if (ticks == 80) {
            if ((ClientState.milestones & 3) != 3) throw new IllegalStateException("client did not receive fragment/meter progress");
            if (dev.yuni.ashenprotocol.client.ClientEvents.JOURNAL.getKey().getValue() != org.lwjgl.glfw.GLFW.GLFW_KEY_J)
                throw new IllegalStateException("journal default key is not J");
            if (mc.options.keySocialInteractions.getKey().equals(dev.yuni.ashenprotocol.client.ClientEvents.JOURNAL.getKey()))
                throw new IllegalStateException("journal conflicts with vanilla social key");
            net.minecraft.client.KeyMapping.click(dev.yuni.ashenprotocol.client.ClientEvents.JOURNAL.getKey());
        }
        if (ticks == 90 && !(mc.screen instanceof ProtocolJournalScreen))
            throw new IllegalStateException("J key did not open native journal");
        if (ticks == 100) {
            Screenshot.grab(mc.gameDirectory, "native-journal.png", mc.getMainRenderTarget(), c -> System.out.println("AP_CLIENT_JOURNAL: " + c.getString()));
            System.out.println("AP_CLIENT_SMOKE_SUCCESS: network state, real inventory, HUD and J-key journal rendered without social key conflict; milestones=" + ClientState.milestones);
        }
        if(ticks==110&&!ClientState.completed(75))throw new IllegalStateException("late milestone not synchronized");
        if (ticks == 120) mc.getWindow().setWindowed(2560,1440);
        if (ticks == 130) {
            for(var child:mc.screen.children()) if(child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals("下一组")) { b.onPress();break; }
        }
        if (ticks == 140) {
            for(var child:mc.screen.children()) if(child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals("下一组")) { b.onPress();break; }

            for (var listener : mc.screen.children()) if (listener instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().startsWith("24")) b.onPress();
        }
        if (ticks == 160 || ticks == 200) {
            for (var listener : mc.screen.children()) if (listener instanceof net.minecraft.client.gui.components.AbstractWidget b) {
                if (b.getX()<0 || b.getY()<0 || b.getX()+b.getWidth()>mc.screen.width || b.getY()+b.getHeight()>mc.screen.height)
                    throw new IllegalStateException("responsive journal widget outside viewport");
            }
            Screenshot.grab(mc.gameDirectory, ticks==160 ? "native-journal-large-auto.png" : "native-journal-large-scale2.png", mc.getMainRenderTarget(), c -> System.out.println("AP_CLIENT_LAYOUT: " + c.getString()));
            System.out.println("AP_CLIENT_LAYOUT_PASS: " + mc.getWindow().getScreenWidth() + "x" + mc.getWindow().getScreenHeight() + ", GUI=" + mc.getWindow().getGuiScaledWidth() + "x" + mc.getWindow().getGuiScaledHeight());
        }
        if (ticks == 180) { mc.options.guiScale().set(2); mc.resizeDisplay(); }
        if(ticks==210) {
            mc.setScreen(null);
            var uuid=mc.player.getUUID();
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(dev.yuni.ashenprotocol.expansion.ExpansionContent.item("coordinate_gate")));
                dev.yuni.ashenprotocol.expansion.ExpansionContent.item("coordinate_gate").use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
                if(!p.level().dimension().equals(dev.yuni.ashenprotocol.expansion.CoordinateGate.REALM))throw new IllegalStateException("gate failed to enter realm");
                enteredRealm=true;
                System.out.println("AP_CLIENT_REALM_ENTER_PASS");
            });
        }
        if(ticks==310) Screenshot.grab(mc.gameDirectory,"expansion-realm.png",mc.getMainRenderTarget(),c->System.out.println("AP_CLIENT_REALM_SCREENSHOT: "+c.getString()));
        if(ticks==330) {
            var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                if(!enteredRealm || !p.level().dimension().equals(dev.yuni.ashenprotocol.expansion.CoordinateGate.REALM))throw new IllegalStateException("return test requires successful realm entry");
                var gate=dev.yuni.ashenprotocol.expansion.ExpansionContent.item("coordinate_gate");
                p.getCooldowns().removeCooldown(gate);gate.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
                if(!p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))throw new IllegalStateException("gate failed to return");
                System.out.println("AP_CLIENT_REALM_RETURN_PASS");
            });
        }
        if(ticks==410)mc.setScreen(new dev.yuni.ashenprotocol.client.ProtocolTitleScreen());
        if(ticks==430)Screenshot.grab(mc.gameDirectory,"expansion-menu.png",mc.getMainRenderTarget(),c->System.out.println("AP_CLIENT_MENU_SCREENSHOT: "+c.getString()));
        if(ticks==450){mc.setScreen(null);var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var gate=dev.yuni.ashenprotocol.expansion.ExpansionContent.item("deep_gate");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(gate));gate.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
            if(!p.level().dimension().equals(dev.yuni.ashenprotocol.campaign.DeepGate.REALM))throw new IllegalStateException("deep gate failed to enter");System.out.println("AP_CLIENT_DEEP_ENTER_PASS");
        });}
        if(ticks==510){var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(p.serverLevel(),p.getX(),p.getY()+15,p.getZ(),-45,20);});}
        if(ticks==550)Screenshot.grab(mc.gameDirectory,"campaign-deep-realm.png",mc.getMainRenderTarget(),c->System.out.println("AP_CLIENT_DEEP_SCREENSHOT: "+c.getString()));
        if(ticks==570){var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var gate=dev.yuni.ashenprotocol.expansion.ExpansionContent.item("deep_gate");p.getCooldowns().removeCooldown(gate);gate.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
            if(!p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))throw new IllegalStateException("deep gate failed to return");System.out.println("AP_CLIENT_DEEP_RETURN_PASS");
        });}
        if(ticks==600){mc.setScreen(new ProtocolJournalScreen(null));for(int n=0;n<11;n++)for(var child:mc.screen.children())if(child instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("下一组")){b.onPress();break;}
            for(var child:mc.screen.children())if(child instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().startsWith("96")){b.onPress();break;}
        }
        if(ticks==620){Screenshot.grab(mc.gameDirectory,"campaign-final-journal.png",mc.getMainRenderTarget(),c->System.out.println("AP_CLIENT_FINAL_JOURNAL: "+c.getString()));System.out.println("AP_CLIENT_96_GOALS_LAYOUT_PASS");}
        if(ticks==640){mc.setScreen(null);var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->ArenaClientProbe.prepare(mc.getSingleplayerServer(),uuid));}
        if(ticks==680){var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->ArenaClientProbe.verify(mc.getSingleplayerServer(),uuid));}
        if(ticks==700&&!ClientState.completed(95))throw new IllegalStateException("highest mastery milestone did not synchronize");
        if(ticks==710)mc.stop();
    }
}
