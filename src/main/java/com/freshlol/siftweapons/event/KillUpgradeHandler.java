package com.freshlol.siftweapons.event;

import com.freshlol.siftweapons.util.WeaponHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KillUpgradeHandler {
    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return;
        }

        ItemStack main = killer.getMainHandItem();

        // Levelled mace OR spear upgrade on kill
        if (WeaponHelper.isLevelled(main)) {
            int current = WeaponHelper.getLevel(main);
            if (current < WeaponHelper.MAX_LEVEL) {
                int next = current + 1;
                WeaponHelper.setLevel(main, next);
                boolean mace = WeaponHelper.getType(main).contains("mace");
                killer.sendSystemMessage(Component.literal(
                        "§b[Sift] Your Level " + current + (mace ? " Mace" : " Spear") +
                        " upgraded to §eLevel " + next + "§b!"));
                if (next == WeaponHelper.MAX_LEVEL) {
                    killer.sendSystemMessage(Component.literal("§6§lMAX LEVEL reached!"));
                }
            }
        }

        // God Mace: shockwave knockback nearby living entities
        if (WeaponHelper.isGodMace(main) && killer.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = entity.position();
            AABB box = new AABB(pos.x - 4, pos.y - 2, pos.z - 4, pos.x + 4, pos.y + 3, pos.z + 4);
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
                if (nearby == killer || nearby == entity || !nearby.isAlive()) continue;
                Vec3 away = nearby.position().subtract(pos).normalize().scale(1.4).add(0, 0.45, 0);
                nearby.setDeltaMovement(nearby.getDeltaMovement().add(away));
            }
            killer.sendSystemMessage(Component.literal("§dGod Mace shockwave!"));
        }

        // God Spear: brief Strength boost + glow the killer as feedback
        if (WeaponHelper.isGodSpear(main)) {
            killer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 3, false, true, true));
            killer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, true, true));
            killer.sendSystemMessage(Component.literal("§6God Spear power surge!"));
        }
    }
}
