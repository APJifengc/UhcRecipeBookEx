package io.github.apjifengc.uhcrecipebookex.inventory;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CraftRecipeViewerInventoryHolder implements InventoryHolder {
    @Getter
    private final Inventory lastInventory;

    public CraftRecipeViewerInventoryHolder(Inventory lastInventory) {
        this.lastInventory = lastInventory;
    }
    @Override
    public Inventory getInventory() {
        return lastInventory;
    }
}
