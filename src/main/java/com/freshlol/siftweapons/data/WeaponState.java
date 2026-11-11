package com.freshlol.siftweapons.data;

import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-wide limits for special weapons.
 * Simple in-memory for reliability across Minecraft 26.3 API changes.
 * Counts reset on server restart (can be upgraded to PersistentState later).
 */
public class WeaponState {
    public static final int MAX_SIFT_MACE = 2;
    public static final int MAX_GOD_MACE = 1;
    public static final int MAX_GOD_SPEAR = 1;

    private static final Map<UUID, Boolean> firstJoinGiven = new HashMap<>();

    private int siftMaceCount = 0;
    private int godMaceCount = 0;
    private int godSpearCount = 0;

    private static WeaponState INSTANCE = new WeaponState();

    public static WeaponState get(MinecraftServer server) {
        return INSTANCE;
    }

    public boolean canCreateSiftMace() {
        return siftMaceCount < MAX_SIFT_MACE;
    }

    public void incrementSiftMace() {
        siftMaceCount++;
    }

    public int getSiftMaceCount() {
        return siftMaceCount;
    }

    public boolean canCreateGodMace() {
        return godMaceCount < MAX_GOD_MACE;
    }

    public void incrementGodMace() {
        godMaceCount++;
    }

    public int getGodMaceCount() {
        return godMaceCount;
    }

    public boolean canCreateGodSpear() {
        return godSpearCount < MAX_GOD_SPEAR;
    }

    public void incrementGodSpear() {
        godSpearCount++;
    }

    public int getGodSpearCount() {
        return godSpearCount;
    }

    public boolean hasReceivedStarter(UUID playerId) {
        return firstJoinGiven.getOrDefault(playerId, false);
    }

    public void markStarterGiven(UUID playerId) {
        firstJoinGiven.put(playerId, true);
    }
}
