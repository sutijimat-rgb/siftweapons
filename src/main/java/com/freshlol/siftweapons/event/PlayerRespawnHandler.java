package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerRespawnHandler {

    /**
     * On death+respawn weapons vanish (Curse of Vanishing),
     * so we always give fresh L1 mace + spear unless they somehow still have one
     * (e.g. keepInventory gamerule).
     */
    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        boolean hasMace = hasAnyMace(newPlayer);
        boolean hasSpear = hasAnySpear(newPlayer);

        if (hasMace && hasSpear) {
            return; // keepInventory or similar — don't duplicate
        }

        if (!hasMace) {
            ItemStack mace = WeaponHelper.createLevelled(1, true);
            WeaponHelper.applyEnchants(mace, 1, true, newPlayer);
            newPlayer.getInventory().add(mace);
        }
        if (!hasSpear) {
            ItemStack spear = WeaponHelper.createLevelled(1, false);
            WeaponHelper.applyEnchants(spear, 1, false, newPlayer);
            newPlayer.getInventory().add(spear);
        }

        newPlayer.sendSystemMessage(Component.literal("§a[Sift Weapons] You respawned with starter weapons (only what you were missing)."));
        newPlayer.sendSystemMessage(Component.literal("§7Both have §cCurse of Vanishing§7. Kill players to upgrade."));
    }

    private static boolean hasAnyMace(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (WeaponHelper.isLevelled(s) && WeaponHelper.getType(s).contains("mace")) return true;
            if (WeaponHelper.isSiftMace(s) || WeaponHelper.isGodMace(s) || WeaponHelper.isRainbowMace(s)) return true;
        }
        return false;
    }

    private static boolean hasAnySpear(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (WeaponHelper.isLevelled(s) && WeaponHelper.getType(s).contains("spear")) return true;
            if (WeaponHelper.isGodSpear(s)) return true;
        }
        return false;
    }
}
