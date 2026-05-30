package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class EndPortalFrameMiningSpeedMixin {

    @Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void fixEndPortalFrameMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (!(state.getBlock() instanceof EndPortalFrameBlock)) {
            return;
        }
        if (!BCLAdditionSettings.mineableEndPortalFrame) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof PickaxeItem) {
            ToolComponent tool = self.get(DataComponentTypes.TOOL);
            if (tool != null) {
                // The tag system doesn't recognize end_portal_frame as pickaxe-mineable,
                // so getSpeed() returns the default (1.0F). Use stone as a proxy to get
                // the pickaxe's actual mining speed from the tool component rule system.
                float correctSpeed = tool.getSpeed(Blocks.STONE.getDefaultState());
                if (correctSpeed > 1.0F) {
                    cir.setReturnValue(correctSpeed);
                }
            }
        }
    }
}
