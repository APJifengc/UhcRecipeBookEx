package io.github.apjifengc.uhcrecipebookex.inventory;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CraftRecipeInventoryHolder implements InventoryHolder {
    @Getter
    private final int page;

    public CraftRecipeInventoryHolder(int page) {
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
