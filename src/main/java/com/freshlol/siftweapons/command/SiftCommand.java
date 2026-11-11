package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class SiftCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("sift")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7| §e/sift info §7| §e/sift reset"), false);
                    return 1;
                })
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6=== Sift Weapons ===\n" +
                                    "§e/sift info §7- show limits\n" +
                                    "§e/sift reset §7- reset all (counts + joins)\n" +
                                    "§e/sift reset counts §7- reset weapon limits only\n" +
                                    "§e/sift reset joins §7- reset first-join flags\n" +
                                    "§e/sift reset player <name> §7- let one player get starters again"
                            ), false);
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            WeaponState s = WeaponState.get(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6Sift Mace: §f" + s.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE +
                                    "  §dGod Mace: §f" + s.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE +
                                    "  §6God Spear: §f" + s.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR
                            ), false);
                            return 1;
                        }))
                .then(Commands.literal("reset")
                        .executes(ctx -> {
                            WeaponState.get(ctx.getSource().getServer()).resetAll();
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Full reset done (counts + first-join flags)."), true);
                            return 1;
                        })
                        .then(Commands.literal("counts")
                                .executes(ctx -> {
                                    WeaponState.get(ctx.getSource().getServer()).resetCounts();
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Weapon counts reset."), true);
                                    return 1;
                                }))
                        .then(Commands.literal("joins")
                                .executes(ctx -> {
                                    WeaponState.get(ctx.getSource().getServer()).resetJoins();
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] First-join flags cleared. Players can get starters again."), true);
                                    return 1;
                                }))
                        .then(Commands.literal("player")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            WeaponState.get(ctx.getSource().getServer()).resetPlayer(target.getUUID());
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§a[Sift] Reset first-join for " + target.getName().getString() + ". They get starters next join."), true);
                                            return 1;
                                        }))))
        );
    }
}
