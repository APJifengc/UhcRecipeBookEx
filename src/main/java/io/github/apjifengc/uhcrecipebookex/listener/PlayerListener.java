package io.github.apjifengc.uhcrecipebookex.listener;

import com.gmail.val59000mc.customitems.Craft;
import com.gmail.val59000mc.customitems.GameItem;
import io.github.apjifengc.uhcrecipebookex.UhcRecipeBookEx;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventoryHolder;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeViewerInventoryHolder;
import io.github.apjifengc.uhcrecipebookex.inventory.item.NextPageItem;
import io.github.apjifengc.uhcrecipebookex.inventory.item.PreviousPageItem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final UhcRecipeBookEx plugin = UhcRecipeBookEx.getInstance();

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack hand = event.getItem();
        if (hand != null && GameItem.isGameItem(hand) && GameItem.getGameItem(hand) == GameItem.CUSTOM_CRAFT_BOOK) {
            event.setCancelled(true);
            event.getPlayer().openInventory(UhcRecipeBookEx.getRecipeInventory().createMainInventory(0));
        }
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CraftRecipeInventoryHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                CraftRecipeInventory recipe = UhcRecipeBookEx.getRecipeInventory();
                CraftRecipeInventoryHolder holder = (CraftRecipeInventoryHolder) event.getView().getTopInventory().getHolder();
                if (recipe.getSlotId().containsKey(event.getSlot())) {
                    int craftId = holder.getPage() * recipe.getSlots().size()
                            + recipe.getSlotId().get(event.getSlot());
                    Craft craft = recipe.getCrafts().get(craftId);
                    event.getWhoClicked().openInventory(recipe.createRecipeViewerInventory(craft, event.getClickedInventory()));
                } else if (recipe.getInventoryItem(event.getSlot()) instanceof PreviousPageItem) {
                    if (holder.getPage() != CraftRecipeInventory.getFirstPage()) {
                        event.getWhoClicked().openInventory(recipe.createMainInventory(holder.getPage() - 1));
                    }
                } else if (recipe.getInventoryItem(event.getSlot()) instanceof NextPageItem) {
                    if (holder.getPage() != CraftRecipeInventory.getLastPage()) {
                        event.getWhoClicked().openInventory(recipe.createMainInventory(holder.getPage() + 1));
                    }
                }
            }
        }
        if (event.getView().getTopInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder) {
            CraftRecipeViewerInventoryHolder holder = (CraftRecipeViewerInventoryHolder) event.getInventory().getHolder();
            if (holder.getLastInventory() != null) {
                event.getPlayer().openInventory(holder.getLastInventory());
            }
        }
    }

    @EventHandler
    void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder ||
                event.getInventory().getHolder() instanceof CraftRecipeInventoryHolder) {
            event.setCancelled(true);
        }
    }
}