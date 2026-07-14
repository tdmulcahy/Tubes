package mod.tubes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {

    /**
     * Attempts to insert the given ItemStack into the given Container.
     * <p>
     * This will handle reducing the size of the given ItemStack automatically if needed.
     *
     * @param container The Container to insert in to.
     * @param itemStack The ItemStack to insert.
     * @return Returns true if the whole stack was inserted, false if not.
     */
    public static boolean insertStackIntoContainer(Container container, ItemStack itemStack) {

        int slots = container.getContainerSize();
        for (int slot = 0; slot < slots; slot++) {

            // Does the slot accept the ItemStack.
            if (!container.canPlaceItem(slot, itemStack)) continue;

            // If the slot does, get the max stack size of the slot.
            int maxStackSize = container.getMaxStackSize();

            ItemStack current = container.getItem(slot);
            if (current.isEmpty()) {
                if (itemStack.getCount() <= maxStackSize) {
                    container.setItem(slot, itemStack);

                    return true; // The whole stack has been inserted here.
                } else {
                    ItemStack copy = itemStack.copy();
                    copy.setCount(maxStackSize);

                    container.setItem(slot, copy);
                    itemStack.shrink(maxStackSize); // Shrink current stack by the amount inserted.
                }
            } else {
                if (ItemStack.isSameItemSameComponents(current, itemStack)) {

                    // Can the whole stack be added to the current?
                    if (current.getCount() + itemStack.getCount() <= maxStackSize) {
                        current.grow(itemStack.getCount());

                        return true; // The whole stack has been inserted here.
                    } else {
                        int insertedAmount = maxStackSize - current.getCount();
                        current.grow(maxStackSize);

                        itemStack.shrink(insertedAmount);
                    }
                }
            }
        }

        return false;
    }
}
