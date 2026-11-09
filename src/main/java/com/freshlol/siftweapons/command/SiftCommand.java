package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SiftCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("sift")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("levelled")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((c, b) -> {
                                                    b.suggest("mace");
                                                    b.suggest("spear");
                                                    return b.buildFuture();
                                                })
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                        .executes(SiftCommand::giveLevelled))))
                                .then(Commands.literal("siftmace")
                                        .executes(SiftCommand::giveSiftMace))
                                .then(Commands.literal("godmace")
                                        .executes(SiftCommand::giveGodMace))
                                .then(Commands.literal("godspear")
                                        .executes(SiftCommand::giveGodSpear))))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("mace");
                                            b.suggest("spear");
                                            return b.buildFuture();
                                        })
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                .executes(SiftCommand::setLevel)))))
                .then(Commands.literal("info")
                        .executes(SiftCommand::info))
                .then(Commands.literal("help")
                        .executes(SiftCommand::help))
        );
    }

    private static int giveLevelled(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
            int level = IntegerArgumentType.getInteger(ctx, "level");

            boolean isMace = type.equals("mace");
            if (!type.equals("mace") && !type.equals("spear")) {
                ctx.getSource().sendFailure(Component.literal("Type must be 'mace' or 'spear'"));
                return 0;
            }

            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
            if (player.getInventory().add(stack)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Gave " + player.getName().getString() + " a Level " + level + " " + (isMace ? "Mace" : "Spear")), true);
                player.sendSystemMessage(Component.literal("You received a Level " + level + " " + (isMace ? "Mace" : "Spear") + "!").withStyle(net.minecraft.ChatFormatting.GREEN));
            } else {
                ctx.getSource().sendFailure(Component.literal("Player inventory full"));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveSiftMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());

            if (!state.canCreateSiftMace()) {
                ctx.getSource().sendFailure(Component.literal("Sift Mace limit reached (" + WeaponState.MAX_SIFT_MACE + "/" + WeaponState.MAX_SIFT_MACE + ")"));
                return 0;
            }

            ItemStack stack = WeaponHelper.createSiftMace();
            if (player.getInventory().add(stack)) {
                state.incrementSiftMace();
                ctx.getSource().sendSuccess(() -> Component.literal("Gave " + player.getName().getString() + " a Sift Mace (" + state.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE + ")"), true);
                player.sendSystemMessage(Component.literal("You received a limited Sift Mace!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            } else {
                ctx.getSource().sendFailure(Component.literal("Player inventory full"));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveGodMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());

            if (!state.canCreateGodMace()) {
                ctx.getSource().sendFailure(Component.literal("God Mace limit reached (1/1) — only one can exist"));
                return 0;
            }

            ItemStack stack = WeaponHelper.createGodMace();
            if (player.getInventory().add(stack)) {
                state.incrementGodMace();
                ctx.getSource().sendSuccess(() -> Component.literal("Gave " + player.getName().getString() + " the God Mace"), true);
                player.sendSystemMessage(Component.literal("You received the one and only God Mace!").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                ctx.getSource().sendFailure(Component.literal("Player inventory full"));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int giveGodSpear(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());

            if (!state.canCreateGodSpear()) {
                ctx.getSource().sendFailure(Component.literal("God Spear limit reached (1/1) — only one can exist"));
                return 0;
            }

            ItemStack stack = WeaponHelper.createGodSpear();
            if (player.getInventory().add(stack)) {
                state.incrementGodSpear();
                ctx.getSource().sendSuccess(() -> Component.literal("Gave " + player.getName().getString() + " the God Spear"), true);
                player.sendSystemMessage(Component.literal("You received the one and only God Spear!").withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                ctx.getSource().sendFailure(Component.literal("Player inventory full"));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
            int level = IntegerArgumentType.getInteger(ctx, "level");

            boolean found = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (WeaponHelper.isLevelled(stack) && WeaponHelper.getType(stack).equals(type)
                        && !WeaponHelper.isSiftMace(stack) && !WeaponHelper.isGodMace(stack) && !WeaponHelper.isGodSpear(stack)) {
                    WeaponHelper.setLevel(stack, level);
                    found = true;
                    break;
                }
            }

            if (found) {
                ctx.getSource().sendSuccess(() -> Component.literal("Set " + player.getName().getString() + "'s " + type + " to level " + level), true);
                player.sendSystemMessage(Component.literal("Your " + type + " was set to Level " + level).withStyle(net.minecraft.ChatFormatting.GREEN));
            } else {
                ctx.getSource().sendFailure(Component.literal("No matching levelled " + type + " found in inventory"));
            }
            return found ? 1 : 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        WeaponState state = WeaponState.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6Sift Weapons Status\n" +
                "§7Sift Mace: §f" + state.getSiftMaceCount() + "§7/§f" + WeaponState.MAX_SIFT_MACE + "\n" +
                "§7God Mace:  §f" + state.getGodMaceCount() + "§7/§f" + WeaponState.MAX_GOD_MACE + "\n" +
                "§7God Spear: §f" + state.getGodSpearCount() + "§7/§f" + WeaponState.MAX_GOD_SPEAR
        ), false);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§6=== Sift Weapons Commands (OP) ===\n" +
                "§e/sift give <player> levelled <mace|spear> <1-14>\n" +
                "§e/sift give <player> siftmace\n" +
                "§e/sift give <player> godmace\n" +
                "§e/sift give <player> godspear\n" +
                "§e/sift setlevel <player> <mace|spear> <1-14>\n" +
                "§e/sift info\n" +
                "§7Limits: SiftMace=2, GodMace=1, GodSpear=1"
        ), false);
        return 1;
    }
}
