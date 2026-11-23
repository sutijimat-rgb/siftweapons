package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Orbital Strike Cannon — Wemmbu-style stab shots.
 * Right-click: fires a hyper-fast piercing stab arrow.
 * Look steeply up: orbital strike (arrow rains from above on look target).
 */
public class OrbitalCannonHandler {

    private static final long SHOT_COOLDOWN_MS = 600L;
    private static final long ORBITAL_COOLDOWN_MS = 3000L;
    private static final Map<UUID, Long> lastShot = new HashMap<>();
    private static final Map<UUID, Long> lastOrbital = new HashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register(OrbitalCannonHandler::onUse);
    }

    private static InteractionResult onUse(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!WeaponHelper.isOrbitalCannon(stack)) {
            return InteractionResult.PASS;
        }

        long now = System.currentTimeMillis();
        float pitch = player.getXRot();

        // Look steeply up → orbital strike
        if (pitch < -55.0f) {
            Long lastO = lastOrbital.get(player.getUUID());
            if (lastO != null && now - lastO < ORBITAL_COOLDOWN_MS) {
                long left = ORBITAL_COOLDOWN_MS - (now - lastO);
                serverPlayer.sendSystemMessage(Component.literal("§cOrbital on cooldown (" + (left / 1000.0) + "s)"), true);
                return InteractionResult.FAIL;
            }

            Vec3 look = player.getLookAngle();
            // Target point ~30 blocks along look
            Vec3 target = player.getEyePosition().add(look.scale(30.0));

            for (int i = 0; i < 5; i++) {
                double ox = (world.random.nextDouble() - 0.5) * 3.0;
                double oz = (world.random.nextDouble() - 0.5) * 3.0;
                Arrow arrow = new Arrow(serverLevel, target.x + ox, target.y + 25.0 + i * 2, target.z + oz, new ItemStack(Items.ARROW), stack);
                arrow.setOwner(player);
                arrow.setBaseDamage(6.0);
                arrow.setCritArrow(true);
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                // Stab downward
                arrow.setDeltaMovement(0.0, -2.8, 0.0);
                serverLevel.addFreshEntity(arrow);
            }

            lastOrbital.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal("§5§lORBITAL STRIKE!"), true);
            return InteractionResult.SUCCESS;
        }

        // Normal stab shot
        Long lastS = lastShot.get(player.getUUID());
        if (lastS != null && now - lastS < SHOT_COOLDOWN_MS) {
            return InteractionResult.FAIL;
        }

        Arrow arrow = new Arrow(serverLevel, player, new ItemStack(Items.ARROW), stack);
        arrow.setOwner(player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 4.8f, 0.0f);
        arrow.setBaseDamage(7.5);
        arrow.setCritArrow(true);
        arrow.setPierceLevel((byte) 4);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        serverLevel.addFreshEntity(arrow);

        lastShot.put(player.getUUID(), now);
        serverPlayer.sendSystemMessage(Component.literal("§d§lSTAB SHOT!"), true);
        return InteractionResult.SUCCESS;
    }
}
