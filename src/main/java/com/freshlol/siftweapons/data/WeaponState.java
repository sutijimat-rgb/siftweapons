package com.freshlol.siftweapons.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

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
        return this.siftMaceCount < 2;
    }

    public void incrementSiftMace() {
        ++this.siftMaceCount;
    }

    public int getSiftMaceCount() {
        return this.siftMaceCount;
    }

    public boolean canCreateGodMace() {
        return this.godMaceCount < 1;
    }

    public void incrementGodMace() {
        ++this.godMaceCount;
    }

    public int getGodMaceCount() {
        return this.godMaceCount;
    }

    public boolean canCreateGodSpear() {
        return this.godSpearCount < 1;
    }

    public void incrementGodSpear() {
        ++this.godSpearCount;
    }

    public int getGodSpearCount() {
        return this.godSpearCount;
    }

    public boolean hasReceivedStarter(UUID playerId) {
        return firstJoinGiven.getOrDefault(playerId, false);
    }

    public void markStarterGiven(UUID playerId) {
        firstJoinGiven.put(playerId, true);
    }

    public void resetAll() {
        this.siftMaceCount = 0;
        this.godMaceCount = 0;
        this.godSpearCount = 0;
        firstJoinGiven.clear();
    }

    public void resetCounts() {
        this.siftMaceCount = 0;
        this.godMaceCount = 0;
        this.godSpearCount = 0;
    }

    public void resetJoins() {
        firstJoinGiven.clear();
    }

    public void resetPlayer(UUID playerId) {
        firstJoinGiven.remove(playerId);
    }
}
