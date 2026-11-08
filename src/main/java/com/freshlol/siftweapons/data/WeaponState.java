package com.freshlol.siftweapons.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WeaponState extends SavedData {
    public static final int MAX_SIFT_MACE = 2;
    public static final int MAX_GOD_MACE = 1;
    public static final int MAX_GOD_SPEAR = 1;

    private int siftMaceCount;
    private int godMaceCount;
    private int godSpearCount;

    public static final Codec<WeaponState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("siftMaceCount").forGetter(s -> s.siftMaceCount),
                    Codec.INT.fieldOf("godMaceCount").forGetter(s -> s.godMaceCount),
                    Codec.INT.fieldOf("godSpearCount").forGetter(s -> s.godSpearCount)
            ).apply(instance, WeaponState::new)
    );

    public static final SavedDataType<WeaponState> TYPE = new SavedDataType<>(
            "siftweapons_state",
            WeaponState::new,
            CODEC,
            null
    );

    public WeaponState() {
        this(0, 0, 0);
    }

    public WeaponState(int siftMaceCount, int godMaceCount, int godSpearCount) {
        this.siftMaceCount = siftMaceCount;
        this.godMaceCount = godMaceCount;
        this.godSpearCount = godSpearCount;
    }

    public static WeaponState get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean canCreateSiftMace() {
        return siftMaceCount < MAX_SIFT_MACE;
    }

    public void incrementSiftMace() {
        if (siftMaceCount < MAX_SIFT_MACE) {
            siftMaceCount++;
            setDirty();
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
            setDirty();
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
            setDirty();
        }
    }

    public int getGodSpearCount() {
        return godSpearCount;
    }
}
