package com.freshlol.siftweapons.command;

import com.freshlol.siftweapons.data.WeaponState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class SiftCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"sift").requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))).executes(ctx -> {
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a7e/sift help \u00a77| \u00a7e/sift info \u00a77| \u00a7e/sift reset"), false);
            return 1;
        })).then(Commands.literal((String)"help").executes(ctx -> {
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a76=== Sift Weapons ===\n\u00a7e/sift info \u00a77- show limits\n\u00a7e/sift reset \u00a77- reset all (counts + joins)\n\u00a7e/sift reset counts \u00a77- reset weapon limits only\n\u00a7e/sift reset joins \u00a77- reset first-join flags\n\u00a7e/sift reset player <name> \u00a77- let one player get starters again"), false);
            return 1;
        }))).then(Commands.literal((String)"info").executes(ctx -> {
            WeaponState s = WeaponState.get(((CommandSourceStack)ctx.getSource()).getServer());
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)("\u00a76Sift Mace: \u00a7f" + s.getSiftMaceCount() + "/2  \u00a7dGod Mace: \u00a7f" + s.getGodMaceCount() + "/1  \u00a76God Spear: \u00a7f" + s.getGodSpearCount() + "/1")), false);
            return 1;
        }))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"reset").executes(ctx -> {
            WeaponState.get(((CommandSourceStack)ctx.getSource()).getServer()).resetAll();
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a7a[Sift] Full reset done (counts + first-join flags)."), true);
            return 1;
        })).then(Commands.literal((String)"counts").executes(ctx -> {
            WeaponState.get(((CommandSourceStack)ctx.getSource()).getServer()).resetCounts();
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a7a[Sift] Weapon counts reset."), true);
            return 1;
        }))).then(Commands.literal((String)"joins").executes(ctx -> {
            WeaponState.get(((CommandSourceStack)ctx.getSource()).getServer()).resetJoins();
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a7a[Sift] First-join flags cleared. Players can get starters again."), true);
            return 1;
        }))).then(Commands.literal((String)"player").then(Commands.argument((String)"target", (ArgumentType)EntityArgument.player()).executes(ctx -> {
            ServerPlayer target = EntityArgument.getPlayer((CommandContext)ctx, (String)"target");
            WeaponState.get(((CommandSourceStack)ctx.getSource()).getServer()).resetPlayer(target.getUUID());
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)("\u00a7a[Sift] Reset first-join for " + target.getName().getString() + ". They get starters next join.")), true);
            return 1;
        })))));
    }
}
