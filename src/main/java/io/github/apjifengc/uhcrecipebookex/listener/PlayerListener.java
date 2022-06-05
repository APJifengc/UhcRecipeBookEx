package io.github.apjifengc.uhcrecipebookex.listener;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.*;
import com.gmail.val59000mc.exceptions.UhcTeamException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import io.github.apjifengc.uhcrecipebookex.Config;
import io.github.apjifengc.uhcrecipebookex.UhcRecipeBookEx;
import io.github.apjifengc.uhcrecipebookex.inventory.*;
import io.github.apjifengc.uhcrecipebookex.inventory.item.*;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerListener implements Listener {
    private final UhcRecipeBookEx plugin = UhcRecipeBookEx.getInstance();
    private final CraftRecipeInventory recipe = UhcRecipeBookEx.getRecipeInventory();
    private final Map<UhcPlayer, Map<ItemStack, Integer>> craftedItems = new HashMap<>();
    private final PlayerManager playerManager;
    GameManager gm = GameManager.getGameManager();
    public static final ItemStack BARRIER = new ItemStack(Material.BARRIER);

    static {
        ItemMeta meta = BARRIER.getItemMeta();
        meta.setDisplayName("\u00A7a");
        BARRIER.setItemMeta(meta);
    }

    public PlayerListener(PlayerManager playerManager) {
        assert playerManager != null;
        this.playerManager = playerManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        UhcPlayer uhcPlayer = GameManager.getGameManager().getPlayerManager().getUhcPlayer(player);
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (GameItem.isGameItem(hand)) {
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
            uhcPlayer.getTeam().regenTeam(false, 1, uhcPlayer);
        }

        if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
                && UhcItems.isGoldenHeadItem(hand)
                && uhcPlayer.getState().equals(PlayerState.PLAYING)
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        ) {
            event.setCancelled(true);
            uhcPlayer.getTeam().regenTeamGold(false, 1, uhcPlayer);
        }

        if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
                && UhcItems.isCornItem(hand)
                && uhcPlayer.getState().equals(PlayerState.PLAYING)
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        ) {
            event.setCancelled(true);
            uhcPlayer.regenPlayerCorn(1);
        }

        if ((state == GameState.PLAYING || state == GameState.DEATHMATCH)
                && UhcItems.isMasterCompassItem(hand)
                && uhcPlayer.getState().equals(PlayerState.PLAYING)
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        ) {
            event.setCancelled(true);
            //if (hand.getAmount() > 0) {
            //	hand.setAmount(hand.getAmount() - 1);
            //}
            uhcPlayer.pointMasterCompassToPlayer(1);
        }
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
                if (GameManager.getGameManager().getConfig().get(MainConfig.ENABLE_SCENARIO_VOTING)) {
                    inv = GameManager.getGameManager().getScenarioManager().getScenarioVoteInventory(uhcPlayer);
                } else {
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
        UhcTeam team = GameManager.getGameManager().getTeamManager().getTeamByName(line);

        if (team == null) {
            uhcPlayer.sendMessage(Lang.TEAM_MESSAGE_NO_LONGER_EXISTS);
            return;
        }

        GameManager.getGameManager().getTeamManager().replyToTeamInvite(uhcPlayer, team, accepted);
    }

    private void openTeamRenameGUI(Player player, UhcTeam team) {
        new AnvilGUI.Builder()
                .plugin(UhcCore.getPlugin())
                .title(Lang.TEAM_INVENTORY_RENAME)
                .text(team.getTeamName())
                .item(new ItemStack(Material.NAME_TAG))
                .onComplete(((p, s) -> {
                    if (GameManager.getGameManager().getTeamManager().isValidTeamName(s)) {
                        team.setTeamName(s);
                        p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED);
                        return AnvilGUI.Response.close();
                    } else {
                        p.sendMessage(Lang.TEAM_MESSAGE_NAME_CHANGED_ERROR);
                        return AnvilGUI.Response.close();
                    }
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
                } else if (recipe.getInventoryItem(Config.INVENTORY_PATTERN, event.getSlot()) instanceof PreviousPageItem) {
                    if (holder.getPage() != CraftRecipeInventory.getFirstPage()) {
                        event.getWhoClicked().openInventory(recipe.createMainInventory(holder.getPage() - 1));
                    }
                } else if (recipe.getInventoryItem(Config.INVENTORY_PATTERN, event.getSlot()) instanceof NextPageItem) {
                    if (holder.getPage() != CraftRecipeInventory.getLastPage()) {
                        event.getWhoClicked().openInventory(recipe.createMainInventory(holder.getPage() + 1));
                    }
                }
            }
        }
        if (event.getView().getTopInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                CraftRecipeInventory recipe = UhcRecipeBookEx.getRecipeInventory();
                CraftRecipeViewerInventoryHolder holder = (CraftRecipeViewerInventoryHolder) event.getView().getTopInventory().getHolder();
                if (recipe.getInventoryItem(Config.RECIPE_VIEWER_PATTERN, event.getSlot()) instanceof GoBackItem) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().openInventory(holder.getLastInventory());
                }
            }
        }
        if (event.getView().getTopInventory().getHolder() instanceof CraftingInventoryHolder) {
            Inventory inventory = event.getClickedInventory();
            Player player = (Player) event.getWhoClicked();
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, event.getSlot());
                if (item instanceof RecipeSlotItem) {
                    int slot = ((RecipeSlotItem) item).getSlot();
                    if (slot == 0) {
                        event.setCancelled(true);
                        Optional<CraftRecipe> craftOptional = getCurrentCraft(inventory, player);
                        if (craftOptional.isEmpty()) {
                            return;
                        }
                        CraftRecipe craft = craftOptional.get();
                        ItemStack itemStack = craft.getCraft();
                        if (gm.getConfig().get(MainConfig.ENABLE_CRAFTS_PERMISSIONS) && itemStack.getItemMeta() != null && itemStack.getItemMeta().getLore() != null) {
                            String permission = "uhc-core.craft." + itemStack.getItemMeta().getLore().get(0).toLowerCase().replaceAll(" ", "-");
                            if (!player.hasPermission(permission)) {
                                player.sendMessage(Lang.ITEMS_CRAFT_NO_PERMISSION.replace("%craft%", ChatColor.translateAlternateColorCodes('&', itemStack.getItemMeta().getDisplayName())));
                                event.setCancelled(true);
                                return;
                            }
                        }
                        if (event.isShiftClick()) {
                            ItemStack addedItems = itemStack.clone();
                            int addedItemCount = ((int) Math.floor(
                                    (double) Math.min(getAddableItemCount(event.getView().getBottomInventory(), addedItems),
                                            itemStack.getAmount() * getMaximumCrafts(inventory)) / itemStack.getAmount())
                            );
                            if (craft.hasLimit()) {
                                addedItemCount = Math.min(addedItemCount, craft.getLimit() - getCraftedTimes(player, craft.getRealCraft()));
                            }
                            addedItems.setAmount(addedItemCount * itemStack.getAmount());
                            event.getWhoClicked().getInventory().addItem(addedItems);
                            reduce(inventory, addedItemCount);
                            addCraftedTimes(player, craft.getRealCraft(), addedItemCount);
                        } else {
                            if (craft.hasLimit() && getCraftedTimes(player, craft.getRealCraft()) == craft.getLimit()) {
                                return;
                            }
                            ItemStack cursor = event.getCursor();
                            if (cursor == null || cursor.getType() == Material.AIR || itemStack.isSimilar(cursor)) {
                                int amount;
                                if (cursor == null) {
                                    amount = itemStack.getAmount();
                                } else {
                                    amount = cursor.getAmount() + itemStack.getAmount();
                                }
                                if (amount <= itemStack.getType().getMaxStackSize()) {
                                    ItemStack newStack = itemStack.clone();
                                    newStack.setAmount(amount);
                                    event.getView().setCursor(newStack);
                                    reduce(inventory, 1);
                                    addCraftedTimes(player, craft.getRealCraft(), 1);
                                }
                            }
                        }
                    }
                } else {
                    event.setCancelled(true);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateInventory(player, event.getView().getTopInventory());
                }
            }.runTaskLater(plugin, 1);
        }

        /*if(event.getInventory().getType().equals(InventoryType.BREWING) && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS)){
            final BrewerInventory inv = (BrewerInventory) event.getInventory();
            final HumanEntity human = event.getWhoClicked();
            Bukkit.getScheduler().runTaskLater(plugin, new CheckBrewingStandAfterClick(inv.getHolder(), human),1);
        }*/
    }

    @EventHandler
    void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CraftRecipeViewerInventoryHolder ||
                event.getInventory().getHolder() instanceof CraftRecipeInventoryHolder) {
            event.setCancelled(true);
        }
        if (event.getInventory().getHolder() instanceof CraftingInventoryHolder) {
            Inventory inventory = event.getView().getTopInventory();
            for (int slot : event.getRawSlots()) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, slot);
                if (item instanceof RecipeSlotItem && ((RecipeSlotItem) item).getSlot() == 0) {
                    event.setCancelled(true);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateInventory((Player) event.getWhoClicked(), inventory);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    @EventHandler
    void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().openInventory(recipe.createCraftingInventory());
                }
            }.runTaskLater(plugin, 1);
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CraftingInventoryHolder) {
            for (int i = 0; i < Config.CRAFTING_PATTERN.size(); i++) {
                for (int j = 0; j < 9; j++) {
                    InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, i, j);
                    if (item instanceof RecipeSlotItem && ((RecipeSlotItem) item).getSlot() != 0) {
                        if (event.getInventory().getItem(i * 9 + j) != null) {
                            Map<Integer, ItemStack> map = event.getPlayer().getInventory().addItem(event.getInventory().getItem(i * 9 + j));
                            for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
                                event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    int getCraftedTimes(Player player, Craft craft) {

        UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);

        return craftedItems.getOrDefault(uhcPlayer, new HashMap<>()).getOrDefault(craft.getCraft(), 0);
    }

    void addCraftedTimes(Player player, Craft craft, int amount) {
        UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
        if (craft.getCraft() == null) {
            return;
        }
        if (!craftedItems.containsKey(uhcPlayer)) {
            craftedItems.put(uhcPlayer, new HashMap<>());
        }
        Map<ItemStack, Integer> map = craftedItems.get(uhcPlayer);
        if (!map.containsKey(craft.getCraft())) {
            map.put(craft.getCraft(), amount);
        } else {
            map.put(craft.getCraft(), map.get(craft.getCraft()) + amount);
        }
    }

    void reduce(Inventory inventory, int amount) {
        for (int i = 0; i < Config.CRAFTING_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, i, j);
                if (item instanceof RecipeSlotItem && ((RecipeSlotItem) item).getSlot() != 0) {
                    ItemStack itemStack = inventory.getItem(i * 9 + j);
                    if (itemStack != null) {
                        if (itemStack.getType().toString().contains("BUCKET")) {
                            itemStack = new ItemStack(Material.BUCKET);
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - amount);
                        }
                    }
                    inventory.setItem(i * 9 + j, itemStack);
                }
            }
        }
    }

    int getAddableItemCount(Inventory inventory, ItemStack itemStack) {
        int count = 0;
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() == Material.AIR) {
                count += itemStack.getType().getMaxStackSize();
            } else if (itemStack.isSimilar(stack)) {
                count += itemStack.getType().getMaxStackSize() - stack.getAmount();
            }
        }
        return count;
    }

    void updateInventory(Player player, Inventory inventory) {
        Optional<CraftRecipe> craft = getCurrentCraft(inventory, player);
        ItemStack newStack;
        newStack = craft.map(value -> value.getCraft().clone()).orElse(BARRIER);
        ItemMeta meta = newStack.getItemMeta();
        if (meta != null && newStack != BARRIER) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("");
            lore.add(Config.CLICK_TO_CRAFT.replace("&", "\u00A7"));
            if (craft.isPresent() && craft.get().hasLimit()) {
                lore.add(Config.LIMIT_TIMES.replace("&", "\u00A7")
                        .replace("{times}", String.valueOf(getCraftedTimes(player, craft.get().getRealCraft())))
                        .replace("{limit}", String.valueOf(craft.get().getLimit())));
            }
            meta.setLore(lore);
            newStack.setItemMeta(meta);
        }
        for (int i = 0; i < Config.CRAFTING_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, i, j);
                if (item instanceof RecipeSlotItem && ((RecipeSlotItem) item).getSlot() == 0) {
                    inventory.setItem(i * 9 + j, newStack);
                }
            }
        }
    }

    boolean matches(ItemStack[] stacks, Craft craft) {
        for (int i = 0; i < 9; i++) {
            if (stacks[i] == null) {
                stacks[i] = new ItemStack(Material.AIR);
            }
            ItemStack oriItem = stacks[i];
            ItemStack oriTarget = craft.getRecipe().get(i);
            if (!craft.getRecipe().get(i).hasItemMeta()) {
                if (!(oriItem.getType() == oriTarget.getType())) {
                    return false;
                }
            } else {
                ItemStack item = oriItem.clone();
                ItemStack target = oriTarget.clone();
                if (item.hasItemMeta() && target.hasItemMeta()) {
                    var itemMeta = item.getItemMeta();
                    var targetMeta = target.getItemMeta();
                    // ignore damage
                    if (itemMeta instanceof Damageable && targetMeta instanceof Damageable) {
                        ((Damageable) itemMeta).setDamage(0);
                        ((Damageable) targetMeta).setDamage(0);
                    }
                    if (itemMeta instanceof SkullMeta && targetMeta instanceof SkullMeta) {
                        ((SkullMeta) itemMeta).setOwningPlayer(null);
                        ((SkullMeta) targetMeta).setOwningPlayer(null);
                    }
                    // ignore enchantments
                    item.getEnchantments().forEach((k, v) -> item.removeEnchantment(k));
                    target.getEnchantments().forEach((k, v) -> target.removeEnchantment(k));
                    // ignore name
                    itemMeta.setDisplayName(null);
                    targetMeta.setDisplayName(null);
                    // ignore lore
                    itemMeta.setLore(null);
                    targetMeta.setLore(null);
                    // ignore attributes
                    if (itemMeta.hasAttributeModifiers()) {
                        itemMeta.getAttributeModifiers().forEach((k, v) -> itemMeta.removeAttributeModifier(k));
                    }
                    if (targetMeta.hasAttributeModifiers()) {
                        targetMeta.getAttributeModifiers().forEach((k, v) -> itemMeta.removeAttributeModifier(k));
                    }
                    item.setItemMeta(itemMeta);
                    target.setItemMeta(targetMeta);
                }
                if (!target.isSimilar(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    int getMaximumCrafts(Inventory inventory) {
        int maximum = Integer.MAX_VALUE;
        for (int i = 0; i < Config.CRAFTING_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, i, j);
                if (item instanceof RecipeSlotItem && ((RecipeSlotItem) item).getSlot() != 0) {
                    ItemStack itemStack = inventory.getItem(i * 9 + j);
                    if (itemStack != null) {
                        maximum = Math.min(maximum, itemStack.getAmount());
                    }
                }
            }
        }
        return maximum;
    }

    Optional<CraftRecipe> getCurrentCraft(Inventory inventory, Player player) {
        ItemStack[] itemStacks = new ItemStack[9];
        for (int i = 0; i < Config.CRAFTING_PATTERN.size(); i++) {
            for (int j = 0; j < 9; j++) {
                InventoryItem item = recipe.getInventoryItem(Config.CRAFTING_PATTERN, i, j);
                if (item instanceof RecipeSlotItem) {
                    int slot = ((RecipeSlotItem) item).getSlot();
                    if (slot != 0) {
                        itemStacks[slot - 1] = inventory.getItem(i * 9 + j);
                    }
                }
            }
        }
        for (Craft craft : CraftsManager.getCrafts()) {
            if (matches(itemStacks, craft)) {
                return Optional.of(new CraftRecipe(craft.getLimit(), craft.getCraft(), craft));
            }
        }
        Recipe recipe = Bukkit.getCraftingRecipe(itemStacks, player.getWorld());
        if (recipe != null) {
            return Optional.of(new CraftRecipe(-1, recipe.getResult(), null));
        }
        return Optional.empty();
    }

    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void onHopperEvent(InventoryMoveItemEvent event) {
        Inventory inv = event.getDestination();
        if(inv.getType().equals(InventoryType.BREWING) && config.get(MainConfig.BAN_LEVEL_TWO_POTIONS) && inv.getHolder() instanceof BrewingStand){
            Bukkit.getScheduler().runTaskLater(plugin, new PlayerListener.CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(), null),1);
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
            PotionType type0 = null;
            PotionType type1 = null;
            PotionType type2 = null;
            if (stand.getInventory().getItem(0) != null){
                PotionMeta potion0 = (PotionMeta) stand.getInventory().getItem(0).getItemMeta();
                type0 = potion0.getBasePotionData().getType();
            }
            if (stand.getInventory().getItem(1) != null){
                PotionMeta potion1 = (PotionMeta) stand.getInventory().getItem(1).getItemMeta();
                type1 = potion1.getBasePotionData().getType();
            }
            if (stand.getInventory().getItem(2) != null){
                PotionMeta potion2 = (PotionMeta) stand.getInventory().getItem(2).getItemMeta();
                type2 = potion2.getBasePotionData().getType();
            }

            if(ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)
                    && (type0==PotionType.STRENGTH||type1==PotionType.STRENGTH||type2==PotionType.STRENGTH)){
                if(human != null){
                    human.sendMessage(Lang.ITEMS_POTION_BANNED);
                }

                stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
                stand.getInventory().setIngredient(new ItemStack(Material.AIR));
            }
        }
    }*/
}
