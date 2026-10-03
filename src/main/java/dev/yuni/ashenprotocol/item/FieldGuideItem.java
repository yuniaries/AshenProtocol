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
    private static final String[] PAGES = {
        "灰烬协议：回声网络\n\n力量越强，世界代价越高。\n\n从普通生存开始：紫水晶与红石 → 协议碎片 → 熵表 → 协议中继 → 相位手枪。\n\n右键本手册即可随时查看。所有物品均可生存合成。",
        "1 · 协议碎片\n\n工作台摆法：\n　 红石\n红石 紫水晶 红石\n　 红石\n\n每次获得 2 个碎片。碎片既是相位手枪弹药，也是中继燃料。",
        "2 · 熵表\n\n工作台摆法：\n　 红石\n碎片 指南针 碎片\n　 红石\n\n手持右键：查看全世界完整度、熵债、阶段，以及你最近一次死亡留下的回声坐标。",
        "3 · 协议中继\n\n铁锭 碎片 铁锭\n红石 黑曜石 红石\n铁锭 碎片 铁锭\n\n放置后，用碎片右键充能，每个提供 200 秒燃料。旁边放红石火把启动。\n每 5 秒：完整度 +1，熵债 -1。只有加载中的中继运行。",
        "4 · 相位手枪\n\n铜锭 铁锭 碎片\n　　 铁锭 红石\n　　 铁锭\n\n背包放碎片，瞄准右键射击。射程 32 格，伤害 8，消耗碎片 1、耐久 1；每枪增加熵债 2。墙壁会挡住射线。",
        "5 · 因果回声\n\n死亡会记录你最近一次死亡地点。重生后用熵表找坐标，返回同一维度、距离 8 格以内，回收残渣。\n\n回声不会替你保存掉落物。再次死亡会覆盖旧回声。",
        "6 · 世界阶段\n\n完整度 500：恢复\n完整度 3000：稳定\n熵债 4000：裂解\n熵债 8000：级联失稳\n\n高熵可能引发黑暗、协议残影与视觉闪电。建设中继网络处理代价。世界状态随存档保存。",
        "7 · 更多材料与指令\n\n熵晶体：紫水晶围绕末影珍珠。\n\n/protocol status\n查看世界状态，无需作弊。\n\n管理测试（需作弊）：\n/protocol entropy set 9000\n/protocol integrity set 3000\n\n按 P 打开原生协议终端，查看八章任务与同步世界状态。"
    };
    public static ItemStack createBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "灰烬协议 · 野外手册");
        tag.putString("author", "yuniaries");
        tag.putBoolean("resolved", true);
        ListTag pages = new ListTag();
        for (String page : PAGES) pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page))));
        tag.put("pages", pages);
        return book;
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
