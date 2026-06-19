package com.bcl.carpet.bcladdition.mixin;

import carpet.commands.PlayerCommand;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.server.command.CommandManager.literal;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

/**
 * 在PlayerCommand.register()中拦截argument("player", ...)构建器，
 * 向/player命令添加esc子命令。
 */
@Mixin(PlayerCommand.class)
public class PlayerCommandCloseScreenMixin {

    @WrapOperation(
            method = "register",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;")
    )
    private static RequiredArgumentBuilder<ServerCommandSource, ?> addEsc(
            String name, com.mojang.brigadier.arguments.ArgumentType<?> type,
            Operation<RequiredArgumentBuilder<ServerCommandSource, ?>> original) {

        RequiredArgumentBuilder<ServerCommandSource, ?> builder = original.call(name, type);

        // 规则启用时添加 esc 子命令
        builder.then(literal("esc")
                .requires(src -> BCLAdditionSettings.playerCommandCloseScreen)
                .executes(ctx -> {
                    String playerName = getString(ctx, "player");
                    ServerPlayerEntity target = ctx.getSource().getServer()
                            .getPlayerManager().getPlayer(playerName);
                    if (target == null) {
                        return 0;
                    }
                    target.closeHandledScreen();
                    return 1;
                })
        );

        return builder;
    }
}
