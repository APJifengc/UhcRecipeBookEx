package io.github.apjifengc.uhcrecipebookex.inventory;

import com.gmail.val59000mc.customitems.Craft;
import com.gmail.val59000mc.customitems.CraftsManager;
import io.github.apjifengc.uhcrecipebookex.Config;
import io.github.apjifengc.uhcrecipebookex.inventory.item.InventoryItem;
import io.github.apjifengc.uhcrecipebookex.inventory.item.RecipeSlotItem;
import io.github.apjifengc.uhcrecipebookex.inventory.item.SlotItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CraftRecipeInventory {
    @Getter
    private static final int firstPage = 0;

    @Getter
    private static int lastPage;

    @Getter
    private final List<Integer> slots = new ArrayList<>();

    @Getter
    private final Map<Integer, Integer> slotId = new HashMap<>();

    @Getter
    private final List<Craft> crafts;

    ItemStack addInfo(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();
        ItemMeta meta = clone.getItemMeta();
        List<String> lores = meta.getLore();
        if (lores == null) lores = new ArrayList<>();
        lores.add("");
        lores.add(Config.MESSAGE.getString("right-click-open").replace("&", "\u00A7"));
        meta.setLore(lores);
        clone.setItemMeta(meta);
        return clone;
    }

    public InventoryItem getInventoryItem(int slot) {
        return getInventoryItem(slot / 9, slot % 9);
    }

    public InventoryItem getInventoryItem(int i, int j) {
        return Config.GUI_ITEM_MAP.get(Config.INVENTORY_PATTERN.get(i).charAt(j));
    }

    public Inventory createMainInventory(int page) {
        Inventory gui = Bukkit.createInventory(new CraftRecipeInventoryHolder(page), Config.INVENTORY_PATTERN.size() * 9,
                Config.GUI_NAME.replace("{page_num}", String.valueOf(page + 1))
                        .replace("&", "\u00A7")
                );
        for (int i = 0; i < Config.INVENTORY_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = getInventoryItem(i, j);
                if (item instanceof SlotItem) {
                    int craftId = page * slots.size() + slotId.get(i * 9 + j);
                    if (craftId >= crafts.size()) {
                        gui.setItem(i * 9 + j, item.getItemStack(page));
                    } else {
                        gui.setItem(i * 9 + j, addInfo(crafts.get(craftId).getDisplayItem()));
                    }
                } else {
                    gui.setItem(i * 9 + j, item.getItemStack(page));
                }
            }
        }
        return gui;
    }

    public Inventory createRecipeViewerInventory(Craft craft, Inventory lastInventory) {
        Inventory gui = Bukkit.createInventory(new CraftRecipeViewerInventoryHolder(lastInventory), Config.RECIPE_VIEWER_PATTERN.size() * 9,
                Config.GUI_RECIPE_VIEWER_NAME.replace("{item_name}", craft.getName())
                        .replace("&", "\u00A7")
                );
        for (int i = 0; i < Config.RECIPE_VIEWER_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = getInventoryItem(i, j);
                if (item instanceof RecipeSlotItem) {
                    int slot = ((RecipeSlotItem) item).getSlot();
                    if (slot == 0) {
                        gui.setItem(i * 9 + j, craft.getCraft());
                    } else {
                        gui.setItem((1 + i) * 9 + (1 + j), craft.getRecipe().get(slot - 1));
                    }
                } else {
                    gui.setItem(i * 9 + j, item.getItemStack(0));
                }
            }
        }
        return gui;
    }

    public CraftRecipeInventory() {
        crafts = CraftsManager.getCrafts().stream()
                .filter(craft -> !Config.IGNORE_CRAFTS.contains(craft.getName()))
                .collect(Collectors.toList());
        for (int i = 0; i < Config.INVENTORY_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                if (Config.GUI_ITEM_MAP.get(Config.INVENTORY_PATTERN.get(i).charAt(j)) instanceof SlotItem) {
                    slotId.put(i * 9 + j, slots.size());
                    slots.add(i * 9 + j);
                }
            }
        }
        lastPage = (crafts.size() - 1) / slots.size();
    }
}
