package com.freshlol.siftweapons.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
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
    private static final String KEY_KILLS = "sift_kills";

    public static final String TYPE_LEVELLED_MACE = "levelled_mace";
    public static final String TYPE_LEVELLED_SPEAR = "levelled_spear";
    public static final String TYPE_SIFT_MACE = "sift_mace";
    public static final String TYPE_GOD_MACE = "god_mace";
    public static final String TYPE_GOD_SPEAR = "god_spear";
    public static final String TYPE_GOD_CROSSBOW = "god_crossbow";
    public static final String TYPE_RAINBOW_MACE = "rainbow_mace";
    public static final String TYPE_ORBITAL_CANNON = "orbital_cannon";

    public static final int KILLS_DASH = 3;
    public static final int KILLS_STRENGTH = 5;
    public static final int KILLS_HASTE = 8;
    public static final int KILLS_SPEED = 12;
    public static final int KILLS_RESISTANCE = 16;
    public static final int KILLS_FIRE_RES = 20;
    public static final int KILLS_FULL_ENCHANTS = 25;
    public static final int KILLS_MAX = 25;

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

    public static int getKills(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        return data.copyTag().getInt(KEY_KILLS).orElse(0);
    }

    public static void setKills(ItemStack stack, int kills) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : new CompoundTag();
        tag.putInt(KEY_KILLS, Math.max(0, kills));
        if (!tag.contains(KEY_TYPE)) {
            tag.putString(KEY_TYPE, TYPE_SIFT_MACE);
            tag.putInt(KEY_LEVEL, 0);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int addSiftKill(ItemStack stack, ServerPlayer player) {
        int kills = getKills(stack) + 1;
        setKills(stack, kills);
        applySiftMaceLook(stack, kills);
        applySiftMaceEnchants(stack, player);
        return kills;
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

    public static void applyEnchants(ItemStack stack, int level, boolean isMace, ServerPlayer player) {
        if (isSiftMace(stack)) {
            applySiftMaceEnchants(stack, player);
            return;
        }
        if (isGodMace(stack)) {
            applyGodMaceEnchants(stack, player);
            return;
        }
        if (isGodSpear(stack)) {
            applyGodSpearEnchants(stack, player);
            return;
        }
        if (isGodCrossbow(stack)) {
            applyGodCrossbowEnchants(stack, player);
            return;
        }
        if (isOrbitalCannon(stack)) {
            applyOrbitalCannonEnchants(stack, player);
            return;
        }

        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
            stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
        );
        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);

        if (isMace) {
            if (level >= 2)  setEnchant(mutable, enchantRegistry, Enchantments.DENSITY, Math.min(5, 1 + (level - 2) / 2));
            if (level >= 3)  setEnchant(mutable, enchantRegistry, Enchantments.BREACH, Math.min(4, 1 + (level - 3) / 3));
            if (level >= 4)  setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, Math.min(3, 1 + (level - 4) / 3));
            if (level >= 6)  setEnchant(mutable, enchantRegistry, Enchantments.WIND_BURST, Math.min(3, 1 + (level - 6) / 3));
            if (level >= 8)  setEnchant(mutable, enchantRegistry, Enchantments.FIRE_ASPECT, Math.min(2, 1 + (level - 8) / 4));
            if (level >= 12) setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        } else {
            if (level >= 2)  setEnchant(mutable, enchantRegistry, Enchantments.LUNGE, Math.min(3, 1 + (level - 2) / 3));
            if (level >= 3)  setEnchant(mutable, enchantRegistry, Enchantments.SHARPNESS, Math.min(5, 1 + (level - 3) / 2));
            if (level >= 4)  setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, Math.min(3, 1 + (level - 4) / 3));
            if (level >= 6)  setEnchant(mutable, enchantRegistry, Enchantments.FIRE_ASPECT, Math.min(2, 1 + (level - 6) / 4));
            if (level >= 8)  setEnchant(mutable, enchantRegistry, Enchantments.LOOTING, Math.min(3, 1 + (level - 8) / 3));
            if (level >= 12) setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        }
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static void applySiftMaceEnchants(ItemStack stack, ServerPlayer player) {
        int kills = getKills(stack);
        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, kills >= 5 ? 3 : (kills >= 2 ? 2 : 1));

        if (kills >= 3)  setEnchant(mutable, enchantRegistry, Enchantments.SHARPNESS, kills >= KILLS_FULL_ENCHANTS ? 5 : (kills >= 15 ? 4 : 3));
        if (kills >= 5)  setEnchant(mutable, enchantRegistry, Enchantments.DENSITY, kills >= KILLS_FULL_ENCHANTS ? 5 : (kills >= 15 ? 4 : 3));
        if (kills >= 8)  setEnchant(mutable, enchantRegistry, Enchantments.BREACH, kills >= KILLS_FULL_ENCHANTS ? 4 : (kills >= 18 ? 3 : 2));
        if (kills >= 10) setEnchant(mutable, enchantRegistry, Enchantments.WIND_BURST, kills >= KILLS_FULL_ENCHANTS ? 2 : 1);
        if (kills >= 12) setEnchant(mutable, enchantRegistry, Enchantments.FIRE_ASPECT, kills >= KILLS_FULL_ENCHANTS ? 2 : 1);
        if (kills >= 20) setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);

        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static void applyGodMaceEnchants(ItemStack stack, ServerPlayer player) {
        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        setEnchant(mutable, enchantRegistry, Enchantments.WIND_BURST, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.DENSITY, 6);
        setEnchant(mutable, enchantRegistry, Enchantments.BREACH, 5);
        setEnchant(mutable, enchantRegistry, Enchantments.FIRE_ASPECT, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.SHARPNESS, 5);
        setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static void applyGodSpearEnchants(ItemStack stack, ServerPlayer player) {
        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        setEnchant(mutable, enchantRegistry, Enchantments.LUNGE, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.SHARPNESS, 5);
        setEnchant(mutable, enchantRegistry, Enchantments.FIRE_ASPECT, 2);
        setEnchant(mutable, enchantRegistry, Enchantments.LOOTING, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static void applyGodCrossbowEnchants(ItemStack stack, ServerPlayer player) {
        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        setEnchant(mutable, enchantRegistry, Enchantments.QUICK_CHARGE, 8);
        setEnchant(mutable, enchantRegistry, Enchantments.FLAME, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.MULTISHOT, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.PIERCING, 4);
        setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static void applyOrbitalCannonEnchants(ItemStack stack, ServerPlayer player) {
        RegistryAccess registry = player.level().registryAccess();
        var enchantRegistry = registry.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        setEnchant(mutable, enchantRegistry, Enchantments.QUICK_CHARGE, 5);
        setEnchant(mutable, enchantRegistry, Enchantments.PIERCING, 5);
        setEnchant(mutable, enchantRegistry, Enchantments.MULTISHOT, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.UNBREAKING, 3);
        setEnchant(mutable, enchantRegistry, Enchantments.MENDING, 1);
        setEnchant(mutable, enchantRegistry, Enchantments.VANISHING_CURSE, 1);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    private static void setEnchant(ItemEnchantments.Mutable mutable, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> registry, ResourceKey<Enchantment> key, int level) {
        registry.get(key).ifPresent(holder -> mutable.set(holder, level));
    }

    public static ItemStack createSiftMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_SIFT_MACE, 0);
        setKills(stack, 0);
        applySiftMaceLook(stack, 0);
        return stack;
    }

    public static void applySiftMaceLook(ItemStack stack, int kills) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sift Mace").withStyle(s -> s.withItalic(false).withColor(0x55FFFF).withBold(true)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Player kills: §f" + kills + "§8/" + KILLS_MAX));
        if (kills < KILLS_DASH) {
            lore.add(Component.literal("§cDash locked §7— need " + KILLS_DASH + " kills"));
        } else {
            lore.add(Component.literal("§bRight-click: Dash §a✓"));
        }
        lore.add(Component.literal(kills >= KILLS_STRENGTH ? "§aStrength II ✓" : "§8Strength @ " + KILLS_STRENGTH + " kills"));
        lore.add(Component.literal(kills >= KILLS_HASTE ? "§aHaste I ✓" : "§8Haste @ " + KILLS_HASTE + " kills"));
        lore.add(Component.literal(kills >= KILLS_SPEED ? "§aSpeed II ✓" : "§8Speed @ " + KILLS_SPEED + " kills"));
        lore.add(Component.literal(kills >= KILLS_RESISTANCE ? "§aResistance II ✓" : "§8Resistance @ " + KILLS_RESISTANCE + " kills"));
        lore.add(Component.literal(kills >= KILLS_FIRE_RES ? "§6Fire Resistance ✓" : "§8Fire Res @ " + KILLS_FIRE_RES + " kills"));
        lore.add(Component.literal(kills >= KILLS_FULL_ENCHANTS ? "§eMax enchants ✓" : "§8Full enchants @ " + KILLS_FULL_ENCHANTS + " kills"));
        lore.add(Component.literal("§8Limited: 2 on server"));
        stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    public static ItemStack createGodMace() {
        ItemStack stack = new ItemStack((ItemLike) Items.MACE);
        setTypeAndLevel(stack, TYPE_GOD_MACE, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Mace").withStyle(s -> s.withItalic(false).withColor(0xFF55FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§dRight-click: Massive Dash (no cooldown)"),
            Component.literal("§dPassive: Strength IV + Speed III + Resistance II + Haste + Fire Res"),
            Component.literal("§8Limited: 1 on server")
        )));
        return stack;
    }

    public static ItemStack createGodSpear() {
        ItemStack stack = new ItemStack((ItemLike) Items.DIAMOND_SPEAR);
        setTypeAndLevel(stack, TYPE_GOD_SPEAR, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Spear").withStyle(s -> s.withItalic(false).withColor(0xFFAA00).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§6Passive: Strength III + Speed II + Jump Boost II + Haste + Fire Res"),
            Component.literal("§6Hunger does not drain while held"),
            Component.literal("§8Limited: 1 on server")
        )));
        return stack;
    }

    public static ItemStack createGodCrossbow() {
        ItemStack stack = new ItemStack((ItemLike) Items.CROSSBOW);
        setTypeAndLevel(stack, TYPE_GOD_CROSSBOW, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("God Crossbow").withStyle(s -> s.withItalic(false).withColor(0xFF5555).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§cQuick Charge VIII · Flame · Multishot · Piercing IV"),
            Component.literal("§8Limited: 1 on server")
        )));
        return stack;
    }

    public static ItemStack createOrbitalCannon() {
        ItemStack stack = new ItemStack((ItemLike) Items.CROSSBOW);
        setTypeAndLevel(stack, TYPE_ORBITAL_CANNON, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Orbital Strike Cannon").withStyle(s -> s.withItalic(false).withColor(0xAA00FF).withBold(true)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§dRight-click: Stab Shot (hyper pierce)"),
            Component.literal("§5Look up + right-click: Orbital Strike"),
            Component.literal("§7Wemmbu-style orbital tech"),
            Component.literal("§8Craft: netherite + diamond blocks + totem + crossbow")
        )));
        return stack;
    }

    private static void applyLevelledLook(ItemStack stack, int level, boolean isMace) {
        int color = levelColor(level);
        String name;
        if (isMace && isRainbowTier(level)) {
            name = "§c§lR§6§la§e§li§a§ln§b§lb§d§lo§5§lw §f§lMace §7[Level " + level + "/14]";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false)));
        } else if (isMace) {
            name = "Level " + level + "/14 Mace";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(color)));
        } else {
            name = "Level " + level + "/14 " + getMaterialName(level) + " Spear";
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(color)));
        }

        String dashLine = !isMace
            ? (level >= 8 ? "§aHunger locked while held (Lvl 8+)" : "§8Hunger lock at Level 8")
            : (level <= 6 ? "§8Dash: unlocks at Level 7" : level <= 10 ? "§bDash: Medium" : "§cDash: Strong");

        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("§7Kill players to upgrade · Max 14"),
            Component.literal("§8Progress: " + level + "/14"),
            Component.literal(getAbilityDescription(level, isMace)),
            Component.literal(dashLine),
            Component.literal("§cCurse of Vanishing")
        )));
    }

    public static String getAbilityDescription(int level, boolean isMace) {
        if (level >= 13) return "§c§lMAX abilities";
        if (level >= 9) return "§eStrong abilities";
        if (level >= 7) return "§aDash unlocked";
        return "§8Keep killing players!";
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
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) tag = existing.copyTag();
        tag.putString(KEY_TYPE, type);
        tag.putInt(KEY_LEVEL, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isLevelled(ItemStack stack) {
        String type = getType(stack);
        return TYPE_LEVELLED_MACE.equals(type) || TYPE_LEVELLED_SPEAR.equals(type) || TYPE_RAINBOW_MACE.equals(type);
    }

    public static boolean isRainbowMace(ItemStack stack) { return TYPE_RAINBOW_MACE.equals(getType(stack)); }
    public static boolean isSiftMace(ItemStack stack) { return TYPE_SIFT_MACE.equals(getType(stack)); }
    public static boolean isGodMace(ItemStack stack) { return TYPE_GOD_MACE.equals(getType(stack)); }
    public static boolean isGodSpear(ItemStack stack) { return TYPE_GOD_SPEAR.equals(getType(stack)); }
    public static boolean isGodCrossbow(ItemStack stack) { return TYPE_GOD_CROSSBOW.equals(getType(stack)); }
    public static boolean isOrbitalCannon(ItemStack stack) { return TYPE_ORBITAL_CANNON.equals(getType(stack)); }
    public static boolean isGodWeapon(ItemStack stack) { return isGodMace(stack) || isGodSpear(stack) || isGodCrossbow(stack); }

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

    public static ItemStack upgradeToLevel(ItemStack old, int newLevel, ServerPlayer player) {
        boolean isMace = getType(old).contains("mace");
        newLevel = Math.max(1, Math.min(MAX_LEVEL, newLevel));
        ItemStack upgraded = createLevelled(newLevel, isMace);
        applyEnchants(upgraded, newLevel, isMace, player);
        if (old.has(DataComponents.DAMAGE)) {
            upgraded.set(DataComponents.DAMAGE, old.get(DataComponents.DAMAGE));
        }
        return upgraded;
    }

    public static ItemStack upgradeToLevel(ItemStack old, int newLevel) {
        boolean isMace = getType(old).contains("mace");
        return createLevelled(Math.max(1, Math.min(MAX_LEVEL, newLevel)), isMace);
    }
}
