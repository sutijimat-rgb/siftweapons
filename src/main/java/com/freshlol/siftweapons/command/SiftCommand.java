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
                ctx.getSource().sendSuccess(() -> Component.literal("Sift Weapons - use /sift help"), false);
                return 1;
            })
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "Commands: help, info, give, setlevel"), false);
                    return 1;
                }))
            .then(Commands.literal("info")
                .executes(ctx -> {
                    WeaponState s = WeaponState.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "SiftMace " + s.getSiftMaceCount() + "/" + WeaponState.MAX_SIFT_MACE +
                        " GodMace " + s.getGodMaceCount() + "/" + WeaponState.MAX_GOD_MACE +
                        " GodSpear " + s.getGodSpearCount() + "/" + WeaponState.MAX_GOD_SPEAR), false);
                    return 1;
                }))
            .then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("siftmace")
                        .executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            WeaponState s = WeaponState.get(ctx.getSource().getServer());
                            if (!s.canCreateSiftMace()) {
                                ctx.getSource().sendFailure(Component.literal("Limit reached"));
                                return 0;
                            }
                            ItemStack stack = WeaponHelper.createSiftMace();
                            if (!p.getInventory().add(stack)) p.drop(stack, false);
                            s.incrementSiftMace();
                            ctx.getSource().sendSuccess(() -> Component.literal("Gave Sift Mace"), true);
                            return 1;
                        }))
                    .then(Commands.literal("godmace")
                        .executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            WeaponState s = WeaponState.get(ctx.getSource().getServer());
                            if (!s.canCreateGodMace()) {
                                ctx.getSource().sendFailure(Component.literal("Limit reached"));
                                return 0;
                            }
                            ItemStack stack = WeaponHelper.createGodMace();
                            if (!p.getInventory().add(stack)) p.drop(stack, false);
                            s.incrementGodMace();
                            ctx.getSource().sendSuccess(() -> Component.literal("Gave God Mace"), true);
                            return 1;
                        }))
                    .then(Commands.literal("godspear")
                        .executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            WeaponState s = WeaponState.get(ctx.getSource().getServer());
                            if (!s.canCreateGodSpear()) {
                                ctx.getSource().sendFailure(Component.literal("Limit reached"));
                                return 0;
                            }
                            ItemStack stack = WeaponHelper.createGodSpear();
                            if (!p.getInventory().add(stack)) p.drop(stack, false);
                            s.incrementGodSpear();
                            ctx.getSource().sendSuccess(() -> Component.literal("Gave God Spear"), true);
                            return 1;
                        }))
                    .then(Commands.literal("levelled")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 14))
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                                    String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                    int level = IntegerArgumentType.getInteger(ctx, "level");
                                    boolean isMace = "mace".equals(type);
                                    if (!isMace && !"spear".equals(type)) {
                                        ctx.getSource().sendFailure(Component.literal("mace or spear"));
                                        return 0;
                                    }
                                    ItemStack stack = WeaponHelper.createLevelled(level, isMace);
                                    if (!p.getInventory().add(stack)) p.drop(stack, false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Gave Level " + level), true);
                                    return 1;
                                })))))));
    }
}
