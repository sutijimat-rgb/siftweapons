package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class KillUpgradeHandler {

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        ItemStack held = player.getMainHandItem();
        if (!WeaponHelper.isLevelled(held)) return;

        if (WeaponHelper.isSiftMace(held) || WeaponHelper.isGodMace(held) || WeaponHelper.isGodSpear(held)) {
            return;
        }

        int current = WeaponHelper.getLevel(held);
        if (current >= WeaponHelper.MAX_LEVEL) {
            player.sendSystemMessage(Component.literal("Weapon is already max level (14)!").withStyle(ChatFormatting.GOLD));
            return;
        }

        int next = current + 1;
        WeaponHelper.setLevel(held, next);

        player.sendSystemMessage(Component.literal("Weapon upgraded to Level " + next + "!").withStyle(ChatFormatting.AQUA));

        if (next == 5) {
            player.sendSystemMessage(Component.literal("Mid-tier abilities unlocked!").withStyle(ChatFormatting.GREEN));
        } else if (next == 10) {
            player.sendSystemMessage(Component.literal("High-tier abilities unlocked!").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (next == 14) {
            player.sendSystemMessage(Component.literal("MAX LEVEL! Ready for God form.").withStyle(ChatFormatting.GOLD));
        }
    }
}
