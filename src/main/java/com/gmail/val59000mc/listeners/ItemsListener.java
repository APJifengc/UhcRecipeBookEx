package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.*;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.ScoreboardHandler;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.*;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.threads.CheckMainhandThread;
import com.gmail.val59000mc.threads.StartDeathmatchThread;
import com.gmail.val59000mc.utils.TimeUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import net.Indyuce.mmoitems.stat.ItemDamage;
import net.Indyuce.mmoitems.stat.Lore;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.block.BrewingStand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static com.gmail.val59000mc.utils.SpigotUtils.sendMessage;
import static org.bukkit.Bukkit.broadcastMessage;

public class ItemsListener implements Listener {

	private final GameManager gameManager;
	private final MainConfig config;
	private final PlayerManager playerManager;
	private final TeamManager teamManager;
	private final ScenarioManager scenarioManager;
	private final ScoreboardHandler scoreboardHandler;

	private final Map<Player,Long> modularUsingLastUpdate = new HashMap<>();

	public ItemsListener(
			GameManager gameManager,
			MainConfig config,
			PlayerManager playerManager,
			TeamManager teamManager,
			ScenarioManager scenarioManager,
			ScoreboardHandler scoreboardHandler) {
		this.gameManager = gameManager;
		this.config = config;
		this.playerManager = playerManager;
		this.teamManager = teamManager;
		this.scenarioManager = scenarioManager;
		this.scoreboardHandler = scoreboardHandler;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeftClickItem(PlayerInteractEvent event) {
		if (
				event.getAction() != Action.LEFT_CLICK_AIR &&
						event.getAction() != Action.LEFT_CLICK_BLOCK
		) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (UhcItems.isModularBowPunchItem(hand)
				||UhcItems.isModularBowLightningItem(hand)
				||UhcItems.isModularBowPoisonItem(hand)){
			handleModularSwitch(10, hand, player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClickItem(PlayerInteractEvent event) {
		if (
				event.getAction() != Action.RIGHT_CLICK_AIR &&
						event.getAction() != Action.RIGHT_CLICK_BLOCK
		) {
			return;
		}

		Player player = event.getPlayer();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (GameItem.isGameItem(hand)) {
			event.setCancelled(true);
			GameItem gameItem = GameItem.getGameItem(hand);
			handleGameItemInteract(gameItem, player, uhcPlayer, hand);
			return;
		}

		GameState state = gameManager.getGameState();
		if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
				&& UhcItems.isRegenHeadItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
		) {
			event.setCancelled(true);
			uhcPlayer.getTeam().regenTeam(config.get(MainConfig.DOUBLE_REGEN_HEAD), 1, uhcPlayer);
		}

		if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
				&& UhcItems.isGoldenHeadItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
		) {
			event.setCancelled(true);
			uhcPlayer.getTeam().regenTeamGold(config.get(MainConfig.DOUBLE_REGEN_HEAD), 1, uhcPlayer);
		}

		if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
				&& UhcItems.isCornItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
		) {
			event.setCancelled(true);
			uhcPlayer.regenPlayerCorn(1,uhcPlayer);
		}

		if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
				&& UhcItems.isMasterCompassItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.PLAYING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
		) {
			event.setCancelled(true);
			uhcPlayer.pointMasterCompassToPlayer(1,uhcPlayer);
		}
	}


