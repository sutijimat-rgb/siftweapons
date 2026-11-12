package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerRespawnHandler {

    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        ItemStack mace = WeaponHelper.createLevelled(1, true);
        ItemStack spear = WeaponHelper.createLevelled(1, false);
        WeaponHelper.applyEnchants(mace, 1, true, newPlayer);
        WeaponHelper.applyEnchants(spear, 1, false, newPlayer);

        newPlayer.getInventory().add(mace);
        newPlayer.getInventory().add(spear);

        newPlayer.sendSystemMessage(Component.literal("§a[Sift Weapons] You respawned with a Level 1 Mace and Level 1 Wooden Spear!"));
        newPlayer.sendSystemMessage(Component.literal("§7Both have §cCurse of Vanishing§7. Kill players to upgrade."));
    }
}
