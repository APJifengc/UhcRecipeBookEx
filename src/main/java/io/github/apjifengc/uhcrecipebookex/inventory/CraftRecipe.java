package io.github.apjifengc.uhcrecipebookex.inventory;

import com.gmail.val59000mc.customitems.Craft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class CraftRecipe {
    @Getter
    int limit;

    @Getter
    ItemStack craft;

    @Getter
    Craft realCraft;


    public boolean hasLimit() {
        return limit != -1;
    }
}
