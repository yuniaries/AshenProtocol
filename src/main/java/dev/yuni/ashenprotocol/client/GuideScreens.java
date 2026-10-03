package dev.yuni.ashenprotocol.client;
import net.minecraft.client.Minecraft;
public final class GuideScreens {
    public static void open() { Minecraft.getInstance().setScreen(new ProtocolJournalScreen(null)); }
}
