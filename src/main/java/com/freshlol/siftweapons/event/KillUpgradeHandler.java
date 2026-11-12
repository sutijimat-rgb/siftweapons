package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KillUpgradeHandler {

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer victim)) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer killer)) {
            return;
        }

        ItemStack main = killer.getMainHandItem();

        if (WeaponHelper.isLevelled(main)) {
            int current = WeaponHelper.getLevel(main);
            if (current < WeaponHelper.MAX_LEVEL) {
                int next = current + 1;
                boolean isMace = WeaponHelper.getType(main).contains("mace");

                ItemStack upgraded = WeaponHelper.upgradeToLevel(main, next, killer);
                killer.setItemInHand(InteractionHand.MAIN_HAND, upgraded);

                if (isMace && next == 10) {
                    killer.sendSystemMessage(Component.literal("§c§l★ RAINBOW MACE UNLOCKED! ★"));
                    killer.sendSystemMessage(Component.literal("§dYour mace has transcended into a §cR§6a§ei§an§bb§do§5w §fMace§d!"));
                } else if (isMace && WeaponHelper.isRainbowTier(next)) {
                    killer.sendSystemMessage(Component.literal(
                        "§d[Sift] Your §cR§6a§ei§an§bb§do§5w §fMace §dupgraded to §eLevel " + next + "§d!"
                    ));
                } else {
                    String mat = isMace ? "" : (WeaponHelper.getMaterialName(next) + " ");
                    killer.sendSystemMessage(Component.literal(
                        "§b[Sift] Your Level " + current + (isMace ? " Mace" : " Spear") +
                        " upgraded to §eLevel " + next + " " + mat + (isMace ? "Mace" : "Spear") + "§b!"
                    ));
                }

                if (next == WeaponHelper.MAX_LEVEL) {
                    killer.sendSystemMessage(Component.literal("§6§lMAX LEVEL reached! Ultimate power unlocked!"));
                } else if (next == 2 && !isMace) {
                    killer.sendSystemMessage(Component.literal("§aYour wooden spear became a §6Copper Spear§a!"));
                } else if (next == 7 && !isMace) {
                    killer.sendSystemMessage(Component.literal("§5Your spear ascended to §dNetherite§5!"));
                }
            }
        }

        if (WeaponHelper.isGodMace(main) && killer.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = entity.position();
            AABB box = new AABB(pos.x - 4.0, pos.y - 2.0, pos.z - 4.0, pos.x + 4.0, pos.y + 3.0, pos.z + 4.0);
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
                if (nearby == killer || nearby == entity || !nearby.isAlive()) continue;
                Vec3 away = nearby.position().subtract(pos).normalize().scale(1.4).add(0.0, 0.45, 0.0);
                nearby.setDeltaMovement(nearby.getDeltaMovement().add(away));
            }
            killer.sendSystemMessage(Component.literal("§dGod Mace shockwave!"));
        }

        if (WeaponHelper.isGodSpear(main)) {
            killer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 80, 3, false, true, true));
            killer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, true, true));
            killer.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, false, true, true));
            killer.sendSystemMessage(Component.literal("§6God Spear power surge!"));
        }

        if (WeaponHelper.isLevelled(main)) {
            int level = WeaponHelper.getLevel(main);
            if (level >= 10) {
                killer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, false, true, true));
                if (level >= 13) {
                    killer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1, false, true, true));
                }
            }
        }
    }
}
