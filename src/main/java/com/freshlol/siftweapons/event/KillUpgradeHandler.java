package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KillUpgradeHandler {
    public static void onDeath(LivingEntity entity, DamageSource source) {
        // Only care about player kills
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return;
        }

        ItemStack main = killer.getMainHandItem();
        if (!WeaponHelper.isLevelled(main)) {
            return;
        }

        int current = WeaponHelper.getLevel(main);
        if (current >= WeaponHelper.MAX_LEVEL) {
            return;
        }

        int next = current + 1;
        WeaponHelper.setLevel(main, next);

        killer.sendSystemMessage(Component.literal(
                "§b[Sift Weapons] Your " + (WeaponHelper.getType(main).contains("mace") ? "Mace" : "Spear") +
                " upgraded to Level " + next + "!"));
    }
}
