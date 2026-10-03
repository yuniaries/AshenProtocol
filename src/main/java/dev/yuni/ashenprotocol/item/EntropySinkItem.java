package dev.yuni.ashenprotocol.item;

import dev.yuni.ashenprotocol.progress.Progression;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntropySinkItem extends Item {
    public EntropySinkItem(Properties p) { super(p); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer p) || p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        var data = ProtocolSavedData.get(p.getServer());
        if (data.getEntropy() == 0) { p.displayClientMessage(Component.literal("当前没有熵债，无需净化。"), true); return InteractionResultHolder.fail(stack); }
        ItemStack fuel = ItemStack.EMPTY;
        for (ItemStack candidate : p.getInventory().items) if (candidate.is(ModItems.ENTROPY_CRYSTAL.get())) { fuel = candidate; break; }
        if (fuel.isEmpty() && !p.isCreative()) { p.displayClientMessage(Component.literal("需要一枚熵晶体作为净化介质。"), true); return InteractionResultHolder.fail(stack); }
        if (!p.isCreative()) fuel.shrink(1);
        data.addEntropy(-250); data.addIntegrity(10);
        p.getCooldowns().addCooldown(this, 100);
        stack.hurtAndBreak(1, p, who -> who.broadcastBreakEvent(hand));
        Progression.complete(p, 6);
        p.displayClientMessage(Component.literal("净化完成：熵债 -250，完整度 +10。"), true);
        return InteractionResultHolder.consume(stack);
    }
}
