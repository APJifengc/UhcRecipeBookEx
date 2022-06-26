package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.threads.TimeBeforeDeathmatchThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetScenarioCommandExecutor implements CommandExecutor{

    private final ScenarioManager scenarioManager;
    private final GameManager gameManager;

    public SetScenarioCommandExecutor(GameManager gameManager, ScenarioManager scenarioManager){
        this.gameManager = gameManager;
        this.scenarioManager = scenarioManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1){
            sender.sendMessage(ChatColor.RED + "Usage: /setscenario <scenario>");
            return true;
        }

        String scenario = args[0];

        scenarioManager.getScenarioByName(scenario)
                .ifPresent(scenarioManager::enableScenario);

        sender.sendMessage(ChatColor.GREEN + "OK!");
        return true;
    }

}