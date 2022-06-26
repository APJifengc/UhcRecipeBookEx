package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.customitems.GameItem;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.TimeUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static org.bukkit.Bukkit.broadcastMessage;


public class CheckMainhandThread implements Runnable{

    private final Player player;
    private final List<String> oldLores;
    private final UhcPlayer uhcPlayer;
    private final GameManager gm;

    public CheckMainhandThread(Player player, List<String> oldLores, UhcPlayer uhcplayer, GameManager gm){
        this.player = player;
        this.oldLores = oldLores;
        this.uhcPlayer = uhcplayer;
        this.gm = gm;
    }

    @Override
    public void run() {
        ItemStack hand = player.getInventory().getItemInMainHand();
        List<String> lores = null;
        if(hand.getItemMeta()!=null){
            lores = hand.getItemMeta().getLore();
        }

        if (lores!=null && lores!=oldLores) {
            if (lores.contains(Lang.ITEMS_ANDURIL)){
                handleAnduril(player);
            }
            if (lores.contains(Lang.ITEMS_MINER)){
                handleMiner(player);
            }
            if (lores.contains(Lang.ITEMS_APPRENTICE_SWORD)){
                if (gm.getGameState().equals(GameState.PLAYING) && gm.getElapsedTime() >= 10*60 && !(gm.getElapsedTime() >= 25*60)){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
                if (gm.getGameState().equals(GameState.PLAYING) && gm.getElapsedTime() >= 25*60){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
                if (gm.getGameState().equals(GameState.DEATHMATCH)){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
            }
            if (lores.contains(Lang.ITEMS_APPRENTICE_BOW)){
                if (gm.getGameState().equals(GameState.PLAYING) && gm.getElapsedTime() >= 10*60 && !(gm.getElapsedTime() >= 25*60)){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
                if (gm.getGameState().equals(GameState.PLAYING) && gm.getElapsedTime() >= 25*60){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.ARROW_DAMAGE, 2, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
                if (gm.getGameState().equals(GameState.DEATHMATCH)){
                    ItemMeta apprenticeSwordMeta = hand.getItemMeta();
                    apprenticeSwordMeta.addEnchant(Enchantment.ARROW_DAMAGE, 3, true);
                    hand.setItemMeta(apprenticeSwordMeta);
                }
            }
            if (lores.contains(Lang.ITEMS_BLOODLUST)){
                if (uhcPlayer.getKills() >= 1) {
                    ItemMeta bloodlustMeta = hand.getItemMeta();
                    if (uhcPlayer.getKills() >= 1
                            && uhcPlayer.getKills() <= 2) {
                        bloodlustMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
                    }
                    if (uhcPlayer.getKills() >= 3
                            && uhcPlayer.getKills() <= 5) {
                        bloodlustMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
                    }
                    if (uhcPlayer.getKills() >= 6
                            && uhcPlayer.getKills() <= 9) {
                        bloodlustMeta.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
                    }
                    if (uhcPlayer.getKills() >= 10) {
                        bloodlustMeta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
                    }
                    hand.setItemMeta(bloodlustMeta);
                }
            }
        }
    }

    public static void handleAnduril(Player player){
        new BukkitRunnable(){
            public void run(){
                ItemStack hand = player.getInventory().getItemInMainHand();
                List<String> newLores = null;
                if(hand.getItemMeta()!=null){
                    newLores = hand.getItemMeta().getLore();
                }
                if(newLores==null||!newLores.contains(Lang.ITEMS_ANDURIL)){
                    this.cancel();
                }else{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 39, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 39, 0));
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,2);
    }

    public static void handleMiner(Player player){
        new BukkitRunnable(){
            public void run(){
                ItemStack hand = player.getInventory().getItemInMainHand();
                List<String> newLores = null;
                if(hand.getItemMeta()!=null){
                    newLores = hand.getItemMeta().getLore();
                }
                if(newLores==null||!newLores.contains(Lang.ITEMS_MINER)){
                    this.cancel();
                }else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 39, 0));
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,2);
    }
}
