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
            mc.setScreen(new ProtocolJournalScreen(null));
        }
        if (ticks == 100) {
            Screenshot.grab(mc.gameDirectory, "native-journal.png", mc.getMainRenderTarget(), c -> System.out.println("AP_CLIENT_JOURNAL: " + c.getString()));
            System.out.println("AP_CLIENT_SMOKE_SUCCESS: network state, real inventory, HUD and native journal rendered; milestones=" + ClientState.milestones);
        }
        if (ticks == 120) mc.stop();
    }
}
