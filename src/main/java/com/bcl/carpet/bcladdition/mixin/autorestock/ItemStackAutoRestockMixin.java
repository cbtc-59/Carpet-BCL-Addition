package com.bcl.carpet.bcladdition.mixin.autorestock;

import carpet.patches.EntityPlayerMPFake;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.bcl.carpet.bcladdition.util.InventoryUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a fake player's tool breaks, auto-replace with same tool from inventory.
 */
@Mixin(ItemStack.class)
public class ItemStackAutoRestockMixin {

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V", at = @At("TAIL"))
    private void onToolBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (!self.isEmpty()) return;
        if (!BCLAdditionSettings.fakePlayerAutoRestock) return;
        if (!(entity instanceof EntityPlayerMPFake fakePlayer)) return;

        Hand hand = (slot == EquipmentSlot.MAINHAND) ? Hand.MAIN_HAND :
                    (slot == EquipmentSlot.OFFHAND) ? Hand.OFF_HAND : null;
        if (hand == null) return;

        // Restock: find same type of tool from inventory (avoid mending tools)
        InventoryUtils.replenish(fakePlayer.getInventory(), hand,
                stack -> stack.isOf(self.getItem()) && !InventoryUtils.isFragileWithMending(stack));
    }
}
