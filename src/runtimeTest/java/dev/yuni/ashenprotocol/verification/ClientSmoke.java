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
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (!Boolean.getBoolean("ashenprotocol.clientSmoke") || e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
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
                player.inventoryMenu.broadcastChanges();
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
        if (ticks == 120) mc.getWindow().setWindowed(2560,1440);
        if (ticks == 140) {
            for (var listener : mc.screen.children()) if (listener instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().startsWith("7")) b.onPress();
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
        if (ticks == 220) mc.stop();
    }
}
