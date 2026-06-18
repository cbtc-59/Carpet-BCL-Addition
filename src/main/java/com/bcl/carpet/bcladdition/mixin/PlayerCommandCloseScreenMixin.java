package com.bcl.carpet.bcladdition.mixin;

import carpet.commands.PlayerCommand;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Adds esc and mannequin subcommands to /player by intercepting the
 * argument("player", ...) builder in PlayerCommand.register().
 * Mirrors Carpet-Org's PlayerCommandExtension approach.
 */
@Mixin(PlayerCommand.class)
public class PlayerCommandCloseScreenMixin {

    @WrapOperation(
            method = "register",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/brigadier/arguments/StringArgumentType;word()Lcom/mojang/brigadier/arguments/StringArgumentType;")
    )
    private static StringArgumentType wrapPlayerArgument(Operation<StringArgumentType> original) {
        return original.call();
    }

    @WrapOperation(
            method = "register",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"),
            remap = false
    )
    private static RequiredArgumentBuilder<ServerCommandSource, ?> wrapArgument(
            String name, com.mojang.brigadier.arguments.ArgumentType<?> type,
            Operation<RequiredArgumentBuilder<ServerCommandSource, ?>> original) {

        RequiredArgumentBuilder<ServerCommandSource, ?> builder = original.call(name, type);

        // Add esc subcommand if rule enabled
        builder.then(literal("esc")
                .requires(src -> BCLAdditionSettings.playerCommandCloseScreen)
                .executes(ctx -> {
                    String playerName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "player");
                    ServerPlayerEntity target = ctx.getSource().getServer()
                            .getPlayerManager().getPlayer(playerName);
                    if (target == null) return 0;
                    target.closeHandledScreen();
                    return 1;
                })
        );

        return builder;
    }
}
