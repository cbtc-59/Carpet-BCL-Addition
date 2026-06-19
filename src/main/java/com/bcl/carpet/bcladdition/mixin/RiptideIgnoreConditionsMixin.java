package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 允许三叉戟激流在不接触水或雨时也能使用。
 * 拦截 TridentItem 方法中对 isTouchingWaterOrRain() 的调用，
 * 规则启用时强制返回 true。
 */
@Mixin(TridentItem.class)
public class RiptideIgnoreConditionsMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z")
    )
    private boolean forceWaterOrRainOnUse(PlayerEntity player, Operation<Boolean> original) {
        if (BCLAdditionSettings.riptideIgnoreConditions) {
            return true;
        }
        return original.call(player);
    }

    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z")
    )
    private boolean forceWaterOrRainOnStoppedUsing(PlayerEntity player, Operation<Boolean> original) {
        if (BCLAdditionSettings.riptideIgnoreConditions) {
            return true;
        }
        return original.call(player);
    }
}
