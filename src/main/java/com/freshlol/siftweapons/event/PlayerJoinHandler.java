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
        if (state.hasReceivedStarter(player.getUUID())) {
            return;
        }
        ItemStack mace = WeaponHelper.createLevelled(1, true);
        ItemStack spear = WeaponHelper.createLevelled(1, false);
        WeaponHelper.applyEnchants(mace, 1, true, player);
        WeaponHelper.applyEnchants(spear, 1, false, player);
        player.getInventory().add(mace);
        player.getInventory().add(spear);
        state.markStarterGiven(player.getUUID());
        player.sendSystemMessage(Component.literal("§a[Sift Weapons] You received Level 1 Mace and Level 1 Wooden Spear!"));
        player.sendSystemMessage(Component.literal("§7Kill players to upgrade. Both have §cCurse of Vanishing§7."));
    }
}
