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
 * 强制打开被方块挡住的箱子和陷阱箱。仅在 any 模式下生效。
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
