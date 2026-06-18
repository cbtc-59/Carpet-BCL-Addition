package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents wind charges from changing block states.
 * Mixes into Explosion.canTriggerBlocks() to block the trigger effect
 * when the explosion source is a wind charge fired by a player.
 */
@Mixin(Explosion.class)
public class DisableWindChargeEffectMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "canTriggerBlocks", at = @At("HEAD"), cancellable = true)
    private void disableTriggerEffect(CallbackInfoReturnable<Boolean> cir) {
        if (BCLAdditionSettings.disableWindChargeEffect
                && this.entity instanceof WindChargeEntity charge
                && charge.getOwner() instanceof PlayerEntity) {
            cir.setReturnValue(false);
        }
    }
}
