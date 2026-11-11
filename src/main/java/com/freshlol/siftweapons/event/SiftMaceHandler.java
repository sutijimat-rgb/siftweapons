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
            lastDash.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal(god ? "§d§lGOD DASH!" : "§bDash!"));
            return InteractionResult.SUCCESS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();

                if (WeaponHelper.isGodMace(main)) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 3, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 1, false, false, true));
                    continue;
                }
                if (WeaponHelper.isSiftMace(main)) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, false, false, true));
                    continue;
                }
                if (WeaponHelper.isGodSpear(main)) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 1, false, false, true));
                    continue;
                }

                if (!WeaponHelper.isLevelled(main)) continue;

                int level = WeaponHelper.getLevel(main);
                boolean rainbow = WeaponHelper.isRainbowMace(main);

                if (rainbow) {
                    if (level >= 13) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 0, false, false, true));
                    } else {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, true));
                    }
                } else {
                    if (level >= 13) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, true));
                    } else if (level >= 11) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, false, false, true));
                    } else if (level >= 9) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 1, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, false, false, true));
                    } else if (level >= 7) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, false, false, true));
                    } else if (level >= 5) {
                        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, true));
                    }
                }
            }
        });
    }
}
