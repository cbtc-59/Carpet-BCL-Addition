package com.bcl.carpet.bcladdition.mixin.autorestock;

import carpet.patches.EntityPlayerMPFake;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.bcl.carpet.bcladdition.util.InventoryUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a fake player finishes using an item (eating, drinking, etc.),
 * auto-restock from inventory.  Also handles totem of undying consumption.
 */
@Mixin(LivingEntity.class)
public class LivingEntityAutoRestockMixin {

    @Inject(method = "tryUseTotem", at = @At("RETURN"))
    private void onTotemUse(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!BCLAdditionSettings.fakePlayerAutoRestock) return;
        if (!((Object) this instanceof EntityPlayerMPFake fakePlayer)) return;

        // Restock totem in offhand
        InventoryUtils.replenish(fakePlayer.getInventory(), Hand.OFF_HAND,
                stack -> stack.getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING);
    }
}
