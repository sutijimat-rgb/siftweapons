package com.freshlol.siftweapons.util;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;

/** Stab / Nuke fishing rods (Wemmbu-style) */
public final class WeaponHelperRods {
    public static final String TYPE_STAB_ROD = "stab_rod";
    public static final String TYPE_NUKE_ROD = "nuke_rod";

    private WeaponHelperRods() {}

    public static boolean isStabRod(ItemStack stack) {
        return TYPE_STAB_ROD.equals(WeaponHelper.getType(stack));
    }

    public static boolean isNukeRod(ItemStack stack) {
        return TYPE_NUKE_ROD.equals(WeaponHelper.getType(stack));
    }

    public static ItemStack createStabRod(int level) {
        level = Math.max(1, Math.min(10, level));
        ItemStack stack = new ItemStack((ItemLike) Items.FISHING_ROD);
        setTypeLevel(stack, TYPE_STAB_ROD, level);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Stab Shot Rod §7[Lv " + level + "/10]")
            .withStyle(s -> s.withItalic(false).withColor(0xAA00FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§dAim at a block + Right-click"),
            Component.literal("§5Vertical TNT drill (Stab)"),
            Component.literal("§8Power scales 1→10 · Lv5 is huge"),
            Component.literal("§7Wemmbu-style orbital tech")
        )));
        return stack;
    }

    public static ItemStack createNukeRod(int level) {
        level = Math.max(1, Math.min(10, level));
        ItemStack stack = new ItemStack((ItemLike) Items.FISHING_ROD);
        setTypeLevel(stack, TYPE_NUKE_ROD, level);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Nuke Shot Rod §7[Lv " + level + "/10]")
            .withStyle(s -> s.withItalic(false).withColor(0xFF5555).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§cAim at a block + Right-click"),
            Component.literal("§6Ring TNT payload (Nuke)"),
            Component.literal("§8Power scales 1→10 · Lv5 is huge"),
            Component.literal("§7Wemmbu-style orbital tech")
        )));
        return stack;
    }

    public static void refreshRodLook(ItemStack stack) {
        int level = Math.max(1, WeaponHelper.getLevel(stack));
        if (isStabRod(stack)) {
            ItemStack proper = createStabRod(level);
            stack.set(DataComponents.CUSTOM_NAME, proper.get(DataComponents.CUSTOM_NAME));
            stack.set(DataComponents.LORE, proper.get(DataComponents.LORE));
        } else if (isNukeRod(stack)) {
            ItemStack proper = createNukeRod(level);
            stack.set(DataComponents.CUSTOM_NAME, proper.get(DataComponents.CUSTOM_NAME));
            stack.set(DataComponents.LORE, proper.get(DataComponents.LORE));
        }
    }

    private static void setTypeLevel(ItemStack stack, String type, int level) {
        CompoundTag tag = new CompoundTag();
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) tag = existing.copyTag();
        tag.putString("sift_type", type);
        tag.putInt("sift_level", level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
