package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesNotExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.handlers.PlayerDamageHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.TimeUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.swing.plaf.basic.BasicTreeUI;
import java.util.*;

import static org.bukkit.Bukkit.broadcastMessage;

public class PlayerDamageListener implements Listener{

	private final GameManager gameManager;
	private final PlayerManager playerManager;
	private final PlayerDamageHandler playerDamageHandler;
	private final boolean friendlyFire;
	private final Map<UhcPlayer,Long> excaliburUsingLastUpdate = new HashMap<>();
	private final Map<UhcPlayer,Long> perunUsingLastUpdate = new HashMap<>();
	private final Map<Player,Long> deusExMachinaDrunk = new HashMap<>();
	private static final Map<Player,Long> playerNewNaturalRegen = new HashMap<>();

	public PlayerDamageListener(GameManager gameManager, PlayerManager playerManager, PlayerDamageHandler playerDamageHandler){
		this.gameManager = gameManager;
		this.playerManager = playerManager;
		this.playerDamageHandler = playerDamageHandler;
		friendlyFire = gameManager.getConfig().get(MainConfig.ENABLE_FRIENDLY_FIRE);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageByEntityEvent event){
		handlePvpAndFriendlyFire(event);
		handleLightningStrike(event);
		handleProjectiles(event);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageEvent event){
		handleAnyDamage(event);
	}
	
	///////////////////////
	// EntityDamageEvent //
	///////////////////////

	private void handleAnyDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			PlayerManager pm = gameManager.getPlayerManager();
			UhcPlayer uhcPlayer = pm.getUhcPlayer(player);

			PlayerState uhcPlayerState = uhcPlayer.getState();
			if((uhcPlayerState.equals(PlayerState.WAITING)&&player.getLocation().getY() >= 97) || uhcPlayerState.equals(PlayerState.DEAD)){
				event.setCancelled(true);
			}

			if (uhcPlayer.isFrozen()){
				event.setCancelled(true);
			}

