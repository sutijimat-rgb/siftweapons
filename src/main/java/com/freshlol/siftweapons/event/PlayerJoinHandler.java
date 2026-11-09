package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PlayerJoinHandler {

    public static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.getPlayer();
        Inventory inv = player.getInventory();

        boolean hasMace = false;
        boolean hasSpear = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (WeaponHelper.isLevelled(stack)) {
                String type = WeaponHelper.getType(stack);
                if ("mace".equals(type)) hasMace = true;
                if ("spear".equals(type)) hasSpear = true;
            }
            if (WeaponHelper.isSiftMace(stack) || WeaponHelper.isGodMace(stack)) hasMace = true;
            if (WeaponHelper.isGodSpear(stack)) hasSpear = true;
        }

        boolean gaveSomething = false;

        if (!hasMace) {
            ItemStack mace = WeaponHelper.createLevelled(1, true);
            if (!inv.add(mace)) {
                // inventory full - just discard for now to avoid API issues
            }
            gaveSomething = true;
        }

        if (!hasSpear) {
            ItemStack spear = WeaponHelper.createLevelled(1, false);
            if (!inv.add(spear)) {
                // inventory full
            }
            gaveSomething = true;
        }

        if (gaveSomething) {
            player.sendSystemMessage(Component.literal("You received Level 1 Spear & Mace. Get kills to upgrade!").withStyle(ChatFormatting.GREEN));
        }
    }
}
