package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.CustomEventHandler;
import com.gmail.val59000mc.maploader.MapLoader;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;

public class StartingGameThread implements Runnable{

    private final GameManager gameManager;
    private int timeToExecuteCommand;
    private final CustomEventHandler customEventHandler;
    private final MapLoader mapLoader;

    public StartingGameThread(GameManager gameManager, CustomEventHandler customEventHandler, MapLoader mapLoader){
        this.gameManager = gameManager;
        timeToExecuteCommand = gameManager.getConfig().get(MainConfig.TIME_TO_EXECUTE_COMMAND);
        this.customEventHandler = customEventHandler;
        this.mapLoader = mapLoader;
    }

    @Override
    public void run() {

        if(!gameManager.getGameState().equals(GameState.PLAYING)) {
            return; // Stop thread
        }

        if(timeToExecuteCommand == 0){
            customEventHandler.handleStartingGameEvent();
            World overworld = mapLoader.getUhcWorld(World.Environment.NORMAL);
            if (GameManager.getGameManager().getPlayerManager().getOnlinePlayingPlayers().size() <= 16){
                overworld.setDifficulty(Difficulty.NORMAL);
                if (GameManager.getGameManager().getPlayerManager().getOnlinePlayingPlayers().size() <= 8){
                    overworld.setDifficulty(Difficulty.EASY);
                }
            };
            return; // Stop thread
        }

        timeToExecuteCommand --;
        Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), this,20);

    }

}