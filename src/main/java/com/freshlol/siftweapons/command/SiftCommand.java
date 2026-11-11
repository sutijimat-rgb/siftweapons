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
        dispatcher.register(
            Commands.literal("sift")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7- show commands"), false);
                    return 1;
                })
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6=== Sift Weapons ===\n" +
                        "§e/sift give <player> levelled <mace|spear> <1-14>\n" +
                        "§e/sift give <player> siftmace|godmace|godspear\n" +
                        "§e/sift setlevel <player> <mace|spear> <1-14>\n" +
                        "§e/sift info"
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    WeaponState state = WeaponState.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6Sift Mace: §f" + state.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE +
                        "  §dGod Mace: §f" + state.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE +
                        "  §6God Spear: §f" + state.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("give")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("levelled")
                            .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                    .executes(ctx -> giveLevelled(ctx)))))
                        .then(Commands.literal("siftmace").executes(ctx -> giveSpecial(ctx, "siftmace")))
                        .then(Commands.literal("godmace").executes(ctx -> giveSpecial(ctx, "godmace")))
                        .then(Commands.literal("godspear").executes(ctx -> giveSpecial(ctx, "godspear")))))
                .then(Commands.literal("setlevel")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("type", StringArgumentType.word())
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                .executes(ctx -> setLevel(ctx)))))
        );
    }

    private static int giveLevelled(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
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
        ctx.getSource().sendSuccess(() -> Component.literal("§aGave Level " + level + " " + (isMace ? "Mace" : "Spear") + " to " + target.getName().getString()), true);
        return 1;
    }

    private static int giveSpecial(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String kind) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        WeaponState state = WeaponState.get(ctx.getSource().getServer());
        ItemStack stack;
        if (kind.equals("siftmace")) {
            if (!state.canCreateSiftMace()) {
                ctx.getSource().sendFailure(Component.literal("§cSift Mace limit reached"));
                return 0;
            }
            stack = WeaponHelper.createSiftMace();
            state.incrementSiftMace();
        } else if (kind.equals("godmace")) {
            if (!state.canCreateGodMace()) {
                ctx.getSource().sendFailure(Component.literal("§cGod Mace limit reached"));
                return 0;
            }
            stack = WeaponHelper.createGodMace();
            state.incrementGodMace();
        } else {
            if (!state.canCreateGodSpear()) {
                ctx.getSource().sendFailure(Component.literal("§cGod Spear limit reached"));
                return 0;
            }
            stack = WeaponHelper.createGodSpear();
            state.incrementGodSpear();
        }
        if (!target.getInventory().add(stack)) {
            target.drop(stack, false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§aGave " + kind + " to " + target.getName().getString()), true);
        return 1;
    }

    private static int setLevel(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String type = StringArgumentType.getString(ctx, "type").toLowerCase();
        int level = IntegerArgumentType.getInteger(ctx, "level");
        boolean isMace = type.equals("mace");
        if (!isMace && !type.equals("spear")) {
            ctx.getSource().sendFailure(Component.literal("Type must be mace or spear"));
            return 0;
        }
        ItemStack main = target.getMainHandItem();
        if (WeaponHelper.isLevelled(main) && ((isMace && WeaponHelper.getType(main).contains("mace")) || (!isMace && WeaponHelper.getType(main).contains("spear")))) {
            WeaponHelper.setLevel(main, level);
            ctx.getSource().sendSuccess(() -> Component.literal("§aSet main hand to Level " + level), true);
            return 1;
        }
        ItemStack off = target.getOffhandItem();
        if (WeaponHelper.isLevelled(off) && ((isMace && WeaponHelper.getType(off).contains("mace")) || (!isMace && WeaponHelper.getType(off).contains("spear")))) {
            WeaponHelper.setLevel(off, level);
            ctx.getSource().sendSuccess(() -> Component.literal("§aSet offhand to Level " + level), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("Player is not holding a matching levelled weapon"));
        return 0;
    }
}
