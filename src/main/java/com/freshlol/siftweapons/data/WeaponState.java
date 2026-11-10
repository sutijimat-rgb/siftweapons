package com.freshlol.siftweapons.data;

import net.minecraft.server.MinecraftServer;

public class WeaponState {
    public static final int MAX_SIFT_MACE = 2;
    public static final int MAX_GOD_MACE = 1;
    public static final int MAX_GOD_SPEAR = 1;

    public static WeaponState get(MinecraftServer server) {
        return new WeaponState();
    }

    public boolean canCreateSiftMace() { return true; }
    public void incrementSiftMace() {}
    public int getSiftMaceCount() { return 0; }
    public boolean canCreateGodMace() { return true; }
    public void incrementGodMace() {}
    public int getGodMaceCount() { return 0; }
    public boolean canCreateGodSpear() { return true; }
    public void incrementGodSpear() {}
    public int getGodSpearCount() { return 0; }
}
