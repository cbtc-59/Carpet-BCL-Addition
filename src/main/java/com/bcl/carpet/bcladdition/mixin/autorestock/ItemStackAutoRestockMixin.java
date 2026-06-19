package com.bcl.carpet.bcladdition.mixin.autorestock;

import carpet.patches.EntityPlayerMPFake;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.bcl.carpet.bcladdition.util.InventoryUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 假玩家的工具损坏时，自动从背包找到相同的工具替换到手上。
 */
@Mixin(ItemStack.class)
public class ItemStackAutoRestockMixin {

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V", at = @At("TAIL"))
    private void onToolBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (!self.isEmpty()) {
            return;
        }
        if (!BCLAdditionSettings.fakePlayerAutoRestock) {
            return;
        }
        if (!(entity instanceof EntityPlayerMPFake fakePlayer)) {
            return;
        }

        Hand hand = (slot == EquipmentSlot.MAINHAND) ? Hand.MAIN_HAND :
                    (slot == EquipmentSlot.OFFHAND) ? Hand.OFF_HAND : null;
        if (hand == null) {
            return;
        }

        // 从背包中找相同工具替换（避开有经验修补的工具）
        InventoryUtils.replenish(fakePlayer.getInventory(), hand,
                stack -> stack.isOf(self.getItem()) && !InventoryUtils.isFragileWithMending(stack));
    }
}
