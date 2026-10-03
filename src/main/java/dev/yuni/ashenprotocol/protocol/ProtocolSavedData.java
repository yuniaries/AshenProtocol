package dev.yuni.ashenprotocol.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProtocolSavedData extends SavedData {

    private static final String DATA_NAME = "ashen_protocol_world";

    private int integrity = 0;
    private int entropy = 0;

    private final Map<UUID, EchoRecord> echoes = new HashMap<>();

    public ProtocolSavedData() {}

    public static ProtocolSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();

        return overworld.getDataStorage().computeIfAbsent(
                ProtocolSavedData::load,
                ProtocolSavedData::new,
                DATA_NAME
        );
    }

    public static ProtocolSavedData load(CompoundTag tag) {
        ProtocolSavedData data = new ProtocolSavedData();

        data.integrity = Mth.clamp(
                tag.getInt("Integrity"),
                0,
                10000
        );

        data.entropy = Mth.clamp(
                tag.getInt("Entropy"),
                0,
                10000
        );

        ListTag echoList = tag.getList(
                "Echoes",
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < echoList.size(); i++) {
            CompoundTag e = echoList.getCompound(i);

            if (!e.hasUUID("Player")) {
                continue;
            }

            UUID player = e.getUUID("Player");

            String dimension = e.getString("Dimension");

            BlockPos pos = new BlockPos(
                    e.getInt("X"),
                    e.getInt("Y"),
                    e.getInt("Z")
            );

            long time = e.getLong("Time");

            data.echoes.put(
                    player,
                    new EchoRecord(
                            dimension,
                            pos,
                            time
                    )
            );
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Integrity", integrity);
        tag.putInt("Entropy", entropy);

        ListTag echoList = new ListTag();

        echoes.forEach((uuid, record) -> {
            CompoundTag e = new CompoundTag();

            e.putUUID("Player", uuid);
            e.putString("Dimension", record.dimension());
            e.putInt("X", record.pos().getX());
            e.putInt("Y", record.pos().getY());
            e.putInt("Z", record.pos().getZ());
            e.putLong("Time", record.gameTime());

            echoList.add(e);
        });

        tag.put("Echoes", echoList);

        return tag;
    }

    public int getIntegrity() {
        return integrity;
    }

    public int getEntropy() {
        return entropy;
    }

    public ProtocolPhase getPhase() {
        return ProtocolPhase.from(integrity, entropy);
    }

    public void addIntegrity(int amount) {
        integrity = Mth.clamp(
                integrity + amount,
                0,
                10000
        );

        setDirty();
    }

    public void setIntegrity(int value) {
        integrity = Mth.clamp(
                value,
                0,
                10000
        );

        setDirty();
    }

    public void addEntropy(int amount) {
        entropy = Mth.clamp(
                entropy + amount,
                0,
                10000
        );

        setDirty();
    }

    public void setEntropy(int value) {
        entropy = Mth.clamp(
                value,
                0,
                10000
        );

        setDirty();
    }

    public void recordDeathEcho(ServerPlayer player) {
        ResourceLocation dimension =
                player.level()
                        .dimension()
                        .location();

        EchoRecord record = new EchoRecord(
                dimension.toString(),
                player.blockPosition(),
                player.level().getGameTime()
        );

        echoes.put(
                player.getUUID(),
                record
        );

        setDirty();
    }

    public EchoRecord getEcho(UUID player) {
        return echoes.get(player);
    }

    public void removeEcho(UUID player) {
        if (echoes.remove(player) != null) {
            setDirty();
        }
    }

    public record EchoRecord(
            String dimension,
            BlockPos pos,
            long gameTime
    ) {}
}
