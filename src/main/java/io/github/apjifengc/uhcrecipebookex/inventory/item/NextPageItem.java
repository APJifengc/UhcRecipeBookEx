package io.github.apjifengc.uhcrecipebookex.inventory.item;

import io.github.apjifengc.uhcrecipebookex.Config;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class NextPageItem extends NormalItem {
    private final Character fallbackItem;

    public NextPageItem(ConfigurationSection section, Character character) throws IllegalArgumentException {
        super(section, character);
        fallbackItem = section.getString("fallback", " ").charAt(0);
    }

    @Override
    public ItemStack getItemStack(int page) {
        if (page == CraftRecipeInventory.getLastPage()) {
            return Config.GUI_ITEM_MAP.get(fallbackItem).getItemStack(page);
        } else {
            return super.getItemStack(page);
        }
    }
}
