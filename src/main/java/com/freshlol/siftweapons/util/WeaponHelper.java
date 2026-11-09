package com.freshlol.siftweapons.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class WeaponHelper {
    public static final String KEY_LEVEL = "sift_level";
    public static final String KEY_TYPE = "sift_type";
    public static final String KEY_SIFT = "sift_mace";
    public static final String KEY_GOD_MACE = "god_mace";
    public static final String KEY_GOD_SPEAR = "god_spear";

    public static final int MAX_LEVEL = 14;

    public static ItemStack createLevelled(int level, boolean isMace) {
        level = Math.max(1, Math.min(level, MAX_LEVEL));
        ItemStack stack = new ItemStack(isMace ? Items.MACE : Items.TRIDENT);

        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_LEVEL, level);
        tag.putString(KEY_TYPE, isMace ? "mace" : "spear");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        String name = (isMace ? "Sift Mace" : "Sift Spear") + " §7[Lv." + level + "]";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name)
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Level " + level + " / " + MAX_LEVEL).withStyle(ChatFormatting.GRAY),
                Component.literal("Get kills to upgrade!").withStyle(ChatFormatting.DARK_GRAY)
        )));

        return stack;
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack(Items.MACE);

        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_SIFT, true);
        tag.putInt(KEY_LEVEL, MAX_LEVEL);
        tag.putString(KEY_TYPE, "mace");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Limited Edition").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                Component.literal("Right-click: Powerful Dash").withStyle(ChatFormatting.AQUA),
                Component.literal("In inventory: Strength III + Speed III").withStyle(ChatFormatting.GREEN)
        )));

        return stack;
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack(Items.MACE);

        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_GOD_MACE, true);
        tag.putInt(KEY_LEVEL, MAX_LEVEL);
        tag.putString(KEY_TYPE, "mace");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("ONLY 1 EXISTS").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                Component.literal("Ultimate Freshlol power").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal("Max Level • God Tier").withStyle(ChatFormatting.YELLOW)
        )));

        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack(Items.TRIDENT);

        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_GOD_SPEAR, true);
        tag.putInt(KEY_LEVEL, MAX_LEVEL);
        tag.putString(KEY_TYPE, "spear");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("ONLY 1 EXISTS").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                Component.literal("Ultimate Freshlol power").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal("Max Level • God Tier").withStyle(ChatFormatting.YELLOW)
        )));

        return stack;
    }

    public static boolean isLevelled(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(KEY_TYPE);
    }

    public static boolean isSiftMace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBooleanOr(KEY_SIFT, false);
    }

    public static boolean isGodMace(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBooleanOr(KEY_GOD_MACE, false);
    }

    public static boolean isGodSpear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBooleanOr(KEY_GOD_SPEAR, false);
    }

    public static int getLevel(ItemStack stack) {
        if (!isLevelled(stack)) return 0;
        return stack.get(DataComponents.CUSTOM_DATA).copyTag().getIntOr(KEY_LEVEL, 1);
    }

    public static String getType(ItemStack stack) {
        if (!isLevelled(stack)) return "";
        return stack.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr(KEY_TYPE, "");
    }

    public static void setLevel(ItemStack stack, int level) {
        if (!isLevelled(stack)) return;
        level = Math.max(1, Math.min(level, MAX_LEVEL));
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        boolean isMace = "mace".equals(getType(stack));
        String name = (isMace ? "Sift Mace" : "Sift Spear") + " §7[Lv." + level + "]";
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name)
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
    }
}
