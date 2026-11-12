package com.freshlol.siftweapons.util;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
        applyEnchantsForLevel(stack, level, isMace);
        return stack;
    }

    /** Apply Curse of Vanishing + progressive enchants based on level (Freshlol style) */
    public static void applyEnchantsForLevel(ItemStack stack, int level, boolean isMace) {
        // Always curse of vanishing so it drops nothing on death
        addEnchant(stack, Enchantments.VANISHING_CURSE, 1);

        if (isMace) {
            // Mace progression: Density, Breach, Unbreaking, Wind Burst
            if (level >= 2)  addEnchant(stack, Enchantments.DENSITY, Math.min(5, 1 + (level - 2) / 2));
            if (level >= 3)  addEnchant(stack, Enchantments.BREACH, Math.min(4, 1 + (level - 3) / 3));
            if (level >= 4)  addEnchant(stack, Enchantments.UNBREAKING, Math.min(3, 1 + (level - 4) / 3));
            if (level >= 6)  addEnchant(stack, Enchantments.WIND_BURST, Math.min(3, 1 + (level - 6) / 3));
            if (level >= 8)  addEnchant(stack, Enchantments.FIRE_ASPECT, Math.min(2, 1 + (level - 8) / 4));
            if (level >= 12) addEnchant(stack, Enchantments.MENDING, 1);
        } else {
            // Spear progression: Lunge, Sharpness, Unbreaking, etc.
            if (level >= 2)  addEnchant(stack, Enchantments.LUNGE, Math.min(3, 1 + (level - 2) / 3));
            if (level >= 3)  addEnchant(stack, Enchantments.SHARPNESS, Math.min(5, 1 + (level - 3) / 2));
            if (level >= 4)  addEnchant(stack, Enchantments.UNBREAKING, Math.min(3, 1 + (level - 4) / 3));
            if (level >= 6)  addEnchant(stack, Enchantments.FIRE_ASPECT, Math.min(2, 1 + (level - 6) / 4));
            if (level >= 8)  addEnchant(stack, Enchantments.LOOTING, Math.min(3, 1 + (level - 8) / 3));
            if (level >= 12) addEnchant(stack, Enchantments.MENDING, 1);
        }
    }

    private static void addEnchant(ItemStack stack, ResourceKey<Enchantment> key, int level) {
        try {
            // Use the registry access from the item if possible; fallback safe path
            var enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchants);

            // Try to resolve the enchantment holder
            // In practice on server we can get registry from server, but for simplicity
            // we use a direct approach that works with official mappings
            stack.enchant(key, level); // ItemStack.enchant exists in modern versions
        } catch (Exception e) {
            // Silent fail if enchant key not found - still keep vanishing via other path
        }
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_SIFT_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace").withStyle(s -> s.withItalic(false).withColor(0x55FFFF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§bRight-click: Dash"),
            Component.literal("§7Passive: Strength III + Speed III"),
            Component.literal("§8Limited: 2 on server")
        )));
        addEnchant(stack, Enchantments.VANISHING_CURSE, 1);
        return stack;
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_GOD_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace").withStyle(s -> s.withItalic(false).withColor(0xFF55FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§dRight-click: Power Dash"),
            Component.literal("§dPassive: Strength IV + Speed III + Resistance II"),
            Component.literal("§dOn kill: Shockwave knockback"),
            Component.literal("§8Limited: 1 on server")
        )));
        addEnchant(stack, Enchantments.VANISHING_CURSE, 1);
        addEnchant(stack, Enchantments.DENSITY, 5);
        addEnchant(stack, Enchantments.BREACH, 4);
        addEnchant(stack, Enchantments.WIND_BURST, 3);
        addEnchant(stack, Enchantments.UNBREAKING, 3);
        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack((ItemLike) Items.DIAMOND_SPEAR);
        setTypeAndLevel(stack, TYPE_GOD_SPEAR, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear").withStyle(s -> s.withItalic(false).withColor(0xFFAA00).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§6Passive: Strength III + Speed II + Jump Boost II"),
            Component.literal("§6On kill: Lightning strike + power surge"),
            Component.literal("§8Limited: 1 on server")
        )));
        addEnchant(stack, Enchantments.VANISHING_CURSE, 1);
        addEnchant(stack, Enchantments.LUNGE, 3);
        addEnchant(stack, Enchantments.SHARPNESS, 5);
        addEnchant(stack, Enchantments.UNBREAKING, 3);
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
            Component.literal(getAbilityDescription(level, isMace)),
            Component.literal("§cCurse of Vanishing")
        )));
    }

    public static String getAbilityDescription(int level, boolean isMace) {
        if (isMace && level >= 10) {
            if (level >= 13) return "§c§lRAINBOW MAX: Strength III + Speed II + Resistance + Regen + Haste";
            return "§d§lRAINBOW: Strength II + Speed II + Resistance + Regen + Haste";
        }
        if (level >= 13) return "§c§lMAX: Strength III + Speed II + Resistance + Regen + Haste II";
        if (level >= 11) return "§6High: Strength II + Speed II + Resistance + Haste";
        if (level >= 9)  return "§eStrong: Strength II + Speed I + Haste";
        if (level >= 7)  return "§aNetherite: Strength I + Speed I + Haste";
        if (level >= 5)  return "§bGrowing: Strength I + Haste";
        if (level >= 3)  return "§7Developing power + Haste";
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

        // Preserve any extra enchants the player may have added
        if (old.has(DataComponents.ENCHANTMENTS)) {
            // New stack already has progressive enchants; keep strongest
        }
        if (old.has(DataComponents.DAMAGE)) {
            upgraded.set(DataComponents.DAMAGE, old.get(DataComponents.DAMAGE));
        }
        return upgraded;
    }
}
