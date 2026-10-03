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
    private int scroll;
    private int panelX, panelY, panelWidth, panelHeight, navWidth, bodyX, bodyY, bodyWidth, bodyBottom;
    private final java.util.List<Button> chapters = new java.util.ArrayList<>();
    @Override protected void init() {
        chapters.clear();
        panelWidth = Math.min(760, width - 24);
        panelHeight = Math.min(420, height - 24);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        navWidth = Math.max(96, Math.min(160, panelWidth / 4));
        bodyX = panelX + navWidth + 24;
        bodyY = panelY + 65;
        bodyWidth = panelX + panelWidth - 14 - bodyX;
        bodyBottom = panelY + panelHeight - 78;
        int gap = Math.min(26, (panelHeight - 64) / 8);
        for (int i = 0; i < DETAILS.length; i++) {
            final int n = i;
            Button b = Button.builder(Component.literal((i + 1) + " · " + Progression.TITLES[i]), button -> { chapter = n; scroll = 0; })
                .bounds(panelX + 12, panelY + 34 + i * gap, navWidth, Math.max(14, gap - 3)).build();
            chapters.add(b); addRenderableWidget(b);
        }
        addRenderableWidget(Button.builder(Component.literal("返回"), b -> onClose())
            .bounds(width / 2 - 45, panelY + panelHeight - 25, 90, 18).build());
    }
    private int maxScroll() {
        return Math.max(0, font.split(Component.literal(DETAILS[chapter]), bodyWidth).size() * 12 - (bodyBottom - bodyY));
    }
    @Override public boolean mouseScrolled(double mx, double my, double amount) {
        if (mx >= bodyX && mx < bodyX + bodyWidth && my >= bodyY && my < bodyBottom) {
            scroll = net.minecraft.util.Mth.clamp(scroll - (int)(amount * 24), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mx, my, amount);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xe009121c);
        g.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xff111e2b);
        g.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xff63e9db);
        g.fill(bodyX - 9, panelY + 33, bodyX - 8, panelY + panelHeight - 34, 0xff2b3e4d);
        g.drawString(font, "灰烬协议 / 协议终端", panelX + 12, panelY + 12, 0xff63e9db);
        for (int i = 0; i < chapters.size(); i++) chapters.get(i).active = i != chapter;
        g.drawString(font, Progression.TITLES[chapter], bodyX, panelY + 34, 0xfff2eadb);
        boolean done = (ClientState.milestones & (1 << chapter)) != 0;
        String state = ClientState.connected ? (done ? "已完成 · 奖励已发放" : "进行中 · 自动记录") : "离线教程";
        g.drawString(font, state, bodyX, panelY + 49, done ? 0xff63e9db : 0xffb9a984);
        scroll = net.minecraft.util.Mth.clamp(scroll, 0, maxScroll());
        g.enableScissor(bodyX, bodyY, bodyX + bodyWidth, bodyBottom);
        int y = bodyY - scroll;
        for (var line : font.split(Component.literal(DETAILS[chapter]), bodyWidth)) {
            g.drawString(font, line, bodyX, y, 0xffc6d3df); y += 12;
        }
        g.disableScissor();
        if (maxScroll() > 0) {
            int track = bodyBottom - bodyY;
            int thumb = Math.max(8, track * track / (track + maxScroll()));
            int top = bodyY + scroll * (track - thumb) / maxScroll();
            g.fill(bodyX + bodyWidth + 3, bodyY, bodyX + bodyWidth + 5, bodyBottom, 0xff293944);
            g.fill(bodyX + bodyWidth + 3, top, bodyX + bodyWidth + 5, top + thumb, 0xff63e9db);
        }
        int statusY = panelY + panelHeight - 69;
        g.fill(bodyX, statusY - 4, panelX + panelWidth - 12, statusY - 3, 0xff2b3e4d);
        if (ClientState.connected) {
            g.drawString(font, "完整度 " + ClientState.integrity + " / 熵债 " + ClientState.entropy, bodyX, statusY, 0xff63e9db);
            g.drawString(font, "阶段：" + ProtocolPhase.from(ClientState.integrity, ClientState.entropy).zh(), bodyX, statusY + 12, 0xffc6d3df);
            if (!ClientState.echo.isEmpty()) {
                g.enableScissor(bodyX, statusY + 24, bodyX + bodyWidth, statusY + 35);
                g.drawString(font, "回声：" + ClientState.echo, bodyX, statusY + 24, 0xffbc90df); g.disableScissor();
            }
        } else g.drawString(font, "进入世界后同步任务状态", bodyX, statusY, 0xff899aa9);
        super.render(g, mx, my, delta);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
