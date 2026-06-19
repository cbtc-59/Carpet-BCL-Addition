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
 * 假玩家自动补货的背包工具类。
 */
public class InventoryUtils {

    /**
     * 按目标数量补货——查找与手上物品类型匹配的物品。
     */
    public static boolean replenish(PlayerInventory inventory, Hand hand, int targetCount) {
        ItemStack handStack = inventory.player.getStackInHand(hand);
        if (handStack.isEmpty()) {
            return false;
        }
        return replenish(inventory, hand,
                stack -> ItemStack.areItemsAndComponentsEqual(stack, handStack));
    }

    /**
     * 从背包（含潜影盒）找到匹配的物品并移动到指定手上。
     */
    public static boolean replenish(PlayerInventory inventory, Hand hand, Predicate<ItemStack> match) {
        ItemStack handStack = inventory.player.getStackInHand(hand);
        int targetCount = Math.max(1, handStack.isEmpty() ? 64 : handStack.getMaxCount() / 2);

        // 先从主物品栏找
        int freeSlot = findAndMove(inventory, match, hand, targetCount, false);
        if (freeSlot >= 0) {
            return true;
        }

        // 再从背包里的潜影盒找
        return replenishFromShulkerBoxes(inventory, match, hand, targetCount);
    }

    /**
     * 从背包里的潜影盒中查找匹配物品并移动到手上。
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
                    // 将取出的物品放到手上或合并到手上已有的堆叠
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
                                if (slot >= 0) {
                                    inventory.setStack(slot, extracted);
                                }
                            }
                        }
                    } else {
                        int slot = inventory.getEmptySlot();
                        if (slot >= 0) {
                            inventory.setStack(slot, extracted);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从潜影盒中取出匹配的物品。
     */
    private static ItemStack extractFromShulkerBox(ItemStack shulker, Predicate<ItemStack> match, int count) {
        ContainerComponent contents = shulker.get(DataComponentTypes.CONTAINER);
        if (contents == null) {
            return ItemStack.EMPTY;
        }

        // 复制到可变列表以便修改
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
        contents.copyTo(stacks);

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty() && match.test(stack)) {
                int toTake = Math.min(count, stack.getCount());
                ItemStack result = stack.copyWithCount(toTake);
                stack.decrement(toTake);

                // 将修改后的内容写回潜影盒
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
            if (idx == handSlot && skipHand) {
                continue;
            }

            ItemStack stack = i < inventory.main.size() ? inventory.main.get(i) : inventory.offHand.get(0);
            if (stack.isEmpty() || !match.test(stack)) {
                continue;
            }
            if (stack == handStack) {
                continue;
            }

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
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }
        ContainerComponent contents = stack.get(DataComponentTypes.CONTAINER);
        return contents != null;
    }

    public static boolean isFragileWithMending(ItemStack stack) {
        return stack.isDamageable() && stack.hasEnchantments();
    }
}
