package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerRespawnHandler {

    /**
     * Called after a player respawns. Gives them a fresh Level 1 Mace and Level 1 Wooden Spear.
     */
    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        // Give Level 1 Mace + Level 1 Wooden Spear every time they respawn after death
        ItemStack mace = WeaponHelper.createLevelled(1, true);
        ItemStack spear = WeaponHelper.createLevelled(1, false);

        newPlayer.getInventory().add(mace);
        newPlayer.getInventory().add(spear);

        newPlayer.sendSystemMessage(Component.literal("§a[Sift Weapons] You respawned with a Level 1 Mace and Level 1 Wooden Spear!"));
        newPlayer.sendSystemMessage(Component.literal("§7Kill players to upgrade them."));
    }
}