	public void handleModularSwitch(int cooldown, ItemStack hand, Player player){
		// Check cooldown
		if (cooldown != -1 && (cooldown*TimeUtils.SECOND_TICKS) + modularUsingLastUpdate.getOrDefault(player,-1L) > System.currentTimeMillis()){
			return;
		}

		modularUsingLastUpdate.put(player,System.currentTimeMillis());

		player.playSound(player.getLocation(),Sound.ENTITY_ARROW_HIT_PLAYER,1,1);
		ItemMeta itemMeta = hand.getItemMeta();

		if(UhcItems.isModularBowPunchItem(hand)){
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_POISON_1);
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_POISON_2);
			itemMeta.removeEnchant(Enchantment.ARROW_KNOCKBACK);
			itemMeta.setLore(List.of(Lang.ITEMS_MODULAR_BOW_POISON
					,Lang.ITEMS_MODULAR_BOW_LORE_1
					,Lang.ITEMS_MODULAR_BOW_LORE_2
					,Lang.ITEMS_MODULAR_BOW_LORE_3
					,Lang.ITEMS_MODULAR_BOW_LORE_4));
			hand.setItemMeta(itemMeta);
			return;
		}

		if(UhcItems.isModularBowLightningItem(hand)){
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_PUNCH_1);
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_PUNCH_2);
			itemMeta.addEnchant(Enchantment.ARROW_KNOCKBACK,1,true);
			itemMeta.setLore(List.of(Lang.ITEMS_MODULAR_BOW_PUNCH
					,Lang.ITEMS_MODULAR_BOW_LORE_1
					,Lang.ITEMS_MODULAR_BOW_LORE_2
					,Lang.ITEMS_MODULAR_BOW_LORE_3
					,Lang.ITEMS_MODULAR_BOW_LORE_4));
			hand.setItemMeta(itemMeta);
			return;
		}

		if(UhcItems.isModularBowPoisonItem(hand)){
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_LIGHTNING_1);
			player.sendMessage(Lang.ITEMS_MODULAR_BOW_TO_LIGHTNING_2);
			itemMeta.removeEnchant(Enchantment.ARROW_KNOCKBACK);
			itemMeta.setLore(List.of(Lang.ITEMS_MODULAR_BOW_LIGHTNING
					,Lang.ITEMS_MODULAR_BOW_LORE_1
					,Lang.ITEMS_MODULAR_BOW_LORE_2
					,Lang.ITEMS_MODULAR_BOW_LORE_3
					,Lang.ITEMS_MODULAR_BOW_LORE_4));
			hand.setItemMeta(itemMeta);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickInInventory(InventoryClickEvent event) {
		handleScenarioInventory(event);

		ItemStack item = event.getCurrentItem();
		Player player = (Player) event.getWhoClicked();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);

		// Stop players from moving game items in their inventory.
		// Above item == null check as item is null on hotbar swap.
		if (gameManager.getGameState() == GameState.WAITING && event.getAction() == InventoryAction.HOTBAR_SWAP) {
			event.setCancelled(true);
		}

		// Only handle clicked items.
		if (item == null) {
			return;
		}

		// Listen for GameItems
		if (gameManager.getGameState() == GameState.WAITING) {
			if (GameItem.isGameItem(item)) {
				event.setCancelled(true);
				handleGameItemInteract(GameItem.getGameItem(item), player, uhcPlayer, item);
			}
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_INVITE_PLAYER)) {
			if (item.getType() != UniversalMaterial.PLAYER_HEAD.getType() || !item.hasItemMeta()) {
				return;
			}

			event.setCancelled(true);
			player.closeInventory();

			String playerName = item.getItemMeta().getDisplayName().replace(ChatColor.GREEN.toString(), "");
			player.performCommand("team invite " + playerName);
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_TEAM_VIEW)) {
			if (item.getType() == UniversalMaterial.PLAYER_HEAD.getType() && item.hasItemMeta()) {
				event.setCancelled(true);
			}
		}

		// Click on a player head to join a team
		if (event.getView().getTitle().equals(Lang.ITEMS_KIT_INVENTORY)) {
			if (KitsManager.isKitItem(item)) {
				event.setCancelled(true);
				Kit kit = KitsManager.getKitByName(item.getItemMeta().getDisplayName());
				if (kit.canBeUsedBy(player, config)) {
					uhcPlayer.setKit(kit);
					uhcPlayer.sendMessage(Lang.ITEMS_KIT_SELECTED.replace("%kit%", kit.getName()));
				} else {
					uhcPlayer.sendMessage(Lang.ITEMS_KIT_NO_PERMISSION);
				}
				player.closeInventory();
			}
		}

		if (UhcItems.isTeamSkullItem(item)) {
			event.setCancelled(true);

			UhcTeam team = teamManager.getTeamByName(item.getItemMeta().getDisplayName());

			// Click on a player head to reply to invite
			if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_INVITES)) {
				if (team == null) {
					player.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
				} else {
					UhcItems.openTeamReplyInviteInventory(player, team);
				}
			}
			// Open team view inventory
			else {
				if (team == null) {
					player.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
				} else {
					UhcItems.openTeamViewInventory(player, team);
				}
			}
		}

		if (event.getView().getTitle().equals(Lang.TEAM_INVENTORY_COLOR)) {
			event.setCancelled(true);

			if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
				String selectedColor = item.getItemMeta().getLore().get(0).replace(ChatColor.RESET.toString(), "");
				player.closeInventory();

				// check if already used by this team
				if (uhcPlayer.getTeam().getColor().contains(selectedColor)) {
					uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_ALREADY_SELECTED);
					return;
				}

				// check if still available
				String newPrefix = teamManager.getTeamPrefix(selectedColor);
				if (newPrefix == null) {
					uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_UNAVAILABLE);
					return;
				}

				// assign color and update color on tab
				uhcPlayer.getTeam().setPrefix(newPrefix);
				for (UhcPlayer teamMember : uhcPlayer.getTeam().getMembers()) {
					scoreboardHandler.updatePlayerOnTab(teamMember);
				}

				uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_COLOR_CHANGED);
				return;
			}
		}

		if (event.getView().getTitle().equals(Lang.ITEMS_CRAFT_BOOK_INVENTORY)) {
			event.setCancelled(true);

			if (CraftsManager.isCraftItem(item)) {
				player.closeInventory();
				Craft craft = CraftsManager.getCraftByDisplayName(item.getItemMeta().getDisplayName());
				if (!config.get(MainConfig.ENABLE_CRAFTS_PERMISSIONS) || (config.get(MainConfig.ENABLE_CRAFTS_PERMISSIONS) && player.hasPermission("uhc-core.craft." + craft.getName()))) {
					CraftsManager.openCraftInventory(player, craft);
				} else {
					player.sendMessage(Lang.ITEMS_CRAFT_NO_PERMISSION.replace("%craft%", craft.getName()));
				}
			}

			if (CraftsManager.isCraftBookBackItem(item)) {
				event.setCancelled(true);
				player.closeInventory();
				CraftsManager.openCraftBookInventory(player);
			}

		}

		// Ban level 2 potions
		if (event.getInventory().getType().equals(InventoryType.BREWING) /*&& config.get(MainConfig.BAN_LEVEL_TWO_POTIONS)*/) {
			final BrewerInventory inv = (BrewerInventory) event.getInventory();
			final HumanEntity human = event.getWhoClicked();
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckBrewingStandAfterClick(inv.getHolder(), human), 1);
		}

		if (event.getInventory().getType().equals(InventoryType.ANVIL) && config.get(MainConfig.BAN_SHARPNESS_AXE)) {
			final AnvilInventory inv = (AnvilInventory) event.getInventory();
			final HumanEntity human = event.getWhoClicked();
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckAnvilAfterClick(inv, human), 1);
		}

		//Check Main Hand Item
		ItemStack hand = player.getInventory().getItemInMainHand();
		List<String> oldLores = null;
		if(hand.getItemMeta()!=null){
			oldLores = hand.getItemMeta().getLore();
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new CheckMainhandThread(player,oldLores,uhcPlayer, gameManager), 1);
	}

	private void handleGameItemInteract(GameItem gameItem, Player player, UhcPlayer uhcPlayer, ItemStack item) {
		switch (gameItem) {
			case TEAM_SELECTION:
				UhcItems.openTeamMainInventory(player, uhcPlayer);
				break;
			case TEAM_SETTINGS:
				UhcItems.openTeamSettingsInventory(player);
				break;
			case KIT_SELECTION:
				KitsManager.openKitSelectionInventory(player);
				break;
			case CUSTOM_CRAFT_BOOK:
				CraftsManager.openCraftBookInventory(player);
				break;
			case TEAM_COLOR_SELECTION:
				UhcItems.openTeamColorInventory(player);
				break;
			case TEAM_RENAME:
				openTeamRenameGUI(player, uhcPlayer.getTeam());
				break;
			case SCENARIO_VIEWER:
				Inventory inv;
				if (config.get(MainConfig.ENABLE_SCENARIO_VOTING)) {
					inv = scenarioManager.getScenarioVoteInventory(uhcPlayer);
				} else {
					inv = scenarioManager.getScenarioMainInventory(player.hasPermission("uhc-core.scenarios.edit"));
				}
				player.openInventory(inv);
				break;
			case BUNGEE_ITEM:
				playerManager.sendPlayerToBungeeServer(player);
				break;
			case COMPASS_ITEM:
				uhcPlayer.pointCompassToNextPlayer(config.get(MainConfig.PLAYING_COMPASS_MODE), config.get(MainConfig.PLAYING_COMPASS_COOLDOWN),uhcPlayer);
				break;
			case TEAM_READY:
			case TEAM_NOT_READY:
				uhcPlayer.getTeam().changeReadyState();
				UhcItems.openTeamSettingsInventory(player);
				break;
			case TEAM_INVITE_PLAYER:
				UhcItems.openTeamInviteInventory(player);
				break;
			case TEAM_INVITE_PLAYER_SEARCH:
				openTeamInviteGUI(player);
				break;
			case TEAM_VIEW_INVITES:
				UhcItems.openTeamInvitesInventory(player, uhcPlayer);
				break;
			case TEAM_INVITE_ACCEPT:
				handleTeamInviteReply(uhcPlayer, item, true);
				player.closeInventory();
				break;
			case TEAM_INVITE_DENY:
				handleTeamInviteReply(uhcPlayer, item, false);
				player.closeInventory();
				break;
			case TEAM_LEAVE:
				try {
					uhcPlayer.getTeam().leave(uhcPlayer);

					// Update player tab
					scoreboardHandler.updatePlayerOnTab(uhcPlayer);
				} catch (UhcTeamException ex) {
					uhcPlayer.sendMessage(ex.getMessage());
				}
				break;
			case TEAM_LIST:
				UhcItems.openTeamsListInventory(player);
				break;
		}
	}

	private void handleTeamInviteReply(UhcPlayer uhcPlayer, ItemStack item, boolean accepted) {
		if (!item.hasItemMeta()) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		ItemMeta meta = item.getItemMeta();

		if (!meta.hasLore()) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		if (meta.getLore().size() != 2) {
			uhcPlayer.sendMessage("Something went wrong!");
			return;
		}

		String line = meta.getLore().get(1).replace(ChatColor.DARK_GRAY.toString(), "");
		UhcTeam team = teamManager.getTeamByName(line);

		if (team == null) {
			uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
			return;
		}

		teamManager.replyToTeamInvite(uhcPlayer, team, accepted);
	}

	private void openTeamRenameGUI(Player player, UhcTeam team) {
		new AnvilGUI.Builder()
				.plugin(UhcCore.getPlugin())
				.title(Lang.TEAM_INVENTORY_RENAME)
				.text(team.getTeamName())
				.item(new ItemStack(Material.NAME_TAG))
				.onComplete(((p, s) -> {
					if (teamManager.isValidTeamName(s)) {
						team.setTeamName(s);
						p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED);
					} else {
						p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED_ERROR);
					}
					return AnvilGUI.Response.close();
				}))
				.open(player);
	}

	private void openTeamInviteGUI(Player player) {
		new AnvilGUI.Builder()
				.plugin(UhcCore.getPlugin())
				.title(Lang.TEAM_INVENTORY_INVITE_PLAYER)
				.text("Enter name ...")
				.item(new ItemStack(Material.NAME_TAG))
				.onComplete(((p, s) -> {
					p.performCommand("team invite " + s);
					return AnvilGUI.Response.close();
				}))
				.open(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperEvent(InventoryMoveItemEvent event) {
		Inventory inv = event.getDestination();
		if (inv.getType().equals(InventoryType.BREWING)/* && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS)*/ && inv.getHolder() instanceof BrewingStand) {
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new ItemsListener.CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(), null), 1);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnvilPrepareEvent(PrepareAnvilEvent event) {
		ItemStack result = event.getResult();
		ItemStack slot0 = event.getInventory().getItem(0);
		ItemStack slot1 = event.getInventory().getItem(1);
		if (config.get(MainConfig.BAN_SHARPNESS_AXE) && result.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL) &&
				UniversalMaterial.isAxe(result.getType())) {
			result.setType(Material.AIR);
		}
		if ((slot0.getItemMeta()!=null&&slot0.getItemMeta().getLore()!=null && slot0.getItemMeta().getLore().contains(Lang.ITEMS_CANT_ENCHANT))
				|| (slot1.getItemMeta()!=null&&slot1.getItemMeta().getLore()!=null && slot1.getItemMeta().getLore().contains(Lang.ITEMS_CANT_ENCHANT))){
			result.setType(Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnchantEvent(EnchantItemEvent event) {
		Map<Enchantment,Integer> map = event.getEnchantsToAdd();
		map.remove(Enchantment.LOOT_BONUS_BLOCKS);
		map.remove(Enchantment.RIPTIDE);
		/*for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
			if(entry.getKey().equals(Enchantment.LOOT_BONUS_BLOCKS)){
				map.remove(Enchantment.LOOT_BONUS_BLOCKS);
			}
		}*/
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnchantPrepareEvent(PrepareItemEnchantEvent event) {
		ItemStack slot0 = event.getInventory().getItem(0);
		EnchantmentOffer offer0 = event.getOffers()[0];
		EnchantmentOffer offer1 = event.getOffers()[1];
		EnchantmentOffer offer2 = event.getOffers()[2];
		if (offer0.getEnchantment().equals(Enchantment.LOOT_BONUS_BLOCKS)){
			offer0.setEnchantment(Enchantment.DIG_SPEED);
		}
		if (offer1.getEnchantment().equals(Enchantment.LOOT_BONUS_BLOCKS)){
			offer1.setEnchantment(Enchantment.DIG_SPEED);
		}
		if (offer2.getEnchantment().equals(Enchantment.LOOT_BONUS_BLOCKS)){
			offer2.setEnchantment(Enchantment.DIG_SPEED);
		}
		if (offer0.getEnchantment().equals(Enchantment.RIPTIDE)){
			offer0.setEnchantment(Enchantment.LOYALTY);
		}
		if (offer1.getEnchantment().equals(Enchantment.RIPTIDE)){
			offer1.setEnchantment(Enchantment.LOYALTY);
		}
		if (offer2.getEnchantment().equals(Enchantment.RIPTIDE)){
			offer2.setEnchantment(Enchantment.LOYALTY);
		}
		if (slot0.getItemMeta()!=null&&slot0.getItemMeta().getLore()!=null && slot0.getItemMeta().getLore().contains(Lang.ITEMS_CANT_ENCHANT)){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSmithPrepareEvent(PrepareSmithingEvent event) {
		ItemStack result = event.getResult();
		if (result.getItemMeta()!=null&&result.getItemMeta().getLore()!=null && result.getItemMeta().getLore().contains(Lang.ITEMS_CANT_ENCHANT)){
			result.setType(Material.AIR);
		}
	}

	private static class CheckBrewingStandAfterClick implements Runnable {
		private final BrewingStand stand;
		private final HumanEntity human;

		private CheckBrewingStandAfterClick(BrewingStand stand, HumanEntity human) {
			this.stand = stand;
			this.human = human;
		}

		@Override
		public void run() {
			ItemStack ingredient = stand.getInventory().getIngredient();
			PotionType type0 = null;
			PotionType type1 = null;
			PotionType type2 = null;
			if (stand.getInventory().getItem(0) != null) {
				PotionMeta potion0 = (PotionMeta) stand.getInventory().getItem(0).getItemMeta();
				type0 = potion0.getBasePotionData().getType();
			}
			if (stand.getInventory().getItem(1) != null) {
				PotionMeta potion1 = (PotionMeta) stand.getInventory().getItem(1).getItemMeta();
				type1 = potion1.getBasePotionData().getType();
			}
			if (stand.getInventory().getItem(2) != null) {
				PotionMeta potion2 = (PotionMeta) stand.getInventory().getItem(2).getItemMeta();
				type2 = potion2.getBasePotionData().getType();
			}

			if (ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)
					&& (type0 == PotionType.STRENGTH || type1 == PotionType.STRENGTH || type2 == PotionType.STRENGTH)) {
				if (human != null) {
					human.sendMessage(Lang.ITEMS_STRENGTH_2_POTION_BANNED);
				}

				stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
				stand.getInventory().setIngredient(new ItemStack(Material.AIR));
			}
		}
	}

	private static class CheckAnvilAfterClick implements Runnable {
		private final AnvilInventory anvil;
		private final HumanEntity human;

		private CheckAnvilAfterClick(AnvilInventory anvil, HumanEntity human) {
			this.anvil = anvil;
			this.human = human;
		}

		@Override
		public void run() {
			ItemStack slot0 = anvil.getItem(0);
			ItemStack slot1 = anvil.getItem(1);
			if (slot1 == null || !(slot1.getItemMeta() instanceof EnchantmentStorageMeta)) {
				return;
			}
			EnchantmentStorageMeta enchmeta = (EnchantmentStorageMeta) slot1.getItemMeta();

			if (slot0 != null && (UniversalMaterial.isAxe(slot0.getType()))
					&& enchmeta.hasStoredEnchant(Enchantment.DAMAGE_ALL)) {
				if (human != null) {
					human.sendMessage(Lang.ITEMS_SHARPNESS_AXE_BANNED);
				}

				human.getLocation().getWorld().dropItemNaturally(human.getLocation().add(-0.5, 0, -0.5), slot1.clone());
				anvil.setItem(1, new ItemStack(Material.AIR));
			}
		}
	}
/*	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperEvent(InventoryMoveItemEvent event) {
		Inventory inv = event.getDestination();
		if(inv.getType().equals(InventoryType.BREWING) && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS) && inv.getHolder() instanceof BrewingStand){
			Bukkit.getScheduler().runTaskLater(UhcCore.getPlugin(), new CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(), null),1);
		}

	}

	private static class CheckBrewingStandAfterClick implements Runnable{
        private final BrewingStand stand;
        private final HumanEntity human;

        private CheckBrewingStandAfterClick(BrewingStand stand, HumanEntity human) {
        	this.stand = stand;
        	this.human = human;
        }

        @Override
        public void run(){
        	ItemStack ingredient = stand.getInventory().getIngredient();
			PotionMeta potion0 = (PotionMeta) stand.getInventory().getItem(0).getItemMeta();
			PotionMeta potion1 = (PotionMeta) stand.getInventory().getItem(1).getItemMeta();
			PotionMeta potion2 = (PotionMeta) stand.getInventory().getItem(2).getItemMeta();
			PotionType type0 = potion0.getBasePotionData().getType();
			PotionType type1 = potion1.getBasePotionData().getType();
			PotionType type2 = potion2.getBasePotionData().getType();

			if(ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)
					&& (type0.equals(PotionType.STRENGTH)||type1.equals(PotionType.STRENGTH)||type2.equals(PotionType.STRENGTH))){
				if(human != null){
                    human.sendMessage(Lang.ITEMS_POTION_BANNED);
                }

				stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
				stand.getInventory().setIngredient(new ItemStack(Material.AIR));
			}
        }
	}*/

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();

		if (gameManager.getGameState() == GameState.WAITING && GameItem.isGameItem(item)) {
			event.setCancelled(true);
		}
	}

/*	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e){
		if (e.getItem() == null) return;

		Craft craft = CraftsManager.getCraft(e.getItem());
		if (craft != null){
			for (Craft.OnConsumeListener listener : craft.getOnConsumeListeners()) {
				if (listener.onConsume(playerManager.getUhcPlayer(e.getPlayer()))) {
					e.setCancelled(true);
					return;
				}
			}
		}

		if (e.getItem().isSimilar(UhcItems.createGoldenHead())){
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 4));
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1));
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
		}
	}*/

	private void handleScenarioInventory(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		InventoryView clickedInv = e.getView();

		if (clickedInv == null || e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
			return;
		}

		Player player = (Player) e.getWhoClicked();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
		ItemStack item = e.getCurrentItem();
		ItemMeta meta = item.getItemMeta();

		boolean mainInventory = clickedInv.getTitle().equals(Lang.SCENARIO_GLOBAL_INVENTORY);
		boolean editInventory = clickedInv.getTitle().equals(Lang.SCENARIO_GLOBAL_INVENTORY_EDIT);
		boolean voteInventory = clickedInv.getTitle().equals(Lang.SCENARIO_GLOBAL_INVENTORY_VOTE);

		// No scenario inventory!
		if (!mainInventory && !editInventory && !voteInventory) {
			return;
		}

		e.setCancelled(true);
		player.closeInventory();

		// Get scenario info when right click or when on the global inventory menu.
		if (e.getClick() == ClickType.RIGHT || mainInventory) {
			// Handle edit item
			if (meta.getDisplayName().equals(Lang.SCENARIO_GLOBAL_ITEM_EDIT)) {
				uhcPlayer.setBrowsingPage(0);
				Inventory inv = scenarioManager.getScenarioEditInventory(0);
				player.openInventory(inv);
				return;
			}

			// Send scenario info
			scenarioManager.getScenarioByName(meta.getDisplayName()).ifPresent(sce -> {
				player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_HEADER.replace("%scenario%", sce.getInfo().getName()));
				sce.getInfo().getDescription().forEach(s -> player.sendMessage(Lang.SCENARIO_GLOBAL_DESCRIPTION_PREFIX + s));
			});
		} else if (editInventory) {
			// Handle back item
			if (item.getItemMeta().getDisplayName().equals(Lang.SCENARIO_GLOBAL_ITEM_BACK)) {
				Inventory inv;

				int page = uhcPlayer.getBrowsingPage() - 1;
				if (page < 0) {
					inv = scenarioManager.getScenarioMainInventory(true);
				} else {
					uhcPlayer.setBrowsingPage(page);
					inv = scenarioManager.getScenarioEditInventory(page);
				}
				player.openInventory(inv);
				return;
			}
			// Handle next item
			if (item.getItemMeta().getDisplayName().equals(Lang.SCENARIO_GLOBAL_ITEM_NEXT)) {
				int page = uhcPlayer.getBrowsingPage() + 1;
				uhcPlayer.setBrowsingPage(page);
				Inventory inv = scenarioManager.getScenarioEditInventory(page);
				player.openInventory(inv);
				return;
			}

			// toggle scenario
			scenarioManager.getScenarioByName(meta.getDisplayName())
					.ifPresent(scenarioManager::toggleScenario);

			// Open edit inventory
			player.openInventory(scenarioManager.getScenarioEditInventory(uhcPlayer.getBrowsingPage()));
		} else if (voteInventory) {
			// Clicked scenario
			Scenario scenario = scenarioManager.getScenarioByName(meta.getDisplayName()).orElse(null);

			// toggle scenario
			if (uhcPlayer.getScenarioVotes().contains(scenario)) {
				uhcPlayer.getScenarioVotes().remove(scenario);
			} else {
				int maxVotes = config.get(MainConfig.MAX_SCENARIO_VOTES);
				if (uhcPlayer.getScenarioVotes().size() == maxVotes) {
					player.sendMessage(Lang.SCENARIO_GLOBAL_VOTE_MAX.replace("%max%", String.valueOf(maxVotes)));
					return;
				}
				uhcPlayer.getScenarioVotes().add(scenario);
			}
			player.openInventory(scenarioManager.getScenarioVoteInventory(uhcPlayer));
		}
	}

	@EventHandler
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		Entity entity = event.getEntity();
		Random random = new Random();
		int number = random.nextInt(4);
		if(entity.getType()==EntityType.SHEEP && entity.getLocation().getWorld()!=null && number == 0){
			entity.getLocation().getWorld().dropItemNaturally(entity.getLocation().add(-0.5, 0, -0.5), new ItemStack(Material.STRING));
		}
	}

	@EventHandler
	public void onPlayerShoot(EntityShootBowEvent event) {
		Player shooter = (Player) event.getEntity();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(shooter);

		Arrow arrow = (Arrow) event.getProjectile();

		ItemStack bow = event.getBow();

		if (UhcItems.isArtemisBowItem(bow)) {
			Random random = new Random();
			int number = random.nextInt(3);
			if (number == 0) {
				arrow.setDamage(2.0d);
				ArtemisArrowAimBot(uhcPlayer, arrow);
			}
		}
		if (UhcItems.isModularBowLightningItem(bow)) {
			arrow.addScoreboardTag("Modular Arrow Lightning");
		}
		if (UhcItems.isModularBowPoisonItem(bow)) {
			arrow.addScoreboardTag("Modular Arrow Poison");
		}
		if (UhcItems.isModularBowPunchItem(bow)) {
			arrow.addScoreboardTag("Modular Arrow Punch");
		}
	}

	@EventHandler
	public void onEntityLaunchProjectile(ProjectileLaunchEvent event) {
		if(event.getEntity() instanceof ThrownPotion){
			ThrownPotion potion = (ThrownPotion) event.getEntity();
			if(potion.getItem().getItemMeta()!=null
					&&potion.getItem().getItemMeta().getLore()!=null
					&&potion.getItem().getItemMeta().getLore().contains(Lang.ITEMS_FLASK)){
				potion.addScoreboardTag("Flask of Cleansing");
			}
		}
	}

	public static void ArtemisArrowAimBot(UhcPlayer uhcPlayer,Arrow arrow){
		PlayerManager pm = GameManager.getGameManager().getPlayerManager();
		List<UhcPlayer> pointPlayers = new ArrayList<>();

		// Get enemy
		pointPlayers.addAll(pm.getOnlinePlayingPlayers());
		for (UhcPlayer teamMember : uhcPlayer.getTeam().getOnlinePlayingMembers()){
			pointPlayers.remove(teamMember);
		}
		new BukkitRunnable(){
			int i = 200;
			public void run(){
				if (i <= 0){
					this.cancel();
				}
				else if (arrow.isOnGround()||arrow.getScoreboardTags().contains("Artemis Arrow Locked")){
					this.cancel();
				}
				else {
					uhcPlayer.ArtemisArrowLock(arrow,pointPlayers);
					i--;
				}
			}
		}.runTaskTimerAsynchronously(UhcCore.getPlugin(UhcCore.class),0,2);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDamage(PlayerItemDamageEvent event) {
		handleDigBlock(event);
	}

	private void handleDigBlock(PlayerItemDamageEvent e){
		Player player = e.getPlayer();
		ItemStack hand = player.getInventory().getItemInMainHand();
		ItemMeta itemMeta = hand.getItemMeta();
		if (itemMeta!=null && itemMeta.getLore()!=null && itemMeta.getLore().contains(Lang.ITEMS_MINER)) {
			org.bukkit.inventory.meta.Damageable damageable = (Damageable) hand.getItemMeta();
			int itemDamage = Objects.requireNonNull(damageable).getDamage();
			if (itemDamage % 250 == 0 && itemDamage != 0) {
				itemMeta.addEnchant(Enchantment.DIG_SPEED, itemDamage / 250, true);
				itemMeta.addEnchant(Enchantment.DAMAGE_ALL, itemDamage / 125, true);
				hand.setItemMeta(itemMeta);
			}
			if (itemDamage % 100 == 0 && itemDamage != 0) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangeItem(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
		ItemStack hand = player.getInventory().getItemInMainHand();
		List<String> oldLores = null;
		if(hand.getItemMeta()!=null){
			oldLores = hand.getItemMeta().getLore();
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new CheckMainhandThread(player, oldLores, uhcPlayer, gameManager), 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		Entity entity = event.getEntity();
		if(entity.getType().equals(EntityType.PLAYER)) {
			Player player = (Player) entity;
			UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
			ItemStack hand = player.getInventory().getItemInMainHand();
			List<String> oldLores = null;
			if (hand.getItemMeta() != null) {
				oldLores = hand.getItemMeta().getLore();
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new CheckMainhandThread(player, oldLores, uhcPlayer, gameManager), 1);
		}
	}


}