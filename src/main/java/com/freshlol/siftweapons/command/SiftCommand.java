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
                    ctx.getSource().sendSuccess(() -> Component.literal("§e/sift help §7| §e/sift give stab|nuke <1-10>"), false);
                    return 1;
                })
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6=== Sift Weapons ===\n" +
                        "§e/sift give <player> spear|mace|sift|godmace|godspear|godcrossbow\n" +
                        "§e/sift give <player> stab <1-10> §7- Stab Shot fishing rod\n" +
                        "§e/sift give <player> nuke <1-10> §7- Nuke Shot fishing rod\n" +
                        "§e/sift setlevel|fix|reset|info"
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    WeaponState s = WeaponState.get(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§6Sift Mace: §f" + s.getSiftMaceCount() + "/2  §dGod: §f" + s.getGodMaceCount() + "/" + s.getGodSpearCount() + "/" + s.getGodCrossbowCount()
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
                        .then(Commands.literal("rainbow").executes(ctx -> giveRainbow(ctx, 10))
                            .then(Commands.argument("level", IntegerArgumentType.integer(10, 14))
                                .executes(ctx -> giveRainbow(ctx, IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("sift").executes(SiftCommand::giveSiftMace))
                        .then(Commands.literal("godmace").executes(SiftCommand::giveGodMace))
                        .then(Commands.literal("godspear").executes(SiftCommand::giveGodSpear))
                        .then(Commands.literal("godcrossbow").executes(SiftCommand::giveGodCrossbow))
                        .then(Commands.literal("stab")
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> giveRod(ctx, true, IntegerArgumentType.getInteger(ctx, "level")))))
                        .then(Commands.literal("nuke")
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> giveRod(ctx, false, IntegerArgumentType.getInteger(ctx, "level")))))
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
                        if (self == null) return 0;
                        return fixHeld(ctx, self);
                    })
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            try { return fixHeld(ctx, EntityArgument.getPlayer(ctx, "target")); }
                            catch (Exception e) { return 0; }
                        })
                    )
                )
                .then(Commands.literal("reset").executes(ctx -> {
                    WeaponState.get(ctx.getSource().getServer()).resetAll();
                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Reset."), true);
                    return 1;
                }))
        );
    }

    private static int giveRod(CommandContext<CommandSourceStack> ctx, boolean stab, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = stab ? WeaponHelper.createStabRod(level) : WeaponHelper.createNukeRod(level);
            target.getInventory().add(stack);
            String name = stab ? "Stab Shot Rod" : "Nuke Shot Rod";
            ctx.getSource().sendSuccess(() -> Component.literal("§a[Sift] Gave §e" + name + " Lv" + level + " §ato " + target.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§c" + e.getMessage()));
            return 0;
        }
    }

    private static int fixHeld(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ItemStack main = target.getMainHandItem();
        if (!WeaponHelper.isLevelled(main) && !WeaponHelper.isGodWeapon(main) && !WeaponHelper.isSiftMace(main)) {
            ctx.getSource().sendFailure(Component.literal("§cNot a Sift weapon."));
            return 0;
        }
        if (WeaponHelper.isGodCrossbow(main)) {
            WeaponHelper.applyGodCrossbowEnchants(main, target);
            return 1;
        }
        boolean isMace = WeaponHelper.getType(main).contains("mace");
        int level = Math.max(1, WeaponHelper.getLevel(main));
        if (WeaponHelper.isLevelled(main)) {
            target.setItemInHand(InteractionHand.MAIN_HAND, WeaponHelper.upgradeToLevel(main, level, target));
        } else {
            WeaponHelper.applyEnchants(main, level, isMace, target);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§aFixed."), true);
        return 1;
    }

    private static int giveWeapon(CommandContext<CommandSourceStack> ctx, boolean isMace, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = WeaponHelper.createLevelled(level, isMace);
            WeaponHelper.applyEnchants(stack, level, isMace, target);
            target.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal("§aGave level " + level), true);
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int giveRainbow(CommandContext<CommandSourceStack> ctx, int level) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            ItemStack stack = WeaponHelper.createLevelled(Math.max(10, level), true);
            WeaponHelper.applyEnchants(stack, level, true, target);
            target.getInventory().add(stack);
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int giveSiftMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateSiftMace()) { ctx.getSource().sendFailure(Component.literal("§cLimit 2/2")); return 0; }
            ItemStack stack = WeaponHelper.createSiftMace();
            WeaponHelper.applyEnchants(stack, 14, true, target);
            target.getInventory().add(stack);
            state.incrementSiftMace();
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int giveGodMace(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateGodMace()) return 0;
            ItemStack stack = WeaponHelper.createGodMace();
            WeaponHelper.applyEnchants(stack, 14, true, target);
            target.getInventory().add(stack);
            state.incrementGodMace();
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int giveGodSpear(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateGodSpear()) return 0;
            ItemStack stack = WeaponHelper.createGodSpear();
            WeaponHelper.applyEnchants(stack, 14, false, target);
            target.getInventory().add(stack);
            state.incrementGodSpear();
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int giveGodCrossbow(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            WeaponState state = WeaponState.get(ctx.getSource().getServer());
            if (!state.canCreateGodCrossbow()) return 0;
            ItemStack stack = WeaponHelper.createGodCrossbow();
            WeaponHelper.applyGodCrossbowEnchants(stack, target);
            target.getInventory().add(stack);
            state.incrementGodCrossbow();
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            int level = IntegerArgumentType.getInteger(ctx, "level");
            ItemStack main = target.getMainHandItem();
            if (!WeaponHelper.isLevelled(main)) return 0;
            target.setItemInHand(InteractionHand.MAIN_HAND, WeaponHelper.upgradeToLevel(main, level, target));
            return 1;
        } catch (Exception e) { return 0; }
    }
}
