package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SiftCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        // Player command: /siftclaim  (no op needed) — claim Sift Mace at level 14
        dispatcher.register(
            Commands.literal("siftclaim")
                .executes(SiftCommand::claimSift)
        );

        dispatcher.register(
            Commands.literal("sift")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7| §e/sift info §7| §e/sift give §7| §e/sift setlevel §7| §e/sift fix §7| §e/sift reset"), false);
                    return 1;
                })
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6=== Sift Weapons Commands ===\n" +
                        "§e/siftclaim §7- (anyone) claim Sift Mace if holding Level 14 mace\n" +
                        "§e/sift info §7- show weapon limits\n" +
                        "§e/sift give <player> <type> [level] §7- give a weapon\n" +
                        "  types: spear, mace, rainbow, sift, godmace, godspear\n" +
                        "§e/sift setlevel <player> <level> §7- set level of held levelled weapon\n" +
                        "§e/sift fix [player] §7- re-apply vanishing + enchants\n" +
                        "§e/sift reset §7- reset all"
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    WeaponState s = WeaponState.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6Sift Mace: §f" + s.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE +
                        "  §dGod Mace: §f" + s.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE +
                        "  §6God Spear: §f" + s.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("give")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("spear")
                            .executes(ctx -> giveWeapon(ctx, false, 1))
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                .executes(ctx -> giveWeapon(ctx, false, IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("mace")
                            .executes(ctx -> giveWeapon(ctx, true, 1))
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                .executes(ctx -> giveWeapon(ctx, true, IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("rainbow")
                            .executes(ctx -> giveRainbow(ctx, 10))
                            .then(Commands.argument("level", IntegerArgumentType.integer(10, 14))
                                .executes(ctx -> giveRainbow(ctx, IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("sift").executes(SiftCommand::giveSiftMace))
                        .then(Commands.literal("godmace").executes(SiftCommand::giveGodMace))
                        .then(Commands.literal("godspear").executes(SiftCommand::giveGodSpear))
                    )
                )
                .then(Commands.literal("setlevel")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                            .executes(SiftCommand::setLevel))
                    )
                )
                .then(Commands.literal("fix")
                    .executes(ctx -> {
                        ServerPlayer self = ctx.getSource().getPlayer();
                        if (self == null) {
                            ctx.getSource().sendFailure(Component.literal("§cMust be a player or specify a target"));
                            return 0;
                        }
                        return fixHeld(ctx, self);
                    })
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            try {
                                return fixHeld(ctx, EntityArgument.getPlayer(ctx, "target"));
                            } catch (Exception e) {
                                ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
                                return 0;
                            }
                        })
                    )
                )
                .then(Commands.literal("reset")
                    .executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetAll();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Full reset done."), true);
                        return 1;
                    })
                    .then(Commands.literal("counts").executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetCounts();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Weapon counts reset."), true);
                        return 1;
                    }))
                    .then(Commands.literal("joins").executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetJoins();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] First-join flags cleared."), true);
                        return 1;
                    }))
                    .then(Commands.literal("player")
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(ctx -> {
                                try {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    WeaponState.get(ctx.getSource().getServer()).resetPlayer(target.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Reset first-join for " + target.getName().getString()), true);
                                } catch (Exception e) {
                                    ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
                                    return 0;
                                }
                                return 1;
                            })
                        )
                    )
                )
        );
    }

    /** Anyone can run /siftclaim while holding a Level 14 mace to get a Sift Mace (once). */
    private static int claimSift(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("§cPlayers only."));
            return 0;
        }

        WeaponState state = WeaponState.get(ctx.getSource().getServer());
        if (state.hasClaimedSift(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cYou already claimed your Sift Mace."));
            return 0;
        }
        if (!state.canCreateSiftMace()) {
            ctx.getSource().sendFailure(Component.literal("§cServer Sift Mace limit reached."));
            return 0;
        }

        ItemStack main = player.getMainHandItem();
        boolean ok = WeaponHelper.isLevelled(main)
            && WeaponHelper.getType(main).contains("mace")
            && WeaponHelper.getLevel(main) >= 14;

        if (!ok) {
            ctx.getSource().sendFailure(Component.literal("§cHold a §eLevel 14 Mace §cin your main hand, then run §e/siftclaim§c."));
            return 0;
        }

        // Consume the level 14 mace and give Sift Mace
        main.shrink(1);
        ItemStack sift = WeaponHelper.createSiftMace();
        WeaponHelper.applyEnchants(sift, 14, true, player);
        player.getInventory().add(sift);

        state.incrementSiftMace();
        state.markClaimedSift(player.getUUID());

        player.sendSystemMessage(Component.literal("§b§l★ SIFT MACE CLAIMED! ★"));
        player.sendSystemMessage(Component.literal("§7Your Level 14 Mace was converted into a Sift Mace."));
        return 1;
    }

    private static int fixHeld(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ItemStack main = target.getMainHandItem();
        if (!WeaponHelper.isLevelled(main) && !WeaponHelper.isGodWeapon(main) && !WeaponHelper.isSiftMace(main)) {
            ctx.getSource().sendFailure(Component.literal("§cNot holding a Sift weapon."));
            return 0;
        }
        boolean isMace = WeaponHelper.getType(main).contains("mace");
        int level = WeaponHelper.getLevel(main);
        if (level < 1) level = 1;
        if (WeaponHelper.isLevelled(main)) {
            ItemStack fixed = WeaponHelper.upgradeToLevel(main, level, target);
            target.setItemInHand(InteractionHand.MAIN_HAND, fixed);
        } else {
            WeaponHelper.applyEnchants(main, level, isMace, target);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Fixed " + target.getName().getString() + "'s weapon."), true);
        return 1;
    }

    private static int giveWeapon(CommandContext<CommandSourceStack> ctx, boolean isMace, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
            WeaponHelper.applyEnchants(stack, level, isMace, target);
            target.getInventory().add(stack);
            String name = isMace ? (level >= 10 ? "Rainbow Mace" : "Mace") : (WeaponHelper.getMaterialName(level) + " Spear");
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave §eLevel " + level + " " + name + " §ato " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveRainbow(CommandContext<CommandSourceStack> ctx, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = WeaponHelper.createLevelled(Math.max(10, level), true);
            WeaponHelper.applyEnchants(stack, level, true, target);
            target.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave Rainbow Mace to " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveSiftMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateSiftMace()) {
                ctx.getSource().sendFailure(Component.literal("§cSift Mace limit reached (" + WeaponState.MAX_SIFT_MACE + ")"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createSiftMace();
            WeaponHelper.applyEnchants(stack, 14, true, target);
            target.getInventory().add(stack);
            state.incrementSiftMace();
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave Sift Mace to " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveGodMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateGodMace()) {
                ctx.getSource().sendFailure(Component.literal("§cGod Mace limit reached (" + WeaponState.MAX_GOD_MACE + ")"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createGodMace();
            WeaponHelper.applyEnchants(stack, 14, true, target);
            target.getInventory().add(stack);
            state.incrementGodMace();
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave God Mace to " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveGodSpear(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateGodSpear()) {
                ctx.getSource().sendFailure(Component.literal("§cGod Spear limit reached (" + WeaponState.MAX_GOD_SPEAR + ")"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createGodSpear();
            WeaponHelper.applyEnchants(stack, 14, false, target);
            target.getInventory().add(stack);
            state.incrementGodSpear();
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave God Spear to " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            int level = IntegerArgumentType.getInteger(ctx, "level");
            ItemStack main = target.getMainHandItem();
            if (!WeaponHelper.isLevelled(main)) {
                ctx.getSource().sendFailure(Component.literal("§cPlayer is not holding a levelled Sift weapon."));
                return 0;
            }
            ItemStack upgraded = WeaponHelper.upgradeToLevel(main, level, target);
            target.setItemInHand(InteractionHand.MAIN_HAND, upgraded);
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Set " + target.getName().getString() + "'s weapon to Level " + level), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
