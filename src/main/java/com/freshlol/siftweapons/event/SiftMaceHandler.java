package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Special abilities for Mace AND Spear (Freshlol style):
 * - Right-click: Dash / Lunge (works on levelled + special weapons)
 * - Hit: Stun / Shield disable at higher levels
 * - Passives: Strength, Speed, Haste, Resistance, Regen by level
 */
public class SiftMaceHandler {
    private static final Map<UUID, Long> lastDash = new HashMap<>();
    private static final long DASH_COOLDOWN_MS = 2500L;
    private static final long GOD_DASH_COOLDOWN_MS = 1500L;

    public static void register() {
        // RIGHT-CLICK = Dash / Lunge for mace + spear
        UseItemCallback.EVENT.register(SiftMaceHandler::onUseItem);

        // ON-HIT specials: stun + shield break
        AttackEntityCallback.EVENT.register(SiftMaceHandler::onAttack);

        // PASSIVE effects every tick while holding
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applyPassives(player);
            }
        });
    }

    private static InteractionResult onUseItem(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!canDash(stack)) {
            return InteractionResult.PASS;
        }

        boolean god = WeaponHelper.isGodMace(stack) || WeaponHelper.isGodSpear(stack);
        boolean sift = WeaponHelper.isSiftMace(stack);
        boolean rainbow = WeaponHelper.isRainbowMace(stack);
        int level = WeaponHelper.getLevel(stack);
        boolean isMace = WeaponHelper.getType(stack).contains("mace");

        long cooldown = god ? GOD_DASH_COOLDOWN_MS : DASH_COOLDOWN_MS;
        long now = System.currentTimeMillis();
        Long last = lastDash.get(player.getUUID());
        if (last != null && now - last < cooldown) {
            long remaining = (cooldown - (now - last) + 999L) / 1000L;
            serverPlayer.sendSystemMessage(Component.literal("§cAbility on cooldown (" + remaining + "s)"), true);
            return InteractionResult.FAIL;
        }

        Vec3 look = player.getLookAngle().normalize();

        // Power scales with tier
        double power;
        double yBoost;
        String msg;

        if (god) {
            power = 3.0;
            yBoost = 0.6;
            msg = isMace ? "§d§lGOD DASH!" : "§6§lGOD LUNGE!";
        } else if (sift || rainbow || level >= 12) {
            power = 2.2;
            yBoost = 0.45;
            msg = isMace ? "§c§lPOWER DASH!" : "§e§lPOWER LUNGE!";
        } else if (level >= 8) {
            power = 1.8;
            yBoost = 0.4;
            msg = isMace ? "§bDash!" : "§bLunge!";
        } else if (level >= 4 || sift) {
            power = 1.4;
            yBoost = 0.32;
            msg = isMace ? "§7Dash" : "§7Lunge";
        } else {
            // Level 1-3: small hop dash so early game still has ability
            power = 1.1;
            yBoost = 0.28;
            msg = isMace ? "§8Dash" : "§8Lunge";
        }

        // Set velocity
        double vx = look.x * power;
        double vy = Math.max(yBoost, look.y * power * 0.5);
        double vz = look.z * power;
        player.setDeltaMovement(vx, vy, vz);

        // FORCE client to see the dash (this was missing before)
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));

        lastDash.put(player.getUUID(), now);
        serverPlayer.sendSystemMessage(Component.literal(msg), true);
        return InteractionResult.SUCCESS;
    }

    private static boolean canDash(ItemStack stack) {
        return WeaponHelper.isLevelled(stack)
            || WeaponHelper.isSiftMace(stack)
            || WeaponHelper.isGodMace(stack)
            || WeaponHelper.isGodSpear(stack);
    }

    private static InteractionResult onAttack(Player player, Level world, InteractionHand hand, Entity entity, net.minecraft.world.phys.EntityHitResult hit) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (!(entity instanceof LivingEntity target)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!canDash(stack)) {
            return InteractionResult.PASS;
        }

        int level = WeaponHelper.getLevel(stack);
        boolean god = WeaponHelper.isGodMace(stack) || WeaponHelper.isGodSpear(stack);
        boolean rainbow = WeaponHelper.isRainbowMace(stack);
        boolean sift = WeaponHelper.isSiftMace(stack);

        // Shield disable (Freshlol "disable" ability) - high level / god / rainbow
        if (god || rainbow || level >= 10 || sift) {
            if (target instanceof ServerPlayer victim && victim.isBlocking()) {
                victim.getCooldowns().addCooldown(victim.getUseItem().getItem(), 100); // 5s shield cooldown
                victim.stopUsingItem();
                serverPlayer.sendSystemMessage(Component.literal("§c§lSHIELD DISABLED!"), true);
            }
        }

        // Stun on hit (slowness + mining fatigue) - mid+ levels
        if (god || rainbow || level >= 7) {
            int duration = god ? 60 : (rainbow || level >= 12 ? 40 : 25);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 0, false, true, true));
            if (god || level >= 12) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration / 2, 0, false, true, true));
            }
        }

        // God / high level knockback boost
        if (god || level >= 11) {
            Vec3 away = target.position().subtract(player.position()).normalize().scale(god ? 1.6 : 1.1);
            target.setDeltaMovement(target.getDeltaMovement().add(away.x, 0.35, away.z));
            if (target instanceof ServerPlayer tp) {
                tp.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }

        return InteractionResult.PASS; // don't cancel the attack
    }

    private static void applyPassives(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();

        if (WeaponHelper.isGodMace(main)) {
            apply(player, MobEffects.STRENGTH, 3);
            apply(player, MobEffects.SPEED, 2);
            apply(player, MobEffects.RESISTANCE, 1);
            apply(player, MobEffects.HASTE, 1);
            return;
        }
        if (WeaponHelper.isSiftMace(main)) {
            apply(player, MobEffects.STRENGTH, 2);
            apply(player, MobEffects.SPEED, 2);
            apply(player, MobEffects.HASTE, 1);
            return;
        }
        if (WeaponHelper.isGodSpear(main)) {
            apply(player, MobEffects.STRENGTH, 2);
            apply(player, MobEffects.SPEED, 1);
            apply(player, MobEffects.JUMP_BOOST, 1);
            apply(player, MobEffects.HASTE, 1);
            return;
        }

        if (!WeaponHelper.isLevelled(main)) return;

        int level = WeaponHelper.getLevel(main);
        boolean rainbow = WeaponHelper.isRainbowMace(main);

        if (rainbow || level >= 13) {
            apply(player, MobEffects.STRENGTH, 2);
            apply(player, MobEffects.SPEED, 1);
            apply(player, MobEffects.RESISTANCE, 0);
            apply(player, MobEffects.REGENERATION, 0);
            apply(player, MobEffects.HASTE, 1);
        } else if (level >= 11) {
            apply(player, MobEffects.STRENGTH, 1);
            apply(player, MobEffects.SPEED, 1);
            apply(player, MobEffects.RESISTANCE, 0);
            apply(player, MobEffects.HASTE, 0);
        } else if (level >= 9) {
            apply(player, MobEffects.STRENGTH, 1);
            apply(player, MobEffects.SPEED, 0);
            apply(player, MobEffects.HASTE, 0);
        } else if (level >= 7) {
            apply(player, MobEffects.STRENGTH, 0);
            apply(player, MobEffects.SPEED, 0);
            apply(player, MobEffects.HASTE, 0);
        } else if (level >= 5) {
            apply(player, MobEffects.STRENGTH, 0);
            apply(player, MobEffects.HASTE, 0);
        } else if (level >= 3) {
            apply(player, MobEffects.HASTE, 0);
        }
    }

    private static void apply(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 40, amplifier, false, false, true));
    }
}
