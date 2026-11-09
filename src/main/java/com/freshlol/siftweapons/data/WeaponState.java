package com.freshlol.siftweapons.data;

import net.minecraft.server.MinecraftServer;

public class WeaponState {
    public static final int MAX_SIFT_MACE = 2;
    public static final int MAX_GOD_MACE = 1;
    public static final int MAX_GOD_SPEAR = 1;

    private static int siftMaceCount = 0;
    private static int godMaceCount = 0;
    private static int godSpearCount = 0;

    public static WeaponState get(MinecraftServer server) {
        return new WeaponState();
    }

    public boolean canCreateSiftMace() {
        return siftMaceCount < MAX_SIFT_MACE;
    }

    public void incrementSiftMace() {
        if (siftMaceCount < MAX_SIFT_MACE) {
            siftMaceCount++;
        }
    }

    public int getSiftMaceCount() {
        return siftMaceCount;
    }

    public boolean canCreateGodMace() {
        return godMaceCount < MAX_GOD_MACE;
    }

    public void incrementGodMace() {
        if (godMaceCount < MAX_GOD_MACE) {
            godMaceCount++;
        }
    }

    public int getGodMaceCount() {
        return godMaceCount;
    }

    public boolean canCreateGodSpear() {
        return godSpearCount < MAX_GOD_SPEAR;
    }

    public void incrementGodSpear() {
        if (godSpearCount < MAX_GOD_SPEAR) {
            godSpearCount++;
        }
    }

    public int getGodSpearCount() {
        return godSpearCount;
    }
}
