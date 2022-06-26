package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerDamageHandler {

    private final GameManager gameManager;
    private final PlayerManager playerManager;

    public PlayerDamageHandler(GameManager gameManager, PlayerManager playerManager){
        this.gameManager = gameManager;
        this.playerManager = playerManager;
    }

    public Map<UhcPlayer, UhcPlayer> playerLastDamager = new HashMap<>();


    public void setLastKiller(UhcPlayer uhcDamaged, UhcPlayer uhcDamager){
        playerLastDamager.put(uhcDamaged, uhcDamager);
    }

    public UhcPlayer getLastKiller(UhcPlayer uhcDamaged){
        return playerLastDamager.getOrDefault(uhcDamaged, null);
    }
}
