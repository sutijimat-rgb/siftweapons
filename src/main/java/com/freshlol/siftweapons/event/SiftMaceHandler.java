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
    private static final long DASH_COOLDOWN_MS = 3000; // 3 seconds

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

            long now = System.currentTimeMillis();
            Long last = lastDash.get(player.getUUID());
            if (last != null && now - last < DASH_COOLDOWN_MS) {
                long remaining = (DASH_COOLDOWN_MS - (now - last)) / 1000;
                serverPlayer.sendSystemMessage(Component.literal("§cDash on cooldown (" + remaining + "s)"));
                return InteractionResult.FAIL;
            }

            // Dash forward
            Vec3 look = player.getLookAngle();
            double power = WeaponHelper.isGodMace(stack) ? 2.2 : 1.6;
            player.setDeltaMovement(look.x * power, Math.max(0.35, look.y * power * 0.6), look.z * power);
            player.hurtMarked = true;

            lastDash.put(player.getUUID(), now);
            serverPlayer.sendSystemMessage(Component.literal("§bDash!"));
            return InteractionResult.SUCCESS;
        });

        // Passive Strength III + Speed III while holding special maces
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();
                if (WeaponHelper.isSpecialMace(main)) {
                    // Strength III (amplifier 2), Speed III (amplifier 2), short duration, no particles
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 2, false, false, true));
                }
            }
        });
    }
}
