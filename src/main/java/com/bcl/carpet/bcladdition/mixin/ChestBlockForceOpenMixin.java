package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Force-open chests and trapped chests when blocked by a solid block.
 * Only active in 'any' mode.
 */
@Mixin(ChestBlock.class)
public class ChestBlockForceOpenMixin {

    @Inject(method = "isChestBlocked", at = @At("HEAD"), cancellable = true)
    private static void forceOpenChest(WorldAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if ("any".equals(BCLAdditionSettings.forceOpenContainer)) {
            cir.setReturnValue(false);
        }
    }
}
