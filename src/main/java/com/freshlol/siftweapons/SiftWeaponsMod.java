package com.freshlol.siftweapons;

import com.freshlol.siftweapons.command.SiftCommand;
import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.event.KillUpgradeHandler;
import com.freshlol.siftweapons.event.PlayerJoinHandler;
import com.freshlol.siftweapons.event.SiftMaceHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiftWeaponsMod implements ModInitializer {
    public static final String MOD_ID = "siftweapons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Sift Weapons (SERVER-ONLY) v2.1.0 loading...");

        // Commands
        CommandRegistrationCallback.EVENT.register(SiftCommand::register);

        // Events
        ServerPlayConnectionEvents.JOIN.register(PlayerJoinHandler::onJoin);
        ServerLivingEntityEvents.AFTER_DEATH.register(KillUpgradeHandler::onDeath);
        SiftMaceHandler.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WeaponState.get(server); // force load
            LOGGER.info("Sift Weapons ready. Limits: SiftMace=2, GodMace=1, GodSpear=1");
        });

        LOGGER.info("Sift Weapons initialized. Use /sift for op commands.");
    }
}
