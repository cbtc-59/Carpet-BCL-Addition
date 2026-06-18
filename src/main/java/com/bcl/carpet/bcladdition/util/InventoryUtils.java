package com.bcl.carpet.bcladdition.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Inventory utilities for fake player auto restock.
 */
public class InventoryUtils {

    /**
     * Find and move an item from inventory (including shulker boxes)
     * to the specified hand. Returns true if restock was successful.
     */
    /**
     * Replenish with a target count (finds compatible items by type matching hand stack).
     */
    public static boolean replenish(PlayerInventory inventory, Hand hand, int targetCount) {
        ItemStack handStack = inventory.player.getStackInHand(hand);
        if (handStack.isEmpty()) return false;
        return replenish(inventory, hand,
                stack -> ItemStack.areItemsAndComponentsEqual(stack, handStack));
    }

    /**
     * Find and move an item from inventory (including shulker boxes)
     * to the specified hand. Returns true if restock was successful.
     */
    public static boolean replenish(PlayerInventory inventory, Hand hand, Predicate<ItemStack> match) {
        ItemStack handStack = inventory.player.getStackInHand(hand);
        int targetCount = Math.max(1, handStack.isEmpty() ? 64 : handStack.getMaxCount() / 2);

        // Try main inventory first
        int freeSlot = findAndMove(inventory, match, hand, targetCount, false);
        if (freeSlot >= 0) return true;

        // Try shulker boxes in inventory
        return replenishFromShulkerBoxes(inventory, match, hand, targetCount);
    }

    /**
     * Try to find matching items in shulker boxes and move to hand.
     */
    private static boolean replenishFromShulkerBoxes(PlayerInventory inventory,
                                                      Predicate<ItemStack> match,
                                                      Hand hand, int targetCount) {
        List<ItemStack> mainAndOffhand = new ArrayList<>();
        for (int i = 0; i < inventory.main.size(); i++) {
            mainAndOffhand.add(inventory.main.get(i));
        }
        for (ItemStack stack : mainAndOffhand) {
            if (isOpenableShulkerBox(stack)) {
                ItemStack extracted = extractFromShulkerBox(stack, match, 1);
                if (!extracted.isEmpty()) {
                    // Put extracted item into hand or merge
                    ItemStack handStack = inventory.player.getStackInHand(hand);
                    if (handStack.isEmpty()) {
                        inventory.player.setStackInHand(hand, extracted);
                    } else if (ItemStack.areItemsAndComponentsEqual(handStack, extracted)) {
                        int space = handStack.getMaxCount() - handStack.getCount();
                        int move = Math.min(space, extracted.getCount());
                        if (move > 0) {
                            handStack.increment(move);
                            extracted.decrement(move);
                            if (!extracted.isEmpty()) {
                                int slot = inventory.getEmptySlot();
                                if (slot >= 0) inventory.setStack(slot, extracted);
                            }
                        }
                    } else {
                        int slot = inventory.getEmptySlot();
                        if (slot >= 0) inventory.setStack(slot, extracted);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract items from a shulker box that match the predicate.
     */
    private static ItemStack extractFromShulkerBox(ItemStack shulker, Predicate<ItemStack> match, int count) {
        ContainerComponent contents = shulker.get(DataComponentTypes.CONTAINER);
        if (contents == null) return ItemStack.EMPTY;

        // Copy to mutable list for modification
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
        contents.copyTo(stacks);

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty() && match.test(stack)) {
                int toTake = Math.min(count, stack.getCount());
                ItemStack result = stack.copyWithCount(toTake);
                stack.decrement(toTake);

                // Write modified contents back to shulker box
                shulker.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static int findAndMove(PlayerInventory inventory, Predicate<ItemStack> match,
                                    Hand hand, int targetCount, boolean skipHand) {
        ItemStack handStack = inventory.player.getStackInHand(hand);
        int handSlot = hand == Hand.MAIN_HAND ? inventory.selectedSlot : PlayerInventory.OFF_HAND_SLOT;

        for (int i = 0; i < inventory.main.size() + 1; i++) {
            int idx = (i < inventory.main.size()) ? i : PlayerInventory.OFF_HAND_SLOT;
            if (idx == handSlot && skipHand) continue;

            ItemStack stack = i < inventory.main.size() ? inventory.main.get(i) : inventory.offHand.get(0);
            if (stack.isEmpty() || !match.test(stack)) continue;
            if (stack == handStack) continue;

            if (handStack.isEmpty()) {
                int toMove = Math.min(targetCount, stack.getCount());
                ItemStack moved = stack.split(toMove);
                inventory.player.setStackInHand(hand, moved);
                return idx;
            } else if (ItemStack.areItemsAndComponentsEqual(stack, handStack)) {
                int space = handStack.getMaxCount() - handStack.getCount();
                int toMove = Math.min(space, stack.getCount());
                if (toMove > 0) {
                    stack.decrement(toMove);
                    handStack.increment(toMove);
                    return idx;
                }
            }
        }
        return -1;
    }

    public static boolean isOpenableShulkerBox(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) return false;
        ContainerComponent contents = stack.get(DataComponentTypes.CONTAINER);
        return contents != null;
    }

    public static boolean isFragileWithMending(ItemStack stack) {
        return stack.isDamageable() && stack.hasEnchantments();
    }
}
