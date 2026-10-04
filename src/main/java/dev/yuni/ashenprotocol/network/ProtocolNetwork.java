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
        new ResourceLocation(AshenProtocol.MOD_ID, "state"), () -> "2", "2"::equals, "2"::equals);
    public static void register() {
        CHANNEL.messageBuilder(State.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(State::encode).decoder(State::decode).consumerMainThread(State::handle).add();
    }
    public static void sync(ServerPlayer player) {
        var data = ProtocolSavedData.get(player.getServer());
        var echo = data.getEcho(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new State(data.getIntegrity(), data.getEntropy(),
            Progression.bits(player), echo == null ? "" : echo.dimension() + " / " + echo.pos().toShortString()));
    }
    public record State(int integrity, int entropy, long[] milestones, String echo) {
        static void encode(State s, FriendlyByteBuf b) { b.writeVarInt(s.integrity); b.writeVarInt(s.entropy); b.writeLongArray(s.milestones); b.writeUtf(s.echo, 256); }
        static State decode(FriendlyByteBuf b) { return new State(b.readVarInt(), b.readVarInt(), b.readLongArray(null,2), b.readUtf(256)); }
        static void handle(State s, Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> dev.yuni.ashenprotocol.client.ClientState.accept(s));
            context.get().setPacketHandled(true);
        }
    }
    private ProtocolNetwork() {}
}
