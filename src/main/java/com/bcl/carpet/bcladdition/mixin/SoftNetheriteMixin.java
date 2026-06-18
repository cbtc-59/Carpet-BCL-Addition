package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reduces hardness of ancient debris and netherite blocks to 1/18
 * when {@code softNetherite} rule is enabled.
 */
@Mixin(AbstractBlock.class)
public abstract class SoftNetheriteMixin {

    @Inject(method = "getHardness", at = @At("RETURN"), cancellable = true)
    private void modifyGetHardness(CallbackInfoReturnable<Float> cir) {
        if (!BCLAdditionSettings.softNetherite) return;
        if ((Object) this == Blocks.ANCIENT_DEBRIS || (Object) this == Blocks.NETHERITE_BLOCK) {
            cir.setReturnValue(cir.getReturnValue() / 18.0F);
        }
    }
}
