package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.EndPortalFrameBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class EndPortalFrameBlockMixin {

    @Inject(method = "getHardness", at = @At("RETURN"), cancellable = true)
    private void modifyGetHardness(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof EndPortalFrameBlock) {
            if (BCLAdditionSettings.mineableEndPortalFrame) {
                cir.setReturnValue(50.0F);
            }
        }
    }
}
