package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxForceOpenMixin {
    @Inject(method = "canOpen", at = @At("HEAD"), cancellable = true)
    private static void forceOpen(BlockState state, World world, BlockPos pos,
                                   ShulkerBoxBlockEntity entity, CallbackInfoReturnable<Boolean> cir) {
        String mode = BCLAdditionSettings.forceOpenContainer;
        if ("any".equals(mode) || "shulker_box".equals(mode)) {
            cir.setReturnValue(true);
        }
    }
}
