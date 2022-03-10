package io.github.apjifengc.uhcrecipebookex.inventory.item;

import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Placeholder. It will be replaced in the {@link CraftRecipeInventory}.
 */
public class RecipeSlotItem extends InventoryItem {
    @Getter
    private final int slot;

    public RecipeSlotItem(int slot) throws IllegalArgumentException {
        this.slot = slot;
    }

    @Override
    public ItemStack getItemStack(int page) {
        return null;
    }
}
