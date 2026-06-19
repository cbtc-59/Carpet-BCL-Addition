package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
        Block block = self.getBlock();

        // 末地传送门框架 → 可挖掘时设为黑曜石硬度
        if (block instanceof EndPortalFrameBlock) {
            if (BCLAdditionSettings.mineableEndPortalFrame) {
                cir.setReturnValue(50.0F);
            }
        }

        // 远古残骸和下界合金块 → 开启易碎后降为 1/18 硬度
        if (BCLAdditionSettings.softNetherite) {
            if (block == Blocks.ANCIENT_DEBRIS || block == Blocks.NETHERITE_BLOCK) {
                cir.setReturnValue(cir.getReturnValue() / 18.0F);
            }
        }
    }
}
