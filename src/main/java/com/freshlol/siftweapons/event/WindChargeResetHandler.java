package com.freshlol.siftweapons.event;

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
 * Wind Charge Reset:
 * Mid-air + looking clearly downward + throw wind charge → stall.
 * Uses pitch (XRot) so looking sideways/up never triggers.
 */
public class WindChargeResetHandler {

    /** Pitch degrees: 0 = horizontal, +90 = straight down. Need at least this much down. */
    private static final float MIN_DOWN_PITCH = 50.0f;

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

        // Must be airborne
        if (player.onGround() || player.isInWater() || player.onClimbable()) {
            return InteractionResult.PASS;
        }

        // Pitch > 0 means looking down. Require a clear downward aim.
        float pitch = player.getXRot();
        if (pitch < MIN_DOWN_PITCH) {
            return InteractionResult.PASS;
        }

        // Stall mid-air (only this case)
        player.setDeltaMovement(0.0, 0.06, 0.0);
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
        serverPlayer.sendSystemMessage(Component.literal("§b§lWIND CHARGE RESET"), true);

        // Still throw the wind charge normally
        return InteractionResult.PASS;
    }
}
