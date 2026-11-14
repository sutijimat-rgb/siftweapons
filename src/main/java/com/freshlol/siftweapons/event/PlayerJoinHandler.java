package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PlayerJoinHandler {

    public static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.player;
        WeaponState state = WeaponState.get(server);

        // Already got starters this world — never give again
        if (state.hasReceivedStarter(player.getUUID())) {
            return;
        }

        boolean hasMace = hasAnyMace(player);
        boolean hasSpear = hasAnySpear(player);

        // Already holding both from a previous session / admin give — just mark and stop
        if (hasMace && hasSpear) {
            state.markStarterGiven(player.getUUID());
            return;
        }

        if (!hasMace) {
            ItemStack mace = WeaponHelper.createLevelled(1, true);
            WeaponHelper.applyEnchants(mace, 1, true, player);
            player.getInventory().add(mace);
        }
        if (!hasSpear) {
            ItemStack spear = WeaponHelper.createLevelled(1, false);
            WeaponHelper.applyEnchants(spear, 1, false, player);
            player.getInventory().add(spear);
        }

        state.markStarterGiven(player.getUUID());

        if (!hasMace || !hasSpear) {
            player.sendSystemMessage(Component.literal("§a[Sift Weapons] Starter weapons given (only what you were missing)."));
            player.sendSystemMessage(Component.literal("§7Kill players to upgrade. Both have §cCurse of Vanishing§7."));
        }
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
