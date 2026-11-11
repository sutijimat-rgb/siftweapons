package com.freshlol.siftweapons.util;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;

public class WeaponHelper {
    public static final int MAX_LEVEL = 14;
    private static final String KEY_TYPE = "sift_type";
    private static final String KEY_LEVEL = "sift_level";
    public static final String TYPE_LEVELLED_MACE = "levelled_mace";
    public static final String TYPE_LEVELLED_SPEAR = "levelled_spear";
    public static final String TYPE_SIFT_MACE = "sift_mace";
    public static final String TYPE_GOD_MACE = "god_mace";
    public static final String TYPE_GOD_SPEAR = "god_spear";
    public static final String TYPE_RAINBOW_MACE = "rainbow_mace";

    public static Item getSpearItemForLevel(int level) {
        level = Math.max(1, Math.min(14, level));
        return switch (level) {
            case 1 -> Items.WOODEN_SPEAR;
            case 2 -> Items.COPPER_SPEAR;
            case 3 -> Items.STONE_SPEAR;
            case 4 -> Items.IRON_SPEAR;
            case 5 -> Items.GOLDEN_SPEAR;
            case 6 -> Items.DIAMOND_SPEAR;
            default -> Items.NETHERITE_SPEAR;
        };
    }

    public static String getMaterialName(int level) {
        return switch (Math.max(1, Math.min(14, level))) {
            case 1 -> "Wooden";
            case 2 -> "Copper";
            case 3 -> "Stone";
            case 4 -> "Iron";
            case 5 -> "Golden";
            case 6 -> "Diamond";
            default -> "Netherite";
        };
    }

    public static boolean isRainbowTier(int level) {
        return level >= 10;
    }

    public static ItemStack createLevelled(int level, boolean isMace) {
        level = Math.max(1, Math.min(14, level));
        ItemStack stack;
        if (isMace) {
            stack = new ItemStack((ItemLike) Items.MACE);
            setTypeAndLevel(stack, isRainbowTier(level) ? TYPE_RAINBOW_MACE : TYPE_LEVELLED_MACE, level);
        } else {
            stack = new ItemStack((ItemLike) getSpearItemForLevel(level));
            setTypeAndLevel(stack, TYPE_LEVELLED_SPEAR, level);
        }
        applyLevelledLook(stack, level, isMace);
        return stack;
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_SIFT_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace").withStyle(s -> s.withItalic(false).withColor(0x55FFFF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§bRight-click: Dash"), Component.literal("§7Passive: Strength III + Speed III"), Component.literal("§8Limited: 2 on server"))));
        return stack;
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_GOD_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace").withStyle(s -> s.withItalic(false).withColor(0xFF55FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§dRight-click: Power Dash"), Component.literal("§dPassive: Strength IV + Speed III + Resistance II"), Component.literal("§dOn kill: Shockwave knockback"), Component.literal("§8Limited: 1 on server"))));
        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack((ItemLike) Items.DIAMOND_SPEAR);
        setTypeAndLevel(stack, TYPE_GOD_SPEAR, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear").withStyle(s -> s.withItalic(false).withColor(0xFFAA00).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§6Passive: Strength III + Speed II + Jump Boost II"), Component.literal("§6On kill: Lightning strike + power surge"), Component.literal("§8Limited: 1 on server"))));
        return stack;
    }

    private static void applyLevelledLook(ItemStack stack, int level, boolean isMace) {
        int color = levelColor(level);
        String name;
        if (isMace && isRainbowTier(level)) {
            name = "§c§lR§6§la§e§li§a§ln§b§lb§d§lo§5§lw §f§lMace §7(Level " + level + ")";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false)));
        } else if (isMace) {
            name = "Level " + level + " Mace";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(color)));
        } else {
            name = "Level " + level + " " + getMaterialName(level) + " Spear";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(color)));
        }
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§7Kill players to upgrade · Max 14"),
            Component.literal("§8Progress: " + level + "/14"),
            Component.literal(getAbilityDescription(level, isMace))
        )));
    }

    public static String getAbilityDescription(int level, boolean isMace) {
        if (isMace && level >= 10) {
            if (level >= 13) return "§c§lRAINBOW MAX: Strength III + Speed II + Resistance I + Regen + Absorption";
            return "§d§lRAINBOW: Strength II + Speed II + Resistance I + Regen";
        }
        if (level >= 13) return "§c§lMAX POWER: Strength III + Speed II + Resistance I + Regeneration";
        if (level >= 11) return "§6High: Strength II + Speed II + Resistance I";
        if (level >= 9) return "§eStrong: Strength II + Speed I";
        if (level >= 7) return "§aNetherite: Strength I + Speed I";
        if (level >= 5) return "§bGrowing: Strength I";
        if (level >= 3) return "§7Developing power...";
        return "§8Basic · Keep killing players!";
    }

    private static int levelColor(int level) {
        if (level >= 12) return 0xFF5555;
        if (level >= 9) return 0xFFAA00;
        if (level >= 6) return 0x55FF55;
        if (level >= 3) return 0x55FFFF;
        return 0xAAAAAA;
    }

    private static void setTypeAndLevel(ItemStack stack, String type, int level) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_TYPE, type);
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isLevelled(ItemStack stack) {
        String type = getType(stack);
        return TYPE_LEVELLED_MACE.equals(type) || TYPE_LEVELLED_SPEAR.equals(type) || TYPE_RAINBOW_MACE.equals(type);
    }

    public static boolean isRainbowMace(ItemStack stack) {
        return TYPE_RAINBOW_MACE.equals(getType(stack));
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
        return data.copyTag().getInt(KEY_LEVEL).orElse(0);
    }

    public static String getType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        return data.copyTag().getString(KEY_TYPE).orElse("");
    }

    public static ItemStack upgradeToLevel(ItemStack old, int newLevel) {
        boolean isMace = getType(old).contains("mace");
        ItemStack upgraded = createLevelled(newLevel, isMace);
        if (old.has(DataComponents.ENCHANTMENTS)) {
            upgraded.set(DataComponents.ENCHANTMENTS, old.get(DataComponents.ENCHANTMENTS));
        }
        if (old.has(DataComponents.DAMAGE)) {
            upgraded.set(DataComponents.DAMAGE, old.get(DataComponents.DAMAGE));
        }
        return upgraded;
    }
}
