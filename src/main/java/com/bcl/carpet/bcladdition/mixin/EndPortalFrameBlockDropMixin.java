package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 强制末地传送门框架在挖掘后掉落方块物品。
 *
 * 因为needs_iron_tool方块标签在Fabric开发环境中不加载，
 * 导致原版战利品表+工具检查无法正常工作。没有这个Mixin就不会掉落。
 */
@Mixin(Block.class)
public abstract class EndPortalFrameBlockDropMixin {

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void ensureEndPortalFrameDrop(World world, PlayerEntity player, BlockPos pos,
                                          BlockState state,
                                          @Nullable BlockEntity blockEntity, ItemStack tool,
                                          CallbackInfo ci) {
        if ((Object) this instanceof EndPortalFrameBlock) {
            if (BCLAdditionSettings.mineableEndPortalFrame) {
                if (!world.isClient && !player.isCreative()) {
                    Block.dropStack(world, pos, new ItemStack(Blocks.END_PORTAL_FRAME));
                }
            }
        }
    }
}
