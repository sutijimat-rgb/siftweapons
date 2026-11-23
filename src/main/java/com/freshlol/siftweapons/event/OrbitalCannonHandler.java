package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Wemmbu-style Orbital rods (fishing rod):
 * Stab Shot = vertical TNT column
 * Nuke Shot = concentric TNT rings above target
 * Levels 1-10 (5 = huge, 10 = max)
 */
public class OrbitalCannonHandler {

    private static final Map<UUID, Long> lastFire = new HashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register(OrbitalCannonHandler::onUse);
    }

    private static InteractionResult onUse(Player player, Level world, InteractionHand hand) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        boolean stab = WeaponHelper.isStabRod(stack);
        boolean nuke = WeaponHelper.isNukeRod(stack);
        if (!stab && !nuke) return InteractionResult.PASS;

        int level = Math.max(1, Math.min(10, WeaponHelper.getLevel(stack)));

        // Cooldown scales with power (level 1 = 4s, level 5 = 8s, level 10 = 14s)
        long cd = 3000L + level * 1000L;
        long now = System.currentTimeMillis();
        Long last = lastFire.get(player.getUUID());
        if (last != null && now - last < cd) {
            double left = (cd - (now - last)) / 1000.0;
            serverPlayer.sendSystemMessage(Component.literal("§cRod cooldown: " + String.format("%.1f", left) + "s"), true);
            return InteractionResult.FAIL;
        }

        // Target: block player is looking at (up to 96 blocks)
        HitResult hit = player.pick(96.0, 0.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            serverPlayer.sendSystemMessage(Component.literal("§cAim at a block (max 96 blocks)"), true);
            return InteractionResult.FAIL;
        }

        BlockPos target = ((BlockHitResult) hit).getBlockPos();
        int tx = target.getX();
        int ty = target.getY();
        int tz = target.getZ();

        if (stab) {
            fireStab(serverLevel, serverPlayer, tx, ty, tz, level);
            serverPlayer.sendSystemMessage(Component.literal("§d§lSTAB SHOT §5Lv" + level + "§d!"), true);
        } else {
            fireNuke(serverLevel, serverPlayer, tx, ty, tz, level);
            serverPlayer.sendSystemMessage(Component.literal("§c§lNUKE SHOT §6Lv" + level + "§c!"), true);
        }

        lastFire.put(player.getUUID(), now);

        // Consume one use of durability feel — optional break chance none, just fire
        return InteractionResult.SUCCESS;
    }

    /** Vertical drill of TNT from sky down toward bedrock, stops at very resistant blocks */
    private static void fireStab(ServerLevel level, ServerPlayer owner, int x, int y, int z, int rodLevel) {
        // spacing: L1=4, L5=2, L10=1
        int spacing = Math.max(1, 5 - rodLevel / 2);
        // start height above target
        int topY = Math.min(level.getMaxY() - 2, y + 40 + rodLevel * 8);
        int bottomY = level.getMinY() + 1;

        // How many charges roughly by level (L1~15, L5~80, L10~160)
        int maxCharges = 10 + rodLevel * rodLevel * 1 + rodLevel * 6;
        if (rodLevel >= 5) maxCharges += 30;
        if (rodLevel >= 8) maxCharges += 40;

        int fuseBase = Math.max(20, 60 - rodLevel * 3); // higher level = faster detonation cascade
        int placed = 0;

        for (int cy = topY; cy >= bottomY && placed < maxCharges; cy -= spacing) {
            BlockPos pos = new BlockPos(x, cy, z);
            BlockState state = level.getBlockState(pos);
            // Stop at bedrock / barrier-like (very high resistance)
            float resist = state.getBlock().getExplosionResistance();
            if (resist >= 1200.0f) break;

            spawnTnt(level, owner, x + 0.5, cy + 0.5, z + 0.5, fuseBase + (placed % 8));
            placed++;
        }
    }

    /** Concentric rings of TNT above target — L5 is huge, L10 is massive */
    private static void fireNuke(ServerLevel level, ServerPlayer owner, int x, int y, int z, int rodLevel) {
        int rings = Math.min(10, 1 + rodLevel);           // L1=2, L5=6, L10=10
        int height = 48 + rodLevel * 4;                   // spawn height above target
        int fuse = 70 + rodLevel * 2;

        // center charge
        spawnTnt(level, owner, x + 0.5, y + height, z + 0.5, fuse);

        for (int r = 1; r <= rings; r++) {
            double radius = 3.0 + r * (2.0 + rodLevel * 0.25); // spreads more at high level
            // points on ring: more at higher level
            int points = 6 + r * 2 + rodLevel;
            if (rodLevel >= 5) points += 4;
            if (rodLevel >= 8) points += 6;

            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2 * i) / points + level.getRandom().nextDouble() * 0.15;
                double ox = Math.cos(angle) * radius + (level.getRandom().nextDouble() - 0.5) * 0.4;
                double oz = Math.sin(angle) * radius + (level.getRandom().nextDouble() - 0.5) * 0.4;
                double oy = height + (level.getRandom().nextDouble() - 0.5) * 2.0;
                spawnTnt(level, owner, x + 0.5 + ox, y + oy, z + 0.5 + oz, fuse + level.getRandom().nextInt(12));
            }
        }
    }

    private static void spawnTnt(ServerLevel level, ServerPlayer owner, double x, double y, double z, int fuse) {
        PrimedTnt tnt = new PrimedTnt(level, x, y, z, owner);
        tnt.setFuse(Math.max(10, fuse));
        level.addFreshEntity(tnt);
    }
}
