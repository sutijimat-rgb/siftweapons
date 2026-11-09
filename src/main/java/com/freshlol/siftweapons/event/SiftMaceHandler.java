package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SiftMaceHandler {

    public static void register() {
        UseItemCallback.EVENT.register(SiftMaceHandler::onUse);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack main = player.getMainHandItem();
                ItemStack off = player.getOffhandItem();

                boolean hasSpecial = WeaponHelper.isSiftMace(main) || WeaponHelper.isSiftMace(off)
                        || WeaponHelper.isGodMace(main) || WeaponHelper.isGodMace(off);

                if (hasSpecial) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 2, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, true, false, true));
                }
            }
        });
    }

    private static InteractionResult onUse(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide()) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (!WeaponHelper.isSiftMace(stack) && !WeaponHelper.isGodMace(stack)) {
            return InteractionResult.PASS;
        }

        Vec3 look = player.getLookAngle();
        double power = WeaponHelper.isGodMace(stack) ? 2.2 : 1.6;
        player.setDeltaMovement(look.scale(power));

        player.getCooldowns().addCooldown(stack.getItem(), 15);

        return InteractionResult.SUCCESS;
    }
}
