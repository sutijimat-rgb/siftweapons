package com.freshlol.siftweapons.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WeaponHelper {
    public static final int MAX_LEVEL = 14;

    public static ItemStack createLevelled(int level, boolean isMace) {
        return new ItemStack(isMace ? Items.MACE : Items.TRIDENT);
    }

    public static ItemStack createSiftMace() {
        return new ItemStack(Items.MACE);
    }

    public static ItemStack createGodMace() {
        return new ItemStack(Items.MACE);
    }

    public static ItemStack createGodSpear() {
        return new ItemStack(Items.TRIDENT);
    }

    public static boolean isLevelled(ItemStack stack) { return false; }
    public static boolean isSiftMace(ItemStack stack) { return false; }
    public static boolean isGodMace(ItemStack stack) { return false; }
    public static boolean isGodSpear(ItemStack stack) { return false; }
    public static int getLevel(ItemStack stack) { return 1; }
    public static String getType(ItemStack stack) { return ""; }
    public static void setLevel(ItemStack stack, int level) {}
}
