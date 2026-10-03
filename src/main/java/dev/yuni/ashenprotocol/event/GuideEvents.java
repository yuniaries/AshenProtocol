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
        player.sendSystemMessage(Component.literal("灰烬协议已接入。右键野外手册，从协议碎片开始探索。"));
    }
}

