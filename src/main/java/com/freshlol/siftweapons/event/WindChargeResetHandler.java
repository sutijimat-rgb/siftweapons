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
import net.minecraft.world.phys.Vec3;

/**
 * Wind Charge Reset tech (26.3):
 * While mid-air, look down, throw a wind charge → stop / stall in place.
 * Lets mace/spear players reset momentum for smash setups.
 */
public class WindChargeResetHandler {

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
        if (player.onGround()) {
            return InteractionResult.PASS;
        }

        Vec3 look = player.getLookAngle();

        // Looking sufficiently downward
        if (look.y > -0.35) {
            return InteractionResult.PASS;
        }

        // Wind Charge Reset: kill momentum and stall mid-air
        // Slight upward nudge so you don't instantly fall through the stall
        player.setDeltaMovement(0.0, 0.08, 0.0);
        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));

        serverPlayer.sendSystemMessage(Component.literal("§b§lWIND CHARGE RESET"), true);

        // PASS so the wind charge is still thrown normally
        return InteractionResult.PASS;
    }
}
