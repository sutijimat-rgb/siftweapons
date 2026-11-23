package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import com.freshlol.siftweapons.util.WeaponHelperRods;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CraftSiftMaceHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                scanInventory(player);
            }
        });
    }

    private static void scanInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(Items.MACE) && WeaponHelper.isSiftMace(stack)) {
                int kills = WeaponHelper.getKills(stack);
                WeaponHelper.applySiftMaceLook(stack, kills);
                if (!stack.isEnchanted() || kills > 0) {
                    WeaponHelper.applySiftMaceEnchants(stack, player);
                }
            }

            if (stack.is(Items.FISHING_ROD) && (WeaponHelper.isStabRod(stack) || WeaponHelper.isNukeRod(stack))) {
                WeaponHelperRods.refreshRodLook(stack);
            }
        }
    }
}
