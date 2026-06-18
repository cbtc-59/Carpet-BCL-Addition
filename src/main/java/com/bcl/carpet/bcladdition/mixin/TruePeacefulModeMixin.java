package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents mobs from targeting players when truePeacefulMode is enabled.
 */
@Mixin(MobEntity.class)
public class TruePeacefulModeMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void cancelPlayerTarget(LivingEntity target, CallbackInfo ci) {
        if (BCLAdditionSettings.truePeacefulMode && target instanceof PlayerEntity) {
            ci.cancel();
        }
    }
}
