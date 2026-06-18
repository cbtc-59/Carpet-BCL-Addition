package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderChestBlock.class)
public class EnderChestForceOpenMixin {
    @WrapOperation(
            method = "onUse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isSolidBlock(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z")
    )
    private boolean forceOpen(BlockState state, BlockView world, BlockPos pos, Operation<Boolean> original) {
        if ("any".equals(BCLAdditionSettings.forceOpenContainer)) {
            return false;
        }
        return original.call(state, world, pos);
    }
}
