package com.freshlol.siftweapons.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

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
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Level " + level + (isMace ? " Mace" : " Spear")).withStyle(style -> style.withItalic(false)));
        return stack;
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        setTypeAndLevel(stack, TYPE_SIFT_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace").withStyle(style -> style.withItalic(false).withColor(0x55FFFF)));
        return stack;
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        setTypeAndLevel(stack, TYPE_GOD_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace").withStyle(style -> style.withItalic(false).withColor(0xFF55FF)));
        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack(Items.TRIDENT);
        setTypeAndLevel(stack, TYPE_GOD_SPEAR, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear").withStyle(style -> style.withItalic(false).withColor(0xFFAA00)));
        return stack;
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

    public static int getLevel(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        CompoundTag tag = data.copyTag();
        if (tag.contains(KEY_LEVEL)) {
            try {
                return tag.getInt(KEY_LEVEL);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static String getType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        CompoundTag tag = data.copyTag();
        if (tag.contains(KEY_TYPE)) {
            try {
                return tag.getString(KEY_TYPE);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public static void setLevel(ItemStack stack, int level) {
        level = Math.max(1, Math.min(MAX_LEVEL, level));
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : new CompoundTag();
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        String currentType = "";
        if (tag.contains(KEY_TYPE)) {
            try {
                currentType = tag.getString(KEY_TYPE);
            } catch (Exception ignored) {}
        }
        boolean isMace = TYPE_LEVELLED_MACE.equals(currentType);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Level " + level + (isMace ? " Mace" : " Spear")).withStyle(style -> style.withItalic(false)));
    }
}
