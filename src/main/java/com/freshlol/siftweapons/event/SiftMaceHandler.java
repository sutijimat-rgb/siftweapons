package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SiftMaceHandler {
    private static final Map<UUID, Long> lastDash = new HashMap<>();
    private static final long DASH_COOLDOWN_MS = 3000;
    private static final long GOD_DASH_COOLDOWN_MS = 2000;

    public static void register() {
        // Right-click dash for Sift Mace / God Mace
        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
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
                long remaining = (cooldown - (now - last) + 999) / 1000;
                serverPlayer.sendSystemMessage(Component.literal("§cDash on cooldown (" + remaining + "s)"));
                return InteractionResult.FAIL;
            }

            Vec3 look = player.getLookAngle();
            double power = god ? 2.8 : 1.6;
            double yBoost = god ? 0.55 : 0.35;
            player.setDeltaMovement(look.x * power, Math.max(yBoost, look.y * power * 0.55), look.z * power);

            lastDash.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal(god ? "§d§lGOD DASH!" : "§bDash!"));
            return InteractionResult.SUCCESS;
        });

        // Passives while holding special weapons
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();

                if (WeaponHelper.isGodMace(main)) {
                    // God Mace: Strength IV, Speed III, Resistance II
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 3, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 1, false, false, true));
                } else if (WeaponHelper.isSiftMace(main)) {
                    // Sift Mace: Strength III, Speed III
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, false, false, true));
                } else if (WeaponHelper.isGodSpear(main)) {
                    // God Spear: Strength III, Speed II, Jump Boost II
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false, true));
                } else if (WeaponHelper.isLevelled(main)) {
                    // Levelled weapons: small bonus scaling with level
                    int level = WeaponHelper.getLevel(main);
                    if (level >= 7) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, true));
                    }
                    if (level >= 11) {
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, false, false, true));
                    }
                }
            }
        });
    }
}
