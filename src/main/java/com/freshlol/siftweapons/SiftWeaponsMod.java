package com.freshlol.siftweapons;

import com.freshlol.siftweapons.command.SiftCommand;
import com.freshlol.siftweapons.event.CraftSiftMaceHandler;
import com.freshlol.siftweapons.event.KillUpgradeHandler;
import com.freshlol.siftweapons.event.OrbitalCannonHandler;
import com.freshlol.siftweapons.event.PlayerJoinHandler;
import com.freshlol.siftweapons.event.PlayerRespawnHandler;
import com.freshlol.siftweapons.event.SiftMaceHandler;
import com.freshlol.siftweapons.event.WindChargeResetHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiftWeaponsMod implements ModInitializer {
    public static final String MOD_ID = "siftweapons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Sift Weapons loading (26.3)");

        ServerPlayConnectionEvents.JOIN.register(PlayerJoinHandler::onJoin);
        ServerPlayerEvents.AFTER_RESPAWN.register(PlayerRespawnHandler::onRespawn);
        ServerLivingEntityEvents.AFTER_DEATH.register(KillUpgradeHandler::onDeath);

        SiftMaceHandler.register();
        WindChargeResetHandler.register();
        CraftSiftMaceHandler.register();
        OrbitalCannonHandler.register();

        CommandRegistrationCallback.EVENT.register(SiftCommand::register);

        LOGGER.info("Sift Weapons ready.");
    }
}
