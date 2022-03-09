package io.github.apjifengc.uhcrecipebookex;

import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.game.GameState;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import io.github.apjifengc.uhcrecipebookex.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onUhcLoad(UhcGameStateChangedEvent event) {
        if (event.getNewGameState() == GameState.LOADING) {
            recipeInventory = new CraftRecipeInventory();
        }
    }
}
