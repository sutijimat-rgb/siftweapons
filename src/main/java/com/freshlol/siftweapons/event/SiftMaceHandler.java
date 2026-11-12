package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SiftMaceHandler {
    private static final Map<UUID, Long> lastDash = new HashMap<>();
    private static final long DASH_COOLDOWN_MS = 3000L;
    private static final long GOD_DASH_COOLDOWN_MS = 2000L;

    public static void register() {
        // Right-click dash for Sift / God Mace
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!WeaponHelper.isSpecialMace(stack)) {
                return InteractionResult.PASS;
            }
            boolean god = WeaponHelper.isGodMace(stack);
            long cooldown = god ? GOD_DASH_COOLDOWN_MS : DASH_COOLDOWN_MS;
            long now = System.currentTimeMillis();
            Long last = lastDash.get(player.getUUID());
            if (last != null && now - last < cooldown) {
                long remaining = (cooldown - (now - last) + 999L) / 1000L;
                serverPlayer.sendSystemMessage(Component.literal("§cDash on cooldown (" + remaining + "s)"));
                return InteractionResult.FAIL;
            }
            Vec3 look = player.getLookAngle();
            double power = god ? 2.8 : 1.6;
            double yBoost = god ? 0.55 : 0.35;
            player.setDeltaMovement(look.x * power, Math.max(yBoost, look.y * power * 0.55), look.z * power);
            player.hurtMarked = true; // force velocity sync
            lastDash.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal(god ? "§d§lGOD DASH!" : "§bDash!"));
            return InteractionResult.SUCCESS;
        });

        // Passive effects every tick while holding the weapon
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();

                // God / Sift specials (highest priority)
                if (WeaponHelper.isGodMace(main)) {
                    apply(player, MobEffects.STRENGTH, 3);
                    apply(player, MobEffects.SPEED, 2);
                    apply(player, MobEffects.RESISTANCE, 1);
                    apply(player, MobEffects.HASTE, 1);
                    continue;
                }
                if (WeaponHelper.isSiftMace(main)) {
                    apply(player, MobEffects.STRENGTH, 2);
                    apply(player, MobEffects.SPEED, 2);
                    apply(player, MobEffects.HASTE, 1);
                    continue;
                }
                if (WeaponHelper.isGodSpear(main)) {
                    apply(player, MobEffects.STRENGTH, 2);
                    apply(player, MobEffects.SPEED, 1);
                    apply(player, MobEffects.JUMP_BOOST, 1);
                    apply(player, MobEffects.HASTE, 1);
                    continue;
                }

                if (!WeaponHelper.isLevelled(main)) continue;

                int level = WeaponHelper.getLevel(main);
                boolean rainbow = WeaponHelper.isRainbowMace(main);

                // Freshlol-style progressive passives
                if (rainbow || level >= 13) {
                    apply(player, MobEffects.STRENGTH, 2);      // III
                    apply(player, MobEffects.SPEED, 1);         // II
                    apply(player, MobEffects.RESISTANCE, 0);    // I
                    apply(player, MobEffects.REGENERATION, 0);  // I
                    apply(player, MobEffects.HASTE, 1);         // II
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
                    apply(player, MobEffects.HASTE, 0); // permanent haste like Freshlol early upgrades
                }
            }
        });
    }

    private static void apply(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 40, amplifier, false, false, true));
    }
}
