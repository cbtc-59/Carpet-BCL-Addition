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
 * Forces end portal frame block items to drop when mined.
 *
 * Needed because the {@code needs_iron_tool} block tag is not loaded at runtime
 * in the Fabric dev environment, which prevents the vanilla loot table + tool
 * check from working correctly. Without this mixin, no items would drop.
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
