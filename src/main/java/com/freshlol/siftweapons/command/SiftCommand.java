package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.freshlol.siftweapons.util.WeaponHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
                ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help"), false);
                return 1;
            })
            .then(Commands.literal("help").executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6/sift give <player> levelled <mace|spear> <1-14>\n" +
                    "§6/sift give <player> siftmace|godmace|godspear\n" +
                    "§6/sift setlevel <player> <mace|spear> <1-14>\n" +
                    "§6/sift info"), false);
                return 1;
            }))
            .then(Commands.literal("info").executes(ctx -> {
                WeaponState s = WeaponState.get(ctx.getSource().getServer());
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "SiftMace " + s.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE +
                    " | GodMace " + s.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE +
                    " | GodSpear " + s.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR), false);
                return 1;
            }))
            .then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("levelled")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                .executes(SiftCommand::giveLevelled))))
                    .then(Commands.literal("siftmace").executes(ctx -> giveSpecial(ctx, "siftmace")))
                    .then(Commands.literal("godmace").executes(ctx -> giveSpecial(ctx, "godmace")))
                    .then(Commands.literal("godspear").executes(ctx -> giveSpecial(ctx, "godspear")))))
            .then(Commands.literal("setlevel")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("type", StringArgumentType.word())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                            .executes(SiftCommand::setLevel)))));
    }

    private static int giveLevelled(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String type = StringArgumentType.getString(ctx, "type").toLowerCase();
        int level = IntegerArgumentType.getInteger(ctx, "level");
        boolean isMace = type.equals("mace");
        if (!isMace && !type.equals("spear")) {
            ctx.getSource().sendFailure(Component.literal("Type must be mace or spear"));
            return 0;
        }
        ItemStack stack = WeaponHelper.createLevelled(level, isMace);
        if (!target.getInventory().add(stack)) target.drop(stack, false);
        ctx.getSource().sendSuccess(() -> Component.literal("Gave Level " + level + " " + (isMace ? "Mace" : "Spear") + " to " + target.getName().getString()), true);
        return 1;
    }

    private static int giveSpecial(CommandContext<CommandSourceStack> ctx, String kind) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        WeaponState state = WeaponState.get(ctx.getSource().getServer());
        ItemStack stack;
        if ("siftmace".equals(kind)) {
            if (!state.canCreateSiftMace()) { ctx.getSource().sendFailure(Component.literal("Sift Mace limit reached")); return 0; }
            stack = WeaponHelper.createSiftMace(); state.incrementSiftMace();
        } else if ("godmace".equals(kind)) {
            if (!state.canCreateGodMace()) { ctx.getSource().sendFailure(Component.literal("God Mace limit reached")); return 0; }
            stack = WeaponHelper.createGodMace(); state.incrementGodMace();
        } else {
            if (!state.canCreateGodSpear()) { ctx.getSource().sendFailure(Component.literal("God Spear limit reached")); return 0; }
            stack = WeaponHelper.createGodSpear(); state.incrementGodSpear();
        }
        if (!target.getInventory().add(stack)) target.drop(stack, false);
        ctx.getSource().sendSuccess(() -> Component.literal("Gave " + kind + " to " + target.getName().getString()), true);
        return 1;
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
            ctx.getSource().sendSuccess(() -> Component.literal("Set main hand to Level " + level), true);
            return 1;
        }
        ItemStack off = target.getOffhandItem();
        if (WeaponHelper.isLevelled(off) && ((isMace && WeaponHelper.getType(off).contains("mace")) || (!isMace && WeaponHelper.getType(off).contains("spear")))) {
            WeaponHelper.setLevel(off, level);
            ctx.getSource().sendSuccess(() -> Component.literal("Set offhand to Level " + level), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("No matching levelled weapon in hand"));
        return 0;
    }
}
