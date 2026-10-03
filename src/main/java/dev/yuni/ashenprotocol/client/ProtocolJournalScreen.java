package dev.yuni.ashenprotocol.client;

import dev.yuni.ashenprotocol.progress.Progression;
import dev.yuni.ashenprotocol.protocol.ProtocolPhase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProtocolJournalScreen extends Screen {
    private final Screen parent;
    private int chapter;
    private static final String[] DETAILS = {
        "在工作台让四个红石围绕一颗紫水晶碎片，得到 2 枚协议碎片。碎片既是中继燃料，也是相位手枪弹药。目标：背包中持有协议碎片。",
        "用指南针、上下各一份红石、左右各一枚协议碎片合成熵表。手持右键读数，也可以按 J 打开本面板。世界状态由服务器同步，退出后保存在世界中。",
        "中继配方：第一行铁锭/碎片/铁锭；第二行红石/黑曜石/红石；第三行铁锭/碎片/铁锭。用碎片右键充能，再接红石信号。每枚燃料维持 200 秒，每 5 秒完整度 +1、熵债 -1。只在区块加载时工作。",
        "手枪配方：第一行铜锭/铁锭/碎片；第二行空/铁锭/红石；第三行空/铁锭/空。背包放碎片，右键射击。射程 32 格，伤害 8；消耗一枚碎片并增加 2 点熵债。墙壁阻挡射线。目标：实际开火一次。",
        "死亡会记录最后一次死亡地点。重生后用熵表或本面板查看坐标，回到同一维度的 8 格范围，回收因果残渣。回声不保管普通掉落物，再次死亡会覆盖位置。残渣可无序合成 4 枚碎片。",
        "将末影珍珠放在中间、上下左右各放紫水晶碎片，合成熵晶体。晶体是主动净化的消耗品。目标：背包持有熵晶体。",
        "净化器配方：第一行铁锭/熵表/铁锭；第二行碎片/熵晶体/碎片；第三行铁锭/红石/铁锭。背包放熵晶体，手持右键：消耗晶体，熵债 -250、完整度 +10，冷却 5 秒。没有熵债时不会扣材料。目标：实际净化一次。",
        "建设并维护中继网络，直到完整度至少 3000、熵债低于 1000，且完成一次主动净化。高熵会带来黑暗、残影与视觉闪电。任务只奖励一次经验，死亡不重置。达成重建后仍可继续生存。"
    };
    public ProtocolJournalScreen(Screen parent) { super(Component.literal("协议终端")); this.parent = parent; }
    @Override protected void init() {
        int left = Math.max(8, width / 2 - 205), top = 40;
        for (int i = 0; i < DETAILS.length; i++) {
            final int n = i;
            addRenderableWidget(Button.builder(Component.literal((i + 1) + " · " + Progression.TITLES[i]), b -> chapter = n).bounds(left, top + i * 23, 112, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("返回"), b -> onClose()).bounds(width / 2 - 50, height - 28, 100, 20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xf509121c);
        g.drawCenteredString(font, "灰烬协议 / 协议终端", width / 2, 12, 0xff63e9db);
        int left = Math.max(8, width / 2 - 205), x = left + 126, w = Math.max(100, width - x - 16);
        g.drawString(font, Progression.TITLES[chapter], x, 43, 0xfff2eadb);
        boolean done = (ClientState.milestones & (1 << chapter)) != 0;
        g.drawString(font, ClientState.connected ? (done ? "已完成 · 经验奖励已发放" : "进行中 · 完成条件后自动记录") : "离线手册 · 进入世界后显示进度", x, 62, done ? 0xff63e9db : 0xffb9a984);
        int y = 85;
        for (var line : font.split(Component.literal(DETAILS[chapter]), Math.min(w, 280))) { g.drawString(font, line, x, y, 0xffc6d3df); y += 12; }
        if (ClientState.connected) {
            int statusY = Math.max(y + 12, height - 81);
            g.drawString(font, "完整度 " + ClientState.integrity + " / 熵债 " + ClientState.entropy, x, statusY, 0xff63e9db);
            g.drawString(font, "阶段：" + ProtocolPhase.from(ClientState.integrity, ClientState.entropy).zh(), x, statusY + 13, 0xffc6d3df);
            if (!ClientState.echo.isEmpty()) g.drawString(font, "回声：" + ClientState.echo, x, statusY + 26, 0xffbc90df);
        }
        super.render(g, mx, my, delta);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
