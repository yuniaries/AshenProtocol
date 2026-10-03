package dev.yuni.ashenprotocol.event;

import dev.yuni.ashenprotocol.AshenProtocol;
import dev.yuni.ashenprotocol.command.ProtocolCommands;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = AshenProtocol.MOD_ID
)
public final class ProtocolEvents {

    private static int serverTickCounter = 0;

    @SubscribeEvent
    public static void onCommands(
            RegisterCommandsEvent event
    ) {
        ProtocolCommands.register(
                event.getDispatcher()
        );
    }

    @SubscribeEvent
    public static void onDeath(
            LivingDeathEvent event
    ) {
        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        MinecraftServer server =
                player.getServer();

        if (server == null) {
            return;
        }

        ProtocolSavedData data =
                ProtocolSavedData.get(server);

        data.recordDeathEcho(player);

        player.sendSystemMessage(
                Component
                        .literal("因果回声已记录。")
                        .withStyle(
                                ChatFormatting.LIGHT_PURPLE
                        )
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player
                instanceof ServerPlayer player)) {

            return;
        }

        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        MinecraftServer server =
                player.getServer();

        if (server == null) {
            return;
        }

        ProtocolSavedData data =
                ProtocolSavedData.get(server);

        ProtocolSavedData.EchoRecord echo =
                data.getEcho(player.getUUID());

        if (echo == null) {
            return;
        }

        String currentDimension =
                player.level()
                        .dimension()
                        .location()
                        .toString();

        if (!currentDimension.equals(echo.dimension())) {
            return;
        }

        if (player.blockPosition()
                .distSqr(echo.pos()) > 64.0D) {

            return;
        }

        ServerLevel level =
                player.serverLevel();

        ItemEntity item =
                new ItemEntity(
                        level,
                        echo.pos().getX() + 0.5,
                        echo.pos().getY() + 0.8,
                        echo.pos().getZ() + 0.5,
                        new ItemStack(
                                ModItems.ECHO_RESIDUE.get()
                        )
                );

        level.addFreshEntity(item);

        data.removeEcho(player.getUUID());

        player.sendSystemMessage(
                Component
                        .literal(
                                "你触碰到了自己留下的因果回声。"
                        )
                        .withStyle(
                                ChatFormatting.LIGHT_PURPLE
                        )
        );
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        serverTickCounter++;

        if (serverTickCounter < 200) {
            return;
        }

        serverTickCounter = 0;

        MinecraftServer server =
                event.getServer();

        ProtocolSavedData data =
                ProtocolSavedData.get(server);

        int entropy = data.getEntropy();

        if (entropy < 1000) {
            return;
        }

        for (ServerPlayer player :
                server.getPlayerList().getPlayers()) {

            if (!player.isAlive() || player.isSpectator()) continue;
            ServerLevel level =
                    player.serverLevel();

            if (entropy >= 2500
                    && level.random.nextFloat() < 0.18F) {

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.DARKNESS,
                                80,
                                0,
                                false,
                                false
                        )
                );

                player.sendSystemMessage(
                        Component
                                .literal(
                                        "信号出现短暂失真。"
                                )
                                .withStyle(
                                        ChatFormatting.DARK_PURPLE
                                )
                );
            }

            if (entropy >= 5000
                    && level.random.nextFloat() < 0.12F) {

                Endermite mite =
                        EntityType.ENDERMITE.create(level);

                if (mite != null) {
                    BlockPos pos =
                            player.blockPosition()
                                    .offset(
                                            level.random.nextInt(9) - 4,
                                            0,
                                            level.random.nextInt(9) - 4
                                    );

                    mite.moveTo(
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            0,
                            0
                    );

                    mite.setCustomName(
                            Component.literal(
                                    "协议残影"
                            )
                    );

                    level.addFreshEntity(mite);
                }
            }

            if (entropy >= 8000
                    && level.random.nextFloat() < 0.08F) {

                LightningBolt lightning =
                        EntityType.LIGHTNING_BOLT
                                .create(level);

                if (lightning != null) {
                    BlockPos pos =
                            player.blockPosition()
                                    .offset(
                                            level.random.nextInt(13) - 6,
                                            0,
                                            level.random.nextInt(13) - 6
                                    );

                    lightning.moveTo(
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    );

                    lightning.setVisualOnly(true);

                    level.addFreshEntity(lightning);
                }
            }
        }
    }

    private ProtocolEvents() {}
}
