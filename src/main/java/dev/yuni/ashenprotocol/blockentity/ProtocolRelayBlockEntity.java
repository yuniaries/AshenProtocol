package dev.yuni.ashenprotocol.blockentity;

import dev.yuni.ashenprotocol.block.ProtocolRelayBlock;
import dev.yuni.ashenprotocol.config.APConfig;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ProtocolRelayBlockEntity extends BlockEntity {

    private int charge = 0;
    private int tickCounter = 0;

    public ProtocolRelayBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities.PROTOCOL_RELAY.get(),
                pos,
                state
        );
    }

    public void addCharge(int amount) {
        charge = Math.min(
                4000,
                charge + amount
        );

        setChanged();
    }

    public int getCharge() {
        return charge;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("Charge", charge);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        charge = Math.min(4000, Math.max(0, tag.getInt("Charge")));
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ProtocolRelayBlockEntity relay
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        relay.tickCounter++;

        boolean powered =
                level.hasNeighborSignal(pos);

        boolean active =
                powered && relay.charge > 0;

        if (state.getValue(ProtocolRelayBlock.ACTIVE) != active) {
            level.setBlock(
                    pos,
                    state.setValue(
                            ProtocolRelayBlock.ACTIVE,
                            active
                    ),
                    3
            );
        }

        if (!active) {
            return;
        }

        if (relay.tickCounter % 20 == 0) {
            relay.charge--;

            relay.setChanged();

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5,
                    pos.getY() + 1.15,
                    pos.getZ() + 0.5,
                    4,
                    0.18,
                    0.15,
                    0.18,
                    0.01
            );
        }

        if (relay.tickCounter % 100 == 0) {
            ProtocolSavedData data =
                    ProtocolSavedData.get(
                            serverLevel.getServer()
                    );

            data.addIntegrity(
                    APConfig.RELAY_INTEGRITY_GAIN.get()
            );

            data.addEntropy(
                    -APConfig.RELAY_ENTROPY_REDUCTION.get()
            );
        }
    }
}
