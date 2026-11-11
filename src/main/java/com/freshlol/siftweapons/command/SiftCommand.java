package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public class SiftCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("sift")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7- show commands"), false);
                    return 1;
                })
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6=== Sift Weapons Commands ===\n" +
                                    "§e/sift give <player> levelled <mace|spear> <1-14>\n" +
                                    "§e/sift give <player> siftmace\n" +
                                    "§e/sift give <player> godmace\n" +
                                    "§e/sift give <player> godspear\n" +
                                    "§e/sift setlevel <player> <mace|spear> <1-14>\n" +
                                    "§e/sift info\n" +
                                    "§e/sift help"
                            ), false);
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            WeaponState state = WeaponState.get(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6=== Sift Weapons Limits ===\n" +
                                    "§bSift Mace: §f" + state.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE + "\n" +
                                    "§dGod Mace: §f" + state.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE + "\n" +
                                    "§6God Spear: §f" + state.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR
                            ), false);
                            return 1;
                        }))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("levelled")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                        .executes(ctx -> {
                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                                            boolean isMace = type.equals("mace");
                                                            if (!isMace && !type.equals("spear")) {
                                                                ctx.getSource().sendFailure(Component.literal("Type must be mace or spear"));
                                                                return 0;
                                                            }
                                                            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
                                                            if (!target.getInventory().add(stack)) {
                                                                target.drop(stack, false);
                                                            }
                                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                                    "§aGave Level " + level + " " + (isMace ? "Mace" : "Spear") + " to " + target.getName().getString()), true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("siftmace")
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            WeaponState state = WeaponState.get(ctx.getSource().getServer());
                                            if (!state.canCreateSiftMace()) {
                                                ctx.getSource().sendFailure(Component.literal("§cSift Mace limit reached (" + WeaponState.MAX_SIFT_MACE + ")"));
                                                return 0;
                                            }
                                            ItemStack stack = WeaponHelper.createSiftMace();
                                            if (!target.getInventory().add(stack)) {
                                                target.drop(stack, false);
                                            }
                                            state.incrementSiftMace();
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aGave Sift Mace to " + target.getName().getString()), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("godmace")
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            WeaponState state = WeaponState.get(ctx.getSource().getServer());
                                            if (!state.canCreateGodMace()) {
                                                ctx.getSource().sendFailure(Component.literal("§cGod Mace limit reached (" + WeaponState.MAX_GOD_MACE + ")"));
                                                return 0;
                                            }
                                            ItemStack stack = WeaponHelper.createGodMace();
                                            if (!target.getInventory().add(stack)) {
                                                target.drop(stack, false);
                                            }
                                            state.incrementGodMace();
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aGave God Mace to " + target.getName().getString()), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("godspear")
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            WeaponState state = WeaponState.get(ctx.getSource().getServer());
                                            if (!state.canCreateGodSpear()) {
                                                ctx.getSource().sendFailure(Component.literal("§cGod Spear limit reached (" + WeaponState.MAX_GOD_SPEAR + ")"));
                                                return 0;
                                            }
                                            ItemStack stack = WeaponHelper.createGodSpear();
                                            if (!target.getInventory().add(stack)) {
                                                target.drop(stack, false);
                                            }
                                            state.incrementGodSpear();
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aGave God Spear to " + target.getName().getString()), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                    int level = IntegerArgumentType.getInteger(ctx, "level");
                                                    boolean isMace = type.equals("mace");
                                                    if (!isMace && !type.equals("spear")) {
                                                        ctx.getSource().sendFailure(Component.literal("Type must be mace or spear"));
                                                        return 0;
                                                    }
                                                    ItemStack main = target.getMainHandItem();
                                                    if (WeaponHelper.isLevelled(main) &&
                                                            ((isMace && WeaponHelper.getType(main).contains("mace")) ||
                                                             (!isMace && WeaponHelper.getType(main).contains("spear")))) {
                                                        WeaponHelper.setLevel(main, level);
                                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                                "§aSet " + target.getName().getString() + "'s main hand to Level " + level), true);
                                                        return 1;
                                                    }
                                                    ItemStack off = target.getOffhandItem();
                                                    if (WeaponHelper.isLevelled(off) &&
                                                            ((isMace && WeaponHelper.getType(off).contains("mace")) ||
                                                             (!isMace && WeaponHelper.getType(off).contains("spear")))) {
                                                        WeaponHelper.setLevel(off, level);
                                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                                "§aSet " + target.getName().getString() + "'s offhand to Level " + level), true);
                                                        return 1;
                                                    }
                                                    ctx.getSource().sendFailure(Component.literal("Player is not holding a matching levelled weapon"));
                                                    return 0;
                                                })))));
    }
}
