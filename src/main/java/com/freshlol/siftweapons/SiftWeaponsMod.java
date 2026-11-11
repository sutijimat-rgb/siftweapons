package com.freshlol.siftweapons;

import com.freshlol.siftweapons.command.SiftCommand;
import com.freshlol.siftweapons.event.KillUpgradeHandler;
import com.freshlol.siftweapons.event.PlayerJoinHandler;
import com.freshlol.siftweapons.event.SiftMaceHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiftWeaponsMod implements ModInitializer {
    public static final String MOD_ID = "siftweapons";
    public static final Logger LOGGER = LoggerFactory.getLogger("siftweapons");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Sift Weapons (SERVER-ONLY) loading...");
        CommandRegistrationCallback.EVENT.register(SiftCommand::register);
        ServerPlayConnectionEvents.JOIN.register(PlayerJoinHandler::onJoin);
        ServerLivingEntityEvents.AFTER_DEATH.register(KillUpgradeHandler::onDeath);
        SiftMaceHandler.register();
        LOGGER.info("Sift Weapons initialized. Player kills only. Rainbow Mace at lvl 10+.");
    }
}
