package dev.yuni.ashenprotocol.item;

import dev.yuni.ashenprotocol.config.APConfig;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import dev.yuni.ashenprotocol.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class PhasePistolItem extends Item {

    private static final double RANGE = 32.0D;

    public PhasePistolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            net.minecraft.world.entity.player.Player player,
            InteractionHand hand
    ) {
        ItemStack weapon = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(
                    weapon,
                    true
            );
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(weapon);
        }

        if (serverPlayer.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(weapon);
        }

        if (!serverPlayer.isCreative()
                && !consumeAmmo(serverPlayer)) {

            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS,
                    0.6F,
                    1.4F
            );

            return InteractionResultHolder.fail(weapon);
        }

        LivingEntity target =
                findTarget(serverPlayer, RANGE);

        if (target != null) {
            target.hurt(
                    serverPlayer
                            .damageSources()
                            .playerAttack(serverPlayer),
                    APConfig.PISTOL_DAMAGE
                            .get()
                            .floatValue()
            );

            ServerLevel serverLevel =
                    serverPlayer.serverLevel();

            Vec3 hitPos =
                    target.getBoundingBox()
                            .getCenter();

            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    hitPos.x,
                    hitPos.y,
                    hitPos.z,
                    12,
                    0.25,
                    0.35,
                    0.25,
                    0.03
            );
        }

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS,
                0.7F,
                1.9F
        );

        ProtocolSavedData data =
                ProtocolSavedData.get(
                        serverPlayer.getServer()
                );

        dev.yuni.ashenprotocol.progress.Progression.complete(serverPlayer, 3);
        data.addEntropy(
                APConfig.PISTOL_ENTROPY.get()
        );

        serverPlayer.getCooldowns()
                .addCooldown(this, 8);

        weapon.hurtAndBreak(
                1,
                serverPlayer,
                p -> p.broadcastBreakEvent(hand)
        );

        return InteractionResultHolder.consume(weapon);
    }

    private static boolean consumeAmmo(
            ServerPlayer player
    ) {
        for (int i = 0;
             i < player.getInventory().getContainerSize();
             i++) {

            ItemStack stack =
                    player.getInventory().getItem(i);

            if (stack.is(ModItems.PROTOCOL_FRAGMENT.get())) {
                stack.shrink(1);
                return true;
            }
        }

        return false;
    }

    private static LivingEntity findTarget(
            ServerPlayer player,
            double range
    ) {
        Vec3 start =
                player.getEyePosition();

        Vec3 direction =
                player.getViewVector(1.0F)
                        .normalize();

        Vec3 end =
                start.add(
                        direction.scale(range)
                );

        var obstruction = player.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (obstruction.getType() != HitResult.Type.MISS) {
            end = obstruction.getLocation();
        }

        AABB searchBox =
                player.getBoundingBox()
                        .expandTowards(
                                direction.scale(range)
                        )
                        .inflate(1.5D);

        List<LivingEntity> entities =
                player.level()
                        .getEntitiesOfClass(
                                LivingEntity.class,
                                searchBox,
                                entity ->
                                        entity != player
                                                && entity.isAlive()
                                                && !entity.isSpectator()
                        );

        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            AABB box =
                    entity.getBoundingBox()
                            .inflate(0.35D);

            Optional<Vec3> hit =
                    box.clip(start, end);

            if (hit.isEmpty()) {
                continue;
            }

            double distance =
                    start.distanceToSqr(hit.get());

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        return closest;
    }
}
