package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class EndPortalFrameBlockStateMixin {

    @Inject(method = "getHardness", at = @At("RETURN"), cancellable = true)
    private void modifyGetHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState self = (BlockState) (Object) this;
        if (self.getBlock() instanceof EndPortalFrameBlock) {
            if (BCLAdditionSettings.mineableEndPortalFrame) {
                cir.setReturnValue(50.0F);
            }
        }
    }
}
