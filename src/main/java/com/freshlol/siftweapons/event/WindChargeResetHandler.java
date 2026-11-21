package com.freshlol.siftweapons.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
 * Wind Charge Reset — very strong, look-down only.
 * Mid-air + pitch down + throw → hard momentum kill + solid pop.
 * Not looking down = normal wind charge, no reset.
 */
public class WindChargeResetHandler {

    /** Must look clearly down (50°+ toward ground) */
    private static final float MIN_DOWN_PITCH = 50.0f;
    private static final long RESET_COOLDOWN_MS = 1000L;

    private static final Map<UUID, Long> lastReset = new HashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register(WindChargeResetHandler::onUse);
    }

    private static InteractionResult onUse(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.WIND_CHARGE)) {
            return InteractionResult.PASS;
        }

        // Air only
        if (player.onGround() || player.isInWater() || player.onClimbable()) {
            return InteractionResult.PASS;
        }

        // STRICT: not looking down → no reset at all
        if (player.getXRot() < MIN_DOWN_PITCH) {
            return InteractionResult.PASS;
        }

        long now = System.currentTimeMillis();
        Long last = lastReset.get(player.getUUID());
        if (last != null && now - last < RESET_COOLDOWN_MS) {
            return InteractionResult.PASS;
        }

        // Very strong: full stop + strong upward pop (not infinite float)
        player.setDeltaMovement(0.0, 0.95, 0.0);
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        lastReset.put(player.getUUID(), now);
        serverPlayer.sendSystemMessage(Component.literal("§b§lWIND CHARGE RESET"), true);

        return InteractionResult.PASS;
    }
}
