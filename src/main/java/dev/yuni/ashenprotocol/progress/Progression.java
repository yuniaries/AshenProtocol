package dev.yuni.ashenprotocol.progress;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.network.ProtocolNetwork;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AshenProtocol.MOD_ID)
public final class Progression {
    public static final String[] TITLES = {"协议接入", "读取信号", "网络节点", "力量的代价", "因果回收", "熵的结晶", "主动净化", "世界重建"};
    private static CompoundTag persisted(ServerPlayer p) { return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG); }
    public static int mask(ServerPlayer p) { return persisted(p).getInt("AshenMilestones"); }
    public static void complete(ServerPlayer p, int index) {
        var tag = persisted(p); int bits = tag.getInt("AshenMilestones");
        if ((bits & (1 << index)) != 0) return;
        tag.putInt("AshenMilestones", bits | (1 << index));
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        p.sendSystemMessage(Component.literal("[灰烬协议] 已完成：" + TITLES[index]));
        p.giveExperiencePoints(15 + index * 5);
    }
    private static boolean has(ServerPlayer p, Item item) { return p.getInventory().contains(new ItemStack(item)); }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p) || p.tickCount % 20 != 0 || !p.isAlive()) return;
        if (has(p, ModItems.PROTOCOL_FRAGMENT.get())) complete(p, 0);
        if (has(p, ModItems.ENTROPY_METER.get())) complete(p, 1);
        if (has(p, ModItems.PROTOCOL_RELAY_ITEM.get())) complete(p, 2);
        if (has(p, ModItems.ECHO_RESIDUE.get())) complete(p, 4);
        if (has(p, ModItems.ENTROPY_CRYSTAL.get())) complete(p, 5);
        var data = ProtocolSavedData.get(p.getServer());
        if ((mask(p) & (1 << 6)) != 0 && data.getIntegrity() >= 3000 && data.getEntropy() < 1000) complete(p, 7);
        ProtocolNetwork.sync(p);
    }
    private Progression() {}
}
