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
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6=== Sift Weapons Commands (OP) ===\n" +
                                    "§e/sift give <player> levelled <mace|spear> <1-14>\n" +
                                    "§e/sift give <player> siftmace|godmace|godspear\n" +
                                    "§e/sift setlevel <player> <mace|spear> <1-14>\n" +
                                    "§e/sift info"
                            ), false);
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .executes(SiftCommand::info))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("siftmace").executes(SiftCommand::giveSiftMace))
                                .then(Commands.literal("godmace").executes(SiftCommand::giveGodMace))
                                .then(Commands.literal("godspear").executes(SiftCommand::giveGodSpear))
                                .then(Commands.literal("levelled")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                        .executes(SiftCommand::giveLevelled))))))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                                .executes(SiftCommand::setLevel)))))
        );
    }

    private static int giveLevelled(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
            int level = IntegerArgumentType.getInteger(ctx, "level");
            boolean isMace = "mace".equals(type);
            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
            player.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("Gave levelled weapon"), true);
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
                ctx.getSource().sendFailure(Component.literal("Sift Mace limit reached"));
                return 0;
            }
            player.getInventory().add(WeaponHelper.createSiftMace());
            state.incrementSiftMace();
            ctx.getSource().sendSuccess(() -> Component.literal("Gave Sift Mace"), true);
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
                ctx.getSource().sendFailure(Component.literal("God Mace limit reached"));
                return 0;
            }
            player.getInventory().add(WeaponHelper.createGodMace());
            state.incrementGodMace();
            ctx.getSource().sendSuccess(() -> Component.literal("Gave God Mace"), true);
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
                ctx.getSource().sendFailure(Component.literal("God Spear limit reached"));
                return 0;
            }
            player.getInventory().add(WeaponHelper.createGodSpear());
            state.incrementGodSpear();
            ctx.getSource().sendSuccess(() -> Component.literal("Gave God Spear"), true);
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
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (WeaponHelper.isLevelled(stack) && type.equals(WeaponHelper.getType(stack))) {
                    WeaponHelper.setLevel(stack, level);
                    ctx.getSource().sendSuccess(() -> Component.literal("Set level"), true);
                    return 1;
                }
            }
            ctx.getSource().sendFailure(Component.literal("No matching weapon found"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        WeaponState state = WeaponState.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Sift Mace: " + state.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE + "\n" +
                "God Mace: " + state.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE + "\n" +
                "God Spear: " + state.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR
        ), false);
        return 1;
    }
}