			/*if (!event.isCancelled()){
				if (player.getHealth()<10){
					new BukkitRunnable() {
						@Override
						public void run() {
							playerNewNaturalRegen.put(player,System.currentTimeMillis());
							PlayerNaturalRegen(player, playerNewNaturalRegen);
						}
					}.runTaskLater(UhcCore.getPlugin(), 1);
				}
			}*/
		}
	}

	public static void PlayerNaturalRegen(Player player, Map<Player, Long> lastRegen){
		final Map<Player, Long> last = lastRegen;
		new BukkitRunnable(){
			int i = 11;
			public void run(){
				if (i <= 0 || !Objects.equals(playerNewNaturalRegen.getOrDefault(player, -1L), last.getOrDefault(player, -1L))){
					this.cancel();
				}
				else if (player.getHealth()>=10){
					this.cancel();
				}
				else{
					if (i < 11){
						player.setHealth(player.getHealth()+1);
					}
					i--;
				}
			}
		}.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,600);
	}
	
	///////////////////////////////
	// EntityDamageByEntityEvent //
	///////////////////////////////
	
	private void handlePvpAndFriendlyFire(EntityDamageByEntityEvent event){


		PlayerManager pm = gameManager.getPlayerManager();

		
		
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){

			
			Player damager = (Player) event.getDamager();
			Player damaged = (Player) event.getEntity();
			UhcPlayer uhcDamager = pm.getUhcPlayer(damager);
			UhcPlayer uhcDamaged = pm.getUhcPlayer(damaged);

			PlayerState uhcDamagerState = uhcDamager.getState();

			//Fix bug after death
			if(damager.getHealth() <= 0){
				damager.setGameMode(GameMode.SPECTATOR);
			}

			if(!(gameManager.getPvp()||(uhcDamagerState.equals(PlayerState.WAITING) && damager.getLocation().getY() < 97))){
				event.setCancelled(true);
				return;
			}

			if(!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING) && uhcDamager.isInTeamWith(uhcDamaged)){
				damager.sendMessage(Lang.PLAYERS_FF_OFF);
				event.setCancelled(true);
			}

			//Update killer
			if(!uhcDamagerState.equals(PlayerState.WAITING)){
				playerDamageHandler.setLastKiller(uhcDamaged, uhcDamager);
			}

			//Ultimate
			ItemStack hand = damager.getInventory().getItemInMainHand();
			List<String> lores = null;
			if(hand.getItemMeta()!=null){
				lores = hand.getItemMeta().getLore();
			}

			if (lores!=null&& !damaged.isBlocking()) {
				if (lores.contains(Lang.ITEMS_EXCALIBUR)) {
					damageByExcalibur(5,damaged,uhcDamager);
				}
				if (lores.contains(Lang.ITEMS_PERUN)) {
					damageByPerun(8,damaged,uhcDamager);
				}
				if (lores.contains(Lang.ITEMS_DEATH)) {
					damageByDeath(hand, damager, damaged);
				}
			}

			if(15 * TimeUtils.SECOND + deusExMachinaDrunk.getOrDefault(damaged,-1L) > System.currentTimeMillis()){
				event.setCancelled(true);
			}
		}
	}
	
	private void handleLightningStrike(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof LightningStrike && event.getEntity() instanceof Player){
			event.setCancelled(true);
		}
	}
	
	private void handleProjectiles(EntityDamageByEntityEvent event) {
		PlayerManager pm = gameManager.getPlayerManager();
		
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Projectile){
			Projectile projectile = (Projectile) event.getDamager();
			final Player shot = (Player) event.getEntity();
			if(projectile.getShooter() instanceof Player){
				
				if(!gameManager.getPvp()){
					event.setCancelled(true);
					return;
				}

				UhcPlayer uhcDamager = pm.getUhcPlayer((Player) projectile.getShooter());
				UhcPlayer uhcDamaged = pm.getUhcPlayer(shot);

				if(!friendlyFire && uhcDamager.getState().equals(PlayerState.PLAYING) && uhcDamager.isInTeamWith(uhcDamaged)){
					uhcDamager.sendMessage(Lang.PLAYERS_FF_OFF);
					event.setCancelled(true);
				}

				Player shooter = (Player) projectile.getShooter();

				//Fix bug after death
				if(shooter.getHealth() <= 0){
					shooter.setGameMode(GameMode.SPECTATOR);
				}

				if(!uhcDamager.getState().equals(PlayerState.WAITING)){
					playerDamageHandler.setLastKiller(uhcDamaged, uhcDamager);
				}

				if(projectile instanceof Arrow && !shot.isBlocking()) {
					Arrow arrow = (Arrow) projectile;
					if (projectile.getScoreboardTags().contains("Modular Arrow Lightning")) {
						damageByModularLightning(shooter, shot, arrow);
					}
					if (projectile.getScoreboardTags().contains("Modular Arrow Poison")) {
						damageByModularPoison(shooter, shot, arrow);
					}
				}
				if(15 * TimeUtils.SECOND + deusExMachinaDrunk.getOrDefault(shot,-1L) > System.currentTimeMillis()){
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	private void onItemConsume(PlayerItemConsumeEvent event){
		handlePotionDrink(event);
	}

	private void handlePotionDrink(PlayerItemConsumeEvent event){
		if(event.getItem().getType().equals(Material.POTION)){
			Player player = event.getPlayer();
			ItemStack potion = event.getItem();
			if (potion.getItemMeta()!=null
					&&potion.getItemMeta().getLore()!=null
					&&potion.getItemMeta().getLore().contains(Lang.ITEMS_DEUS_EX_MACHINA)){
				deusExMachinaDrunk.put(player,System.currentTimeMillis());
			}
		}
	}

	@EventHandler
	private void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();

		if(projectile instanceof ThrownPotion){
			ThrownPotion potion = (ThrownPotion) projectile;
			if(potion.getScoreboardTags().contains("Flask of Cleansing")){
				hitByFlaskOfCleansing((Player)event.getEntity().getShooter(),potion);
			}
		}
	}

	@EventHandler
	public void onRodLand(ProjectileHitEvent e) {
		if (e.getHitEntity() != null) {
			if (e.getEntityType() != EntityType.FISHING_HOOK) {
				return;
			}

			if (e.getHitEntity() != null) {

				FishHook hook = (FishHook) e.getEntity();
				Player hookShooter = (Player) hook.getShooter();
				LivingEntity hitEntity = (LivingEntity) e.getHitEntity();

				double kx = hook.getLocation().getDirection().getX() / 2;
				double kz = hook.getLocation().getDirection().getZ() / 2;
				kx = -kx;

				if (hitEntity.getNoDamageTicks() >= 6.5) {
					return;
				}

				//hitEntity.damage(-0.001,hook);
				hitEntity.damage(0.001,hook);
				double upVel = 0.372;
				if (!hitEntity.isOnGround()) {
					upVel = 0;
				}

				hitEntity.setVelocity(new Vector(kx, upVel, kz));

				hitEntity.setNoDamageTicks(10);
			}
		}
	}

	public void damageByExcalibur(int cooldown, Player damaged, UhcPlayer uhcDamager){
		// Check cooldown
		if (cooldown != -1 && (cooldown* TimeUtils.SECOND) + excaliburUsingLastUpdate.getOrDefault(uhcDamager,-1L) > System.currentTimeMillis()){
			return;
		}

		excaliburUsingLastUpdate.put(uhcDamager,System.currentTimeMillis());
		damaged.setHealth(damaged.getHealth()-3);
		if (damaged.getHealth()<0){
			damaged.setHealth(0);
		}
		Objects.requireNonNull(damaged.getLocation().getWorld()).playSound(damaged.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1,1);
		Objects.requireNonNull(damaged.getLocation().add(0, 1, 0).getWorld()).spawnParticle(Particle.EXPLOSION_LARGE, damaged.getLocation(),1);
	}

	public void damageByPerun(int cooldown, Player damaged, UhcPlayer uhcDamager){
		// Check cooldown
		if (cooldown != -1 && (cooldown*TimeUtils.SECOND) + perunUsingLastUpdate.getOrDefault(uhcDamager,-1L) > System.currentTimeMillis()){
			return;
		}

		perunUsingLastUpdate.put(uhcDamager,System.currentTimeMillis());
		damaged.setHealth(damaged.getHealth()-2);
		if (damaged.getHealth()<0){
			damaged.setHealth(0);
		}
		Objects.requireNonNull(damaged.getLocation().getWorld()).strikeLightningEffect(damaged.getLocation());
	}

	public void damageByDeath(ItemStack hand,Player damager, Player damaged){

		Damageable damageable = (Damageable) hand.getItemMeta();
		Objects.requireNonNull(damageable).setDamage(damageable.getDamage()+19);
		hand.setItemMeta(damageable);

		double damage = damaged.getHealth()/5;
		damaged.setHealth(damaged.getHealth()-damaged.getHealth()/5);

		if (damaged.getHealth()<0){
			damaged.setHealth(0);
		}

		String message = Lang.ITEMS_DEATH_MESSAGE.replace("&","\u00A7")
				.replace("{damage}", String.format("%.2f",damage))
				.replace("{heal}", String.format("%.2f",damage/4));
		damager.sendMessage(message);
		damager.setHealth(damager.getHealth()+damage/4);
	}

	public void damageByModularLightning(Player shooter, Player damaged, Arrow arrow){
		if(arrow.isCritical()) {
			damaged.setHealth(damaged.getHealth() - 2);
			if (damaged.getHealth() < 0) {
				damaged.setHealth(0);
			}
			Objects.requireNonNull(damaged.getLocation().getWorld()).strikeLightningEffect(damaged.getLocation());
		}else{
			shooter.sendMessage(Lang.ITEMS_BOW_NOT_CRITICAL);
		}
	}

	public void damageByModularPoison(Player shooter, Player damaged, Arrow arrow){
		if(arrow.isCritical()) {
			damaged.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 1));
			damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
		}else{
			shooter.sendMessage(Lang.ITEMS_BOW_NOT_CRITICAL);
		}
	}

	public void hitByFlaskOfCleansing(Player shooter, ThrownPotion potion){
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(shooter);
		PlayerManager pm = GameManager.getGameManager().getPlayerManager();
		List<UhcPlayer> pointPlayers = new ArrayList<>(pm.getOnlinePlayingPlayers());

		// Get enemies in radius
		for (UhcPlayer pointPlayer : pointPlayers) {
			double distance;
			try {
				distance = potion.getLocation().distance(pointPlayer.getPlayer().getLocation());
				if (potion.getWorld().getEnvironment() == pointPlayer.getPlayer().getWorld().getEnvironment()
						&& distance <= 4) {
					if (!uhcPlayer.getTeam().getOnlinePlayingMembers().contains(pointPlayer)){
						Player player = null;
						try {
							player = pointPlayer.getPlayer();
						} catch (UhcPlayerNotOnlineException e) {
							e.printStackTrace();
						}
						for (PotionEffect effect : player.getActivePotionEffects()) {
							player.removePotionEffect(effect.getType());
						}
						player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_HURT,1,1);
						handleCleansing(player);
						player.sendMessage(Lang.ITEMS_FLASK_HIT);
						pointPlayers.remove(pointPlayer);
					}
					//broadcastMessage(String.valueOf(distance));
					//pointPlayers.remove(pointPlayer);
					//broadcastMessage("not in radius"+pointPlayer.getRealName());

				}
			} catch (UhcPlayerNotOnlineException ignored) {

			}
		}

		/*for (UhcPlayer a : pointPlayers){
			broadcastMessage("1-> "+a.getRealName());
		}

		// Get enemy
		pointPlayers.addAll(pm.getOnlinePlayingPlayers());
		for (UhcPlayer teamMember : uhcPlayer.getTeam().getOnlinePlayingMembers()){
			pointPlayers.remove(teamMember);
			//broadcastMessage("team member"+teamMember.getRealName());
		}

		for (UhcPlayer a : pointPlayers){
			broadcastMessage("2-> "+a.getRealName());
		}

		broadcastMessage("============");

		for (UhcPlayer pointPlayer : pointPlayers) {
			Player player = null;
			try {
				player = pointPlayer.getPlayer();
			} catch (UhcPlayerNotOnlineException e) {
				e.printStackTrace();
			}
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_HURT,1,1);
			handleCleansing(player);
			player.sendMessage(Lang.ITEMS_FLASK_HIT);
			pointPlayers.remove(pointPlayer);
		}*/
	}

	public static void handleCleansing(Player player) {
		new BukkitRunnable(){
			int i = 400;
			public void run(){
				if (i <= 0){
					this.cancel();
				}
				else
				{
					player.removePotionEffect(PotionEffectType.REGENERATION);
					i--;
				}
			}
		}.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,1);
	}

}