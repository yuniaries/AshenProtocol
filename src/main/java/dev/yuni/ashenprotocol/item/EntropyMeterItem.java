package dev.yuni.ashenprotocol.item;

import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntropyMeterItem extends Item {

    public EntropyMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(hand);

        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer) {

            ProtocolSavedData data =
                    ProtocolSavedData.get(
                            serverPlayer.getServer()
                    );

            serverPlayer.sendSystemMessage(
                    Component
                            .literal("灰烬协议 // 世界状态")
                            .withStyle(ChatFormatting.AQUA)
            );

            serverPlayer.sendSystemMessage(
                    Component.literal(
                            "完整度："
                                    + data.getIntegrity()
                                    + " / 10000"
                    )
            );

            serverPlayer.sendSystemMessage(
                    Component
                            .literal(
                                    "熵债："
                                            + data.getEntropy()
                                            + " / 10000"
                            )
                            .withStyle(
                                    data.getEntropy() >= 4000
                                            ? ChatFormatting.RED
                                            : ChatFormatting.GRAY
                            )
            );

            serverPlayer.sendSystemMessage(
                    Component.literal(
                            "阶段："
                                    + data.getPhase().zh()
                    )
            );

            ProtocolSavedData.EchoRecord echo =
                    data.getEcho(
                            serverPlayer.getUUID()
                    );

            if (echo != null) {
                serverPlayer.sendSystemMessage(
                        Component
                                .literal(
                                        "因果回声："
                                                + echo.dimension() + " / " + echo.pos().toShortString()
                                )
                                .withStyle(
                                        ChatFormatting.LIGHT_PURPLE
                                )
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }
}
