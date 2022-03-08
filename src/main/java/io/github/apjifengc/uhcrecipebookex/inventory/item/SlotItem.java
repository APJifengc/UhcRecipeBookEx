package io.github.apjifengc.uhcrecipebookex.inventory.item;

import io.github.apjifengc.uhcrecipebookex.Config;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Placeholder. It will be replaced in the {@link CraftRecipeInventory}.
 */
public class SlotItem extends InventoryItem {
    private final Character fallbackItem;

    public SlotItem(ConfigurationSection section) throws IllegalArgumentException {
        fallbackItem = section.getString("fallback", " ").charAt(0);
    }

    @Override
    public ItemStack getItemStack(int page) {
        return Config.GUI_ITEM_MAP.get(fallbackItem).getItemStack(page);
    }
}
