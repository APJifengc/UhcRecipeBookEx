package io.github.apjifengc.uhcrecipebookex;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.listeners.*;
import com.gmail.val59000mc.players.PlayerManager;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import io.github.apjifengc.uhcrecipebookex.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import com.gmail.val59000mc.configuration.MainConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

    // this is SB(
    private static PlayerListener sbPlayerListener;

    private void load() {
        recipeInventory = new CraftRecipeInventory();
//        PlayerManager playerManager = null;
//
//        try {
//            Plugin uhcCore = getServer().getPluginManager().getPlugin("UhcCore");
//            assert uhcCore != null;
//            ClassLoader classLoader = uhcCore.getPluginLoader().getClass().getClassLoader();
//            Class<?> aClass = classLoader.loadClass("com.gmail.val59000mc.game.GameManager");
//            Field declaredField = aClass.getDeclaredField("gameManager");
//            declaredField.setAccessible(true);
//            GameManager o = (GameManager) declaredField.get(null);
//            playerManager = o.getPlayerManager();
//        } catch (Exception e) {
//            getServer().getLogger().warning("Catch Exp!!!!!!!!!!!!!!");
//            e.printStackTrace();
//            getServer().getLogger().warning("!!!!!!!!!!!!!!!!!!!!!Catch Exp");
//        }
//        assert playerManager != null;
        PlayerManager playerManager = GameManager.getGameManager().getPlayerManager();
        assert playerManager != null;
        sbPlayerListener = new PlayerListener(playerManager);

        // Remove the default listener for the book item.
        for (RegisteredListener listener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
            if (listener.getListener() instanceof ItemsListener) {
                PlayerInteractEvent.getHandlerList().unregister(listener);
            }
        }
        Bukkit.getPluginCommand("craft").setExecutor(new CraftsCommandExecutor());
    }
}


