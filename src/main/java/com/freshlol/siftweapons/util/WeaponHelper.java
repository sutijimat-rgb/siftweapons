package com.freshlol.siftweapons.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class WeaponHelper {
    public static final int MAX_LEVEL = 14;

    private static final String KEY_TYPE = "sift_type";
    private static final String KEY_LEVEL = "sift_level";

    public static final String TYPE_LEVELLED_MACE = "levelled_mace";
    public static final String TYPE_LEVELLED_SPEAR = "levelled_spear";
    public static final String TYPE_SIFT_MACE = "sift_mace";
    public static final String TYPE_GOD_MACE = "god_mace";
    public static final String TYPE_GOD_SPEAR = "god_spear";

    public static ItemStack createLevelled(int level, boolean isMace) {
        level = Math.max(1, Math.min(MAX_LEVEL, level));
        ItemStack stack = new ItemStack(isMace ? Items.MACE : Items.TRIDENT);
        setTypeAndLevel(stack, isMace ? TYPE_LEVELLED_MACE : TYPE_LEVELLED_SPEAR, level);
        applyLevelledLook(stack, level, isMace);
        return stack;
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        setTypeAndLevel(stack, TYPE_SIFT_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace").withStyle(s -> s.withItalic(false).withColor(0x55FFFF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§bRight-click: Dash"),
                Component.literal("§7Passive: Strength III + Speed III"),
                Component.literal("§8Limited: 2 on server")
        )));
        return stack;
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        setTypeAndLevel(stack, TYPE_GOD_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace").withStyle(s -> s.withItalic(false).withColor(0xFF55FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§dRight-click: Power Dash"),
                Component.literal("§dPassive: Strength IV + Speed III + Resistance II"),
                Component.literal("§dOn kill: Shockwave knockback"),
                Component.literal("§8Limited: 1 on server")
        )));
        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack(Items.TRIDENT);
        setTypeAndLevel(stack, TYPE_GOD_SPEAR, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear").withStyle(s -> s.withItalic(false).withColor(0xFFAA00).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§6Passive: Strength III + Speed II + Jump Boost II"),
                Component.literal("§6On kill: Lightning strike on target"),
                Component.literal("§8Limited: 1 on server")
        )));
        return stack;
    }

    private static void applyLevelledLook(ItemStack stack, int level, boolean isMace) {
        int color = levelColor(level);
        String name = "Level " + level + (isMace ? " Mace" : " Spear");
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(color)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("§7Kill to upgrade · Max 14"),
                Component.literal("§8Progress: " + level + "/" + MAX_LEVEL)
        )));
    }

    private static int levelColor(int level) {
        if (level >= 12) return 0xFF5555; // red
        if (level >= 9) return 0xFFAA00;  // gold
        if (level >= 6) return 0x55FF55;  // green
        if (level >= 3) return 0x55FFFF;  // cyan
        return 0xAAAAAA; // gray
    }

    private static void setTypeAndLevel(ItemStack stack, String type, int level) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_TYPE, type);
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isLevelled(ItemStack stack) {
        String type = getType(stack);
        return TYPE_LEVELLED_MACE.equals(type) || TYPE_LEVELLED_SPEAR.equals(type);
    }

    public static boolean isSiftMace(ItemStack stack) {
        return TYPE_SIFT_MACE.equals(getType(stack));
    }

    public static boolean isGodMace(ItemStack stack) {
        return TYPE_GOD_MACE.equals(getType(stack));
    }

    public static boolean isGodSpear(ItemStack stack) {
        return TYPE_GOD_SPEAR.equals(getType(stack));
    }

    public static boolean isSpecialMace(ItemStack stack) {
        return isSiftMace(stack) || isGodMace(stack);
    }

    public static boolean isGodWeapon(ItemStack stack) {
        return isGodMace(stack) || isGodSpear(stack);
    }

    public static int getLevel(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        CompoundTag tag = data.copyTag();
        return tag.getInt(KEY_LEVEL).orElse(0);
    }

    public static String getType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        CompoundTag tag = data.copyTag();
        return tag.getString(KEY_TYPE).orElse("");
    }

    public static void setLevel(ItemStack stack, int level) {
        level = Math.max(1, Math.min(MAX_LEVEL, level));
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : new CompoundTag();
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        String currentType = tag.getString(KEY_TYPE).orElse("");
        boolean isMace = TYPE_LEVELLED_MACE.equals(currentType);
        applyLevelledLook(stack, level, isMace);
    }
}
