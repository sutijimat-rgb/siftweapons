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
        dispatcher.register(
            Commands.literal("sift")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7| §e/sift info §7| §e/sift give §7| §e/sift setlevel §7| §e/sift reset"), false);
                    return 1;
                })
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6=== Sift Weapons Commands ===\n" +
                        "§e/sift info §7- show weapon limits\n" +
                        "§e/sift give <player> <type> [level] §7- give a weapon\n" +
                        "  types: spear, mace, rainbow, sift, godmace, godspear\n" +
                        "§e/sift setlevel <player> <level> §7- set level of held levelled weapon\n" +
                        "§e/sift reset §7- reset all (counts + joins)\n" +
                        "§e/sift reset counts §7- reset weapon limits only\n" +
                        "§e/sift reset joins §7- reset first-join flags\n" +
                        "§e/sift reset player <name> §7- let one player get starters again"
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    WeaponState s = WeaponState.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6Sift Mace: §f" + s.getSiftMaceCount() + "/2  §dGod Mace: §f" + s.getGodMaceCount() + "/1  §6God Spear: §f" + s.getGodSpearCount() + "/1"
                    ), false);
                    return 1;
                }))
                // /sift give <player> <type> [level]
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
                // /sift setlevel <player> <level>
                .then(Commands.literal("setlevel")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                            .executes(SiftCommand::setLevel))
                    )
                )
                // reset subcommands
                .then(Commands.literal("reset")
                    .executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetAll();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Full reset done (counts + first-join flags)."), true);
                        return 1;
                    })
                    .then(Commands.literal("counts").executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetCounts();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Weapon counts reset."), true);
                        return 1;
                    }))
                    .then(Commands.literal("joins").executes(ctx -> {
                        WeaponState.get(ctx.getSource().getServer()).resetJoins();
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] First-join flags cleared. Players can get starters again."), true);
                        return 1;
                    }))
                    .then(Commands.literal("player")
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(ctx -> {
                                try {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    WeaponState.get(ctx.getSource().getServer()).resetPlayer(target.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Reset first-join for " + target.getName().getString() + ". They get starters next join."), true);
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

    private static int giveWeapon(CommandContext<CommandSourceStack> ctx, boolean isMace, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
            target.getInventory().add(stack);
            String name = isMace ? (level >= 10 ? "Rainbow Mace" : "Mace") : (WeaponHelper.getMaterialName(level) + " Spear");
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave §eLevel " + level + " " + name + " §ato " + target.getName().getString()), true);
            target.sendSystemMessage(Component.literal("§a[Sift] You received a Level " + level + " " + name + "!"));
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
            target.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave §cRainbow Mace §e(Level " + level + ") §ato " + target.getName().getString()), true);
            target.sendSystemMessage(Component.literal("§a[Sift] You received a §cRainbow Mace§a!"));
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
                ctx.getSource().sendFailure(Component.literal("§cSift Mace limit reached (2/2)"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createSiftMace();
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
                ctx.getSource().sendFailure(Component.literal("§cGod Mace limit reached (1/1)"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createGodMace();
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
                ctx.getSource().sendFailure(Component.literal("§cGod Spear limit reached (1/1)"));
                return 0;
            }
            ItemStack stack = WeaponHelper.createGodSpear();
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

            ItemStack upgraded = WeaponHelper.upgradeToLevel(main, level);
            target.setItemInHand(InteractionHand.MAIN_HAND, upgraded);

            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Set " + target.getName().getString() + "'s weapon to Level " + level), true);
            target.sendSystemMessage(Component.literal("§a[Sift] Your weapon was set to Level " + level + "!"));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
