package dev.yuni.ashenprotocol.network;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.progress.Progression;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public final class ProtocolNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AshenProtocol.MOD_ID, "state"), () -> "3", "3"::equals, "3"::equals);
    public static void register() {
        CHANNEL.messageBuilder(State.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(State::encode).decoder(State::decode).consumerMainThread(State::handle).add();
        CHANNEL.messageBuilder(Track.class,1,NetworkDirection.PLAY_TO_SERVER).encoder((s,b)->b.writeVarInt(s.id())).decoder(b->new Track(b.readVarInt())).consumerMainThread((s,c)->{var p=c.get().getSender();if(p!=null&&dev.yuni.ashenprotocol.progress.QuestTracking.select(p,s.id()))sync(p);c.get().setPacketHandled(true);}).add();
    }
    public static void sync(ServerPlayer player) {
        var data = ProtocolSavedData.get(player.getServer());
        var echo = data.getEcho(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new State(data.getIntegrity(), data.getEntropy(),
            Progression.bits(player), dev.yuni.ashenprotocol.progress.QuestCatalog.progressValues(player),dev.yuni.ashenprotocol.progress.QuestTracking.selected(player), echo == null ? "" : echo.dimension() + " / " + echo.pos().toShortString()));
    }
    public record State(int integrity, int entropy, long[] milestones, int[] progress, int tracked, String echo) {
        static void encode(State s, FriendlyByteBuf b) { b.writeVarInt(s.integrity); b.writeVarInt(s.entropy); b.writeLongArray(s.milestones); b.writeVarIntArray(s.progress);b.writeVarInt(s.tracked); b.writeUtf(s.echo, 256); }
        static State decode(FriendlyByteBuf b) { return new State(b.readVarInt(), b.readVarInt(), b.readLongArray(null,2), b.readVarIntArray(96), b.readVarInt(), b.readUtf(256)); }
        static void handle(State s, Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> dev.yuni.ashenprotocol.client.ClientState.accept(s));
            context.get().setPacketHandled(true);
        }
    }
    public record Track(int id) {}
    public static void track(int id){if(id>=-1&&id<Progression.TITLES.length)CHANNEL.sendToServer(new Track(id));}
    private ProtocolNetwork() {}
}
