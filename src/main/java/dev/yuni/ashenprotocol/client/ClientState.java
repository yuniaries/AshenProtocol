package dev.yuni.ashenprotocol.client;
import dev.yuni.ashenprotocol.network.ProtocolNetwork;
public final class ClientState {
    public static int integrity, entropy, milestones;
    public static String echo = "";
    public static boolean connected;
    public static void accept(ProtocolNetwork.State s) { integrity = s.integrity(); entropy = s.entropy(); milestones = s.milestones(); echo = s.echo(); connected = true; }
    public static void clear() { integrity = entropy = milestones = 0; echo = ""; connected = false; }
    private ClientState() {}
}
