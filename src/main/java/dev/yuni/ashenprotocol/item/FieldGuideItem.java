package dev.yuni.ashenprotocol.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class FieldGuideItem extends Item {
    public FieldGuideItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack,Level level,java.util.List<Component> lines,net.minecraft.world.item.TooltipFlag flag){
        lines.add(Component.literal("右键打开12章任务书；也可按 J").withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.literal("查看步骤、前置任务、进度与奖励").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.literal("选择追踪，让下一步显示在游戏画面").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> dev.yuni.ashenprotocol.client.GuideScreens.open());
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
