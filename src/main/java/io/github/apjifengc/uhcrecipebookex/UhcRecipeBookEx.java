package io.github.apjifengc.uhcrecipebookex;

import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.listeners.ItemsListener;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import io.github.apjifengc.uhcrecipebookex.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class UhcRecipeBookEx extends JavaPlugin implements Listener {

    @Getter
    private static UhcRecipeBookEx instance;

    @Getter
    private static CraftRecipeInventory recipeInventory;

    public UhcRecipeBookEx() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config.loadConfig();
        new PlayerListener();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (GameManager.getGameManager().getGameState() != null) {
            // If you use yum or other plugins, the UhcCore plugin will be already loaded, so load immediately.
            load();
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onUhcLoad(UhcGameStateChangedEvent event) {
        if (event.getNewGameState() == GameState.WAITING) {
            load();
        }
    }

    private void load() {
        recipeInventory = new CraftRecipeInventory();
        // Remove the default listener for the book item.
        for (RegisteredListener listener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
            if (listener.getListener() instanceof ItemsListener) {
                PlayerInteractEvent.getHandlerList().unregister(listener);
            }
        }
        Bukkit.getPluginCommand("craft").setExecutor(new CraftsCommandExecutor());
    }
}
