package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * After crafting, ensure Sift Mace has proper name/lore/enchants.
 * Also converts a freshly crafted marked mace and enforces the 2-limit.
 */
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
            if (stack.isEmpty() || !stack.is(Items.MACE)) continue;

            if (!WeaponHelper.isSiftMace(stack)) continue;

            // Refresh look/enchants based on kills (covers crafted items with only custom_data)
            int kills = WeaponHelper.getKills(stack);
            WeaponHelper.applySiftMaceLook(stack, kills);
            // Light enchants only if missing vanishing
            if (!stack.isEnchanted() || kills > 0) {
                WeaponHelper.applySiftMaceEnchants(stack, player);
            }
        }
    }
}
