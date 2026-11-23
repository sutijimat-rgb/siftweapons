package com.freshlol.siftweapons.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Wind Charge Reset: pure stop. Locks velocity for several ticks so
 * the wind charge explosion cannot boost the player up.
 */
public class WindChargeResetHandler {

    private static final float MIN_DOWN_PITCH = 50.0f;
    private static final long RESET_COOLDOWN_MS = 750L;
    /** Keep velocity at 0 for this many server ticks after reset (~0.6s) */
    private static final int LOCK_TICKS = 12;

    private static final Map<UUID, Long> lastReset = new HashMap<>();
    private static final Map<UUID, Integer> lockTicks = new HashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register(WindChargeResetHandler::onUse);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (lockTicks.isEmpty()) return;
            Iterator<Map.Entry<UUID, Integer>> it = lockTicks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> e = it.next();
                ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
                int left = e.getValue() - 1;
                if (p != null && left >= 0) {
                    // Kill any boost from wind charge explosion
                    p.setDeltaMovement(0.0, 0.0, 0.0);
                    p.connection.send(new ClientboundSetEntityMotionPacket(p));
                }
                if (left <= 0 || p == null) {
                    it.remove();
                } else {
                    e.setValue(left);
                }
            }
        });
    }

    private static InteractionResult onUse(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.WIND_CHARGE)) {
            return InteractionResult.PASS;
        }

        if (player.onGround() || player.isInWater() || player.onClimbable()) {
            return InteractionResult.PASS;
        }

        if (player.getXRot() < MIN_DOWN_PITCH) {
            return InteractionResult.PASS;
        }

        long now = System.currentTimeMillis();
        Long last = lastReset.get(player.getUUID());
        if (last != null && now - last < RESET_COOLDOWN_MS) {
            return InteractionResult.PASS;
        }

        player.setDeltaMovement(0.0, 0.0, 0.0);
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        lastReset.put(player.getUUID(), now);
        lockTicks.put(player.getUUID(), LOCK_TICKS);
        serverPlayer.sendSystemMessage(Component.literal("§b§lWIND CHARGE RESET"), true);

        return InteractionResult.PASS;
    }
}
