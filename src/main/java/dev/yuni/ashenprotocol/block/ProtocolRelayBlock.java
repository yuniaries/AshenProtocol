package dev.yuni.ashenprotocol.block;

import dev.yuni.ashenprotocol.blockentity.ProtocolRelayBlockEntity;
import dev.yuni.ashenprotocol.registry.ModBlockEntities;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ProtocolRelayBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE =
            BooleanProperty.create("active");

    public ProtocolRelayBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(ACTIVE, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(ACTIVE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new ProtocolRelayBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!player.getItemInHand(hand)
                .is(ModItems.PROTOCOL_FRAGMENT.get())) {

            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity =
                    level.getBlockEntity(pos);

            if (blockEntity
                    instanceof ProtocolRelayBlockEntity relay) {

                relay.addCharge(200);

                if (!player.isCreative()) {
                    player.getItemInHand(hand)
                            .shrink(1);
                }

                level.playSound(
                        null,
                        pos,
                        SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.5F
                );
            }
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(
                type,
                ModBlockEntities.PROTOCOL_RELAY.get(),
                ProtocolRelayBlockEntity::serverTick
        );
    }
}
