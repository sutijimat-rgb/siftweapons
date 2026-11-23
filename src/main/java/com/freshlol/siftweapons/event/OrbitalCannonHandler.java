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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Orbital Strike Cannon — Wemmbu-style stab shots (raycast, 26.3-safe).
 * RMB: hyper pierce stab line.
 * Look up + RMB: orbital strikes from above.
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

        // Look steeply up → orbital strike from sky
        if (pitch < -55.0f) {
            Long lastO = lastOrbital.get(player.getUUID());
            if (lastO != null && now - lastO < ORBITAL_COOLDOWN_MS) {
                long left = ORBITAL_COOLDOWN_MS - (now - lastO);
                serverPlayer.sendSystemMessage(Component.literal("§cOrbital on cooldown (" + String.format("%.1f", left / 1000.0) + "s)"), true);
                return InteractionResult.FAIL;
            }

            Vec3 look = player.getLookAngle();
            Vec3 focus = player.getEyePosition().add(look.scale(28.0));

            // 5 vertical stab columns around the focus point
            for (int i = 0; i < 5; i++) {
                double ox = (serverLevel.getRandom().nextDouble() - 0.5) * 4.0;
                double oz = (serverLevel.getRandom().nextDouble() - 0.5) * 4.0;
                Vec3 top = new Vec3(focus.x + ox, focus.y + 30.0, focus.z + oz);
                Vec3 bot = new Vec3(focus.x + ox, focus.y - 5.0, focus.z + oz);
                damageAlongLine(serverLevel, player, top, bot, 9.0f, 0.9);
            }

            lastOrbital.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal("§5§lORBITAL STRIKE!"), true);
            return InteractionResult.SUCCESS;
        }

        // Stab shot — pierce line along look
        Long lastS = lastShot.get(player.getUUID());
        if (lastS != null && now - lastS < SHOT_COOLDOWN_MS) {
            return InteractionResult.FAIL;
        }

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().normalize().scale(48.0));
        int hit = damageAlongLine(serverLevel, player, start, end, 11.0f, 0.55);

        lastShot.put(player.getUUID(), now);
        if (hit > 0) {
            serverPlayer.sendSystemMessage(Component.literal("§d§lSTAB SHOT! §7(" + hit + ")"), true);
        } else {
            serverPlayer.sendSystemMessage(Component.literal("§d§lSTAB SHOT!"), true);
        }
        return InteractionResult.SUCCESS;
    }

    private static int damageAlongLine(ServerLevel level, Player attacker, Vec3 from, Vec3 to, float damage, double radius) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.1) return 0;
        dir = dir.normalize();

        int hits = 0;
        java.util.Set<UUID> already = new java.util.HashSet<>();

        for (double d = 0.5; d < len; d += 0.45) {
            Vec3 pos = from.add(dir.scale(d));
            AABB box = new AABB(pos.x - radius, pos.y - radius, pos.z - radius,
                                pos.x + radius, pos.y + radius, pos.z + radius);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, ent -> ent.isAlive() && ent != attacker)) {
                if (already.contains(e.getUUID())) continue;
                already.add(e.getUUID());
                e.hurtServer(level, attacker.damageSources().playerAttack(attacker), damage);
                hits++;
            }
        }
        return hits;
    }
}
