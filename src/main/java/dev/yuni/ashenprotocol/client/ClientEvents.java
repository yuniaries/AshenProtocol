package dev.yuni.ashenprotocol.client;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.protocol.ProtocolPhase;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.ItemStack;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    public static final KeyMapping JOURNAL = new KeyMapping("key.ashenprotocol.journal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.ashenprotocol");
    @SubscribeEvent public static void opening(ScreenEvent.Opening e) { if (e.getNewScreen() instanceof TitleScreen) e.setNewScreen(new ProtocolTitleScreen()); }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { ClientState.clear(); }
    private static boolean checkedJournalBinding;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            var mc = Minecraft.getInstance();
            if (!checkedJournalBinding) {
                checkedJournalBinding = true;
                if (JOURNAL.getKey().equals(mc.options.keySocialInteractions.getKey())) {
                    JOURNAL.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_J));
                    KeyMapping.resetMapping();
                    mc.options.save();
                }
            }
            if (mc.player.tickCount % 20 == 0) mc.getWindow().setTitle("灰烬协议：生态重启 | yuniaries | 1.1.0");
            while (JOURNAL.consumeClick()) mc.setScreen(new ProtocolJournalScreen(null));
        }
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.options.hideGui || !ClientState.connected) return;
        if(ClientState.tracked>=0&&ClientState.tracked<dev.yuni.ashenprotocol.progress.Progression.TITLES.length){
            int id=ClientState.tracked;int w=Math.min(200,mc.getWindow().getGuiScaledWidth()/2-12),x=mc.getWindow().getGuiScaledWidth()-w-6,y=mc.getWindow().getGuiScaledHeight()-84;var q=e.getGuiGraphics();
            q.fill(x,y,x+w,y+65,0xd0111e2b);q.fill(x,y,x+2,y+65,0xff63e9db);
            q.drawString(mc.font,"追踪目标 · J 任务书",x+8,y+7,0xff63e9db);
            q.drawString(mc.font,mc.font.plainSubstrByWidth(dev.yuni.ashenprotocol.progress.Progression.TITLES[id],w-16),x+8,y+20,0xfff2eadb);
            int target=dev.yuni.ashenprotocol.progress.QuestCatalog.target(id),current=ClientState.progress[id];q.drawString(mc.font,"进度 "+current+" / "+target,x+8,y+33,0xffc6d3df);
            q.drawString(mc.font,mc.font.plainSubstrByWidth(dev.yuni.ashenprotocol.progress.QuestCatalog.hint(id),w-16),x+8,y+45,0xffb9a984);
            q.fill(x+8,y+58,x+w-8,y+60,0xff293944);q.fill(x+8,y+58,x+8+Math.min(current,target)*(w-16)/target,y+60,0xff63e9db);
        }
        if (!mc.player.getMainHandItem().is(ModItems.ENTROPY_METER.get()) && !mc.player.getOffhandItem().is(ModItems.ENTROPY_METER.get())) return;
        int bosses = mc.level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, mc.player.getBoundingBox().inflate(96), entity ->
            entity instanceof dev.yuni.ashenprotocol.expansion.ProtocolMob mob && mob.isBoss()
            || entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss).size();
        var g = e.getGuiGraphics();
        g.pose().pushPose();
        g.pose().translate(0, Math.min(mc.getWindow().getGuiScaledHeight() / 3, bosses * 20), 0);
        g.fill(6, 6, 186, 49, 0xc009121c);
        g.drawString(mc.font, "协议 " + ProtocolPhase.from(ClientState.integrity, ClientState.entropy).zh() + " · J 手册", 11, 11, 0xff63e9db);
        g.drawString(mc.font, "完整度 " + ClientState.integrity + " / 熵债 " + ClientState.entropy, 11, 25, 0xffc6d3df);
        g.fill(11, 39, 179, 43, 0xff293944);
        g.fill(11, 39, 11 + ClientState.integrity * 168 / 10000, 43, 0xff63e9db);
        g.pose().popPose();
    }
    @Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e) { e.register(JOURNAL); }
    }
}
