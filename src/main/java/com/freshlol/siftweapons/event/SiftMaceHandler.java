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

public class SiftMaceHandler {
    private static final Map<UUID, Long> lastDash = new HashMap<>();
    private static final long DASH_COOLDOWN_MS = 2500L;
    private static final long GOD_DASH_COOLDOWN_MS = 1500L;

    public static void register() {
        UseItemCallback.EVENT.register(SiftMaceHandler::onUseItem);
        AttackEntityCallback.EVENT.register(SiftMaceHandler::onAttack);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                applyPassivesFromInventory(player);
            }
        });
    }

    private static InteractionResult onUseItem(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!canUseAbility(stack)) {
            return InteractionResult.PASS;
        }

        boolean god = WeaponHelper.isGodMace(stack) || WeaponHelper.isGodSpear(stack);
        boolean sift = WeaponHelper.isSiftMace(stack);
        boolean rainbow = WeaponHelper.isRainbowMace(stack);
        int level = WeaponHelper.getLevel(stack);
        boolean isMace = WeaponHelper.getType(stack).contains("mace");

        // Levels 1-6: NO dash at all (levelled weapons only)
        if (!god && !sift && level > 0 && level <= 6) {
            return InteractionResult.PASS;
        }

        // Sift mace can always dash; God always; levelled only 7+
        if (!god && !sift && level < 7 && !rainbow) {
            return InteractionResult.PASS;
        }

        long cooldown = god ? GOD_DASH_COOLDOWN_MS : DASH_COOLDOWN_MS;
        long now = System.currentTimeMillis();
        Long last = lastDash.get(player.getUUID());
        if (last != null && now - last < cooldown) {
            long remaining = (cooldown - (now - last) + 999L) / 1000L;
            serverPlayer.sendSystemMessage(Component.literal("§cAbility on cooldown (" + remaining + "s)"), true);
            return InteractionResult.FAIL;
        }

        Vec3 look = player.getLookAngle().normalize();

        /*
         * Balanced vertical scale (approx height):
         *  7-8   ~6-8 blocks
         *  9-10  ~10-12 blocks
         *  11-12 ~14-16 blocks
         *  13-14 ~18-20 blocks
         *  God   ~28-32 blocks
         *  Sift  ~12-14 blocks
         */
        double power;
        double yBoost;
        String msg;

        if (god) {
            power = 4.2;
            yBoost = 5.2;
            msg = isMace ? "§d§lGOD DASH!" : "§6§lGOD LUNGE!";
        } else if (sift) {
            power = 2.8;
            yBoost = 2.2;
            msg = "§b§lSIFT DASH!";
        } else if (level >= 13) {
            power = 3.4;
            yBoost = 3.2;
            msg = isMace ? "§c§lMAX DASH!" : "§e§lMAX LUNGE!";
        } else if (level >= 11) {
            power = 3.0;
            yBoost = 2.6;
            msg = isMace ? "§cPower Dash!" : "§ePower Lunge!";
        } else if (level >= 9) {
            power = 2.5;
            yBoost = 2.0;
            msg = isMace ? "§bDash!" : "§bLunge!";
        } else {
            // level 7-8
            power = 2.0;
            yBoost = 1.4;
            msg = isMace ? "§7Dash" : "§7Lunge";
        }

        double vx = look.x * power;
        double vy = Math.max(yBoost, look.y * power + yBoost * 0.3);
        double vz = look.z * power;

        player.setDeltaMovement(vx, vy, vz);
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));

        lastDash.put(player.getUUID(), now);
        serverPlayer.sendSystemMessage(Component.literal(msg), true);
        return InteractionResult.SUCCESS;
    }

    private static boolean canUseAbility(ItemStack stack) {
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
        if (!canUseAbility(stack)) {
            return InteractionResult.PASS;
        }

        int level = WeaponHelper.getLevel(stack);
        boolean god = WeaponHelper.isGodMace(stack) || WeaponHelper.isGodSpear(stack);
        boolean rainbow = WeaponHelper.isRainbowMace(stack);
        boolean sift = WeaponHelper.isSiftMace(stack);

        if (god || rainbow || level >= 10 || sift) {
            if (target instanceof ServerPlayer victim && victim.isBlocking()) {
                ItemStack blocking = victim.getUseItem();
                if (!blocking.isEmpty()) {
                    victim.getCooldowns().addCooldown(blocking, 100);
                }
                victim.stopUsingItem();
                serverPlayer.sendSystemMessage(Component.literal("§c§lSHIELD DISABLED!"), true);
            }
        }

        if (god || rainbow || level >= 7) {
            int duration = god ? 60 : (rainbow || level >= 12 ? 40 : 25);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 0, false, true, true));
            if (god || level >= 12) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration / 2, 0, false, true, true));
            }
        }

        if (god || level >= 11) {
            Vec3 away = target.position().subtract(player.position()).normalize().scale(god ? 1.6 : 1.1);
            target.setDeltaMovement(target.getDeltaMovement().add(away.x, 0.35, away.z));
            if (target instanceof ServerPlayer tp) {
                tp.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
        }

        return InteractionResult.PASS;
    }

    private static void applyPassivesFromInventory(ServerPlayer player) {
        ItemStack best = ItemStack.EMPTY;
        int bestScore = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            int score = scoreWeapon(stack);
            if (score > bestScore) {
                bestScore = score;
                best = stack;
            }
        }

        if (bestScore < 0 || best.isEmpty()) return;

        if (WeaponHelper.isGodMace(best)) {
            apply(player, MobEffects.STRENGTH, 3);
            apply(player, MobEffects.SPEED, 2);
            apply(player, MobEffects.RESISTANCE, 1);
            apply(player, MobEffects.HASTE, 1);
            return;
        }
        if (WeaponHelper.isSiftMace(best)) {
            apply(player, MobEffects.STRENGTH, 2);
            apply(player, MobEffects.SPEED, 2);
            apply(player, MobEffects.HASTE, 1);
            return;
        }
        if (WeaponHelper.isGodSpear(best)) {
            apply(player, MobEffects.STRENGTH, 2);
            apply(player, MobEffects.SPEED, 1);
            apply(player, MobEffects.JUMP_BOOST, 1);
            apply(player, MobEffects.HASTE, 1);
            return;
        }

        if (!WeaponHelper.isLevelled(best)) return;

        int level = WeaponHelper.getLevel(best);
        boolean rainbow = WeaponHelper.isRainbowMace(best);

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

    private static int scoreWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;
        if (WeaponHelper.isGodMace(stack)) return 1000;
        if (WeaponHelper.isGodSpear(stack)) return 900;
        if (WeaponHelper.isSiftMace(stack)) return 800;
        if (WeaponHelper.isRainbowMace(stack)) return 500 + WeaponHelper.getLevel(stack);
        if (WeaponHelper.isLevelled(stack)) return WeaponHelper.getLevel(stack);
        return -1;
    }

    private static void apply(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 40, amplifier, false, false, true));
    }
}
