package io.github.apjifengc.uhcrecipebookex.listener;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.Craft;
import com.gmail.val59000mc.customitems.GameItem;
import com.gmail.val59000mc.customitems.KitsManager;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import io.github.apjifengc.uhcrecipebookex.UhcRecipeBookEx;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventory;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeInventoryHolder;
import io.github.apjifengc.uhcrecipebookex.inventory.CraftRecipeViewerInventoryHolder;
import io.github.apjifengc.uhcrecipebookex.inventory.item.NextPageItem;
import io.github.apjifengc.uhcrecipebookex.inventory.item.PreviousPageItem;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    private final UhcRecipeBookEx plugin = UhcRecipeBookEx.getInstance();

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClickItem(PlayerInteractEvent event){
        if (
                event.getAction() != Action.RIGHT_CLICK_AIR &&
                        event.getAction() != Action.RIGHT_CLICK_BLOCK
        ){
            return;
        }

        Player player = event.getPlayer();
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
        ItemStack hand = player.getItemInHand();

        if (GameItem.isGameItem(hand)){
            event.setCancelled(true);
            GameItem gameItem = GameItem.getGameItem(hand);
            handleGameItemInteract(gameItem, player, uhcPlayer, hand);
            return;
        }

        GameState state = GameManager.getGameManager().getGameState();
        if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
                && UhcItems.isRegenHeadItem(hand)
                && uhcPlayer.getState().equals(PlayerState.PLAYING)
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        ) {
            event.setCancelled(true);
            uhcPlayer.getTeam().regenTeam(GameManager.getGameManager().getConfig().get(MainConfig.DOUBLE_REGEN_HEAD));
            player.getInventory().remove(hand);
        }
    }

    private void handleGameItemInteract(GameItem gameItem, Player player, UhcPlayer uhcPlayer, ItemStack item){
        switch (gameItem){
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
                // Custom craft book
                player.openInventory(UhcRecipeBookEx.getRecipeInventory().createMainInventory(0));
                break;
            case TEAM_COLOR_SELECTION:
                UhcItems.openTeamColorInventory(player);
                break;
            case TEAM_RENAME:
                openTeamRenameGUI(player, uhcPlayer.getTeam());
                break;
            case SCENARIO_VIEWER:
                Inventory inv;
                if (GameManager.getGameManager().getConfig().get(MainConfig.ENABLE_SCENARIO_VOTING)){
                    inv = GameManager.getGameManager().getScenarioManager().getScenarioVoteInventory(uhcPlayer);
                }else {
                    inv = GameManager.getGameManager().getScenarioManager().getScenarioMainInventory(player.hasPermission("uhc-core.scenarios.edit"));
                }
                player.openInventory(inv);
                break;
            case BUNGEE_ITEM:
                GameManager.getGameManager().getPlayerManager().sendPlayerToBungeeServer(player);
                break;
            case COMPASS_ITEM:
                uhcPlayer.pointCompassToNextPlayer(GameManager.getGameManager().getConfig().get(MainConfig.PLAYING_COMPASS_MODE), GameManager.getGameManager().getConfig().get(MainConfig.PLAYING_COMPASS_COOLDOWN));
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
                    GameManager.getGameManager().getScoreboardManager().updatePlayerOnTab(uhcPlayer);
                }catch (UhcTeamException ex){
                    uhcPlayer.sendMessage(ex.getMessage());
                }
                break;
            case TEAM_LIST:
                UhcItems.openTeamsListInventory(player);
                break;
        }
    }

    private void handleTeamInviteReply(UhcPlayer uhcPlayer, ItemStack item, boolean accepted){
        if (!item.hasItemMeta()){
            uhcPlayer.sendMessage("Something went wrong!");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasLore()){
            uhcPlayer.sendMessage("Something went wrong!");
            return;
        }

        if (meta.getLore().size() != 2){
            uhcPlayer.sendMessage("Something went wrong!");
            return;
        }

        String line = meta.getLore().get(1).replace(ChatColor.DARK_GRAY.toString(), "");
        UhcTeam team = GameManager.getGameManager().getTeamManager().getTeamByName(line);

        if (team == null){
            uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
            return;
        }

        GameManager.getGameManager().getTeamManager().replyToTeamInvite(uhcPlayer, team, accepted);
    }

    private void openTeamRenameGUI(Player player, UhcTeam team){
        new AnvilGUI.Builder()
                .plugin(UhcCore.getPlugin())
                .title(Lang.TEAM_INVENTORY_RENAME)
                .text(team.getTeamName())
                .item(new ItemStack(Material.NAME_TAG))
                .onComplete(((p, s) -> {
                    if (GameManager.getGameManager().getTeamManager().isValidTeamName(s)){
                        team.setTeamName(s);
                        p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED);
                        return AnvilGUI.Response.close();
                    }else{
                        p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED_ERROR);
                        return AnvilGUI.Response.close();
                    }
                }))
                .open(player);
    }

    private void openTeamInviteGUI(Player player){
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
    void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder ||
                event.getInventory().getHolder() instanceof CraftRecipeInventoryHolder) {
            event.setCancelled(true);
        }
    }
}