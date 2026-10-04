package dev.yuni.ashenprotocol.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraftforge.client.gui.ModListScreen;

public final class ProtocolTitleScreen extends Screen {
    public static final String PROJECT_URL = "https://github.com/yuniaries/AshenProtocol";
    public ProtocolTitleScreen() { super(Component.literal("灰烬协议：生态重启")); }
    private void button(String name, int x, int y, int w, Button.OnPress action) {
        addRenderableWidget(Button.builder(Component.literal(name), action).bounds(x, y, w, 20).build());
    }
    @Override protected void init() {
        minecraft.getWindow().setTitle("灰烬协议：生态重启 | yuniaries | 1.0.0");
        int x = width / 2 - 100, y = Math.max(68, height / 2 - 55);
        button("进入世界", x, y, 200, b -> minecraft.setScreen(new SelectWorldScreen(this)));
        button("连接服务器", x, y + 22, 200, b -> minecraft.setScreen(new JoinMultiplayerScreen(this)));
        button("协议手册", x, y + 44, 200, b -> minecraft.setScreen(new ProtocolJournalScreen(this)));
        button("设置", x, y + 66, 97, b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options)));
        button("MOD 信息", x + 103, y + 66, 97, b -> minecraft.setScreen(new ModListScreen(this)));
        button("GitHub 主页", x, y + 88, 97, b -> Util.getPlatform().openUri("https://github.com/yuniaries"));
        button("项目仓库", x + 103, y + 88, 97, b -> Util.getPlatform().openUri(PROJECT_URL));
        button("退出", x, y + 110, 200, b -> minecraft.stop());
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fillGradient(0, 0, width, height, 0xff09121c, 0xff1c2330);
        for (int x = 0; x < width; x += 32) g.fill(x, 0, x + 1, height, 0x142bddcf);
        for (int y = 0; y < height; y += 32) g.fill(0, y, width, y + 1, 0x142bddcf);
        int mid = width / 2;
        g.drawCenteredString(font, "ASHEN PROTOCOL", mid, Math.max(10, height / 2 - 113), 0xff63e9db);
        g.drawCenteredString(font, "灰烬协议：生态重启", mid, Math.max(30, height / 2 - 88), 0xfff2eadb);
        g.drawCenteredString(font, "每一次重建，都需要面对力量的代价。", mid, Math.max(48, height / 2 - 70), 0xffa1b4c5);
        g.drawString(font, "灰烬协议 1.0.0 · yuniaries", 8, height - 12, 0xff899aa9);
        super.render(g, mouseX, mouseY, delta);
    }
    @Override public boolean shouldCloseOnEsc() { return false; }
}
