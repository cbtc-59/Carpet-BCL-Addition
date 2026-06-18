package com.bcl.carpet.bcladdition.mixin.autorestock;

import carpet.patches.EntityPlayerMPFake;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.bcl.carpet.bcladdition.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a fake player uses an item (place block, throw potion, etc.),
 * auto-restock from inventory after use.
 */
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerAutoRestockMixin {

    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "interactItem", at = @At("TAIL"))
    private void onUseItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand,
                            CallbackInfoReturnable<ActionResult> cir) {
        restock(stack, hand, cir.getReturnValue());
    }

    @Inject(method = "interactBlock", at = @At("TAIL"))
    private void onUseItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack,
                                   Hand hand, BlockHitResult hitResult,
                                   CallbackInfoReturnable<ActionResult> cir) {
        restock(stack, hand, cir.getReturnValue());
    }

    private void restock(ItemStack usedStack, Hand hand, ActionResult result) {
        if (!BCLAdditionSettings.fakePlayerAutoRestock) return;
        if (!(this.player instanceof EntityPlayerMPFake)) return;
        if (!result.isAccepted()) return;

        ItemStack handStack = this.player.getStackInHand(hand);
        if (InventoryUtils.isFragileWithMending(usedStack)) {
            InventoryUtils.replenish(this.player.getInventory(), hand,
                    stack -> stack.getItem() == usedStack.getItem()
                            && !InventoryUtils.isFragileWithMending(stack));
        } else if (handStack.isEmpty()) {
            InventoryUtils.replenish(this.player.getInventory(), hand,
                    stack -> ItemStack.areItemsAndComponentsEqual(stack, usedStack));
        } else {
            InventoryUtils.replenish(this.player.getInventory(), hand,
                    Math.max(1, handStack.getMaxCount() / 2));
        }
    }
}
