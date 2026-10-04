package dev.yuni.ashenprotocol.event;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID)
public final class GuideEvents {
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var persistent = player.getPersistentData();
        var data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        dev.yuni.ashenprotocol.network.ProtocolNetwork.sync(player);
        if (data.getBoolean("AshenGuideReceived")) return;
        var guide = new ItemStack(ModItems.FIELD_GUIDE.get());
        if (!player.getInventory().add(guide)) player.drop(guide, false);
        data.putBoolean("AshenGuideReceived", true);
        persistent.put(Player.PERSISTED_NBT_TAG, data);
        player.sendSystemMessage(Component.literal("欢迎进入灰烬协议。按 J 或右键任务书查看章节；右下角会指引当前目标，从紫水晶与红石制作协议碎片开始。"));
    }
}

