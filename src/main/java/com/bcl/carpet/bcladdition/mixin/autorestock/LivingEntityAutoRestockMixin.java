package com.bcl.carpet.bcladdition.mixin.autorestock;

import carpet.patches.EntityPlayerMPFake;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.bcl.carpet.bcladdition.util.InventoryUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 假玩家使用物品（吃、喝、不死图腾等）后，自动从背包补货。
 */
@Mixin(LivingEntity.class)
public class LivingEntityAutoRestockMixin {

    @Inject(method = "tryUseTotem", at = @At("RETURN"))
    private void onTotemUse(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }
        if (!BCLAdditionSettings.fakePlayerAutoRestock) {
            return;
        }
        if (!((Object) this instanceof EntityPlayerMPFake fakePlayer)) {
            return;
        }

        // 补充不死图腾到副手
        InventoryUtils.replenish(fakePlayer.getInventory(), Hand.OFF_HAND,
                stack -> stack.getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING);
    }
}
