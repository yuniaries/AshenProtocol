package dev.yuni.ashenprotocol.client;
import dev.yuni.ashenprotocol.network.ProtocolNetwork;
public final class ClientState {
    public static int integrity, entropy, milestones;
    public static long[] milestoneBits=new long[2];
    public static boolean completed(int index){return index>=0&&index<128&&(milestoneBits[index/64]&(1L<<(index%64)))!=0;}
    public static String echo = "";
    public static boolean connected;
    public static void accept(ProtocolNetwork.State s) { integrity = s.integrity(); entropy = s.entropy(); milestoneBits=java.util.Arrays.copyOf(s.milestones(),2);milestones=(int)milestoneBits[0]; echo = s.echo(); connected = true; }
    public static void clear() { integrity = entropy = milestones = 0; milestoneBits=new long[2];echo = ""; connected = false; }
    private ClientState() {}
}
