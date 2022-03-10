package io.github.apjifengc.uhcrecipebookex;

import io.github.apjifengc.uhcrecipebookex.inventory.item.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {
    public static List<String> INVENTORY_PATTERN;

    public static List<String> RECIPE_VIEWER_PATTERN;

    public static Map<Character, InventoryItem> GUI_ITEM_MAP;

    public static List<String> IGNORE_CRAFTS;

    public static ConfigurationSection MESSAGE;

    public static String GUI_NAME;

    public static String GUI_RECIPE_VIEWER_NAME;

    static void loadConfig() throws NullPointerException {
        FileConfiguration config = UhcRecipeBookEx.getInstance().getConfig();
        INVENTORY_PATTERN = Arrays.asList(
                Objects.requireNonNull(config.getString("inventory.inventory-pattern")).split("\n")
        );
        RECIPE_VIEWER_PATTERN = Arrays.asList(
                Objects.requireNonNull(config.getString("inventory.recipe-viewer-pattern")).split("\n")
        );
        IGNORE_CRAFTS = config.getStringList("ignore-crafts");
        var map = config.getConfigurationSection("inventory.items");
        GUI_ITEM_MAP = new HashMap<>();
        GUI_ITEM_MAP.put(' ', new NormalItem(Material.AIR));
        for (int i = 0; i <= 9; i++) {
            GUI_ITEM_MAP.put((char) ('0' + i), new RecipeSlotItem(i));
        }
        MESSAGE = config.getConfigurationSection("message");
        GUI_NAME = config.getString("inventory.name");
        GUI_RECIPE_VIEWER_NAME = config.getString("inventory.recipe-view-name");
        for (var key : Objects.requireNonNull(map).getKeys(false)) {
            switch (Objects.requireNonNull(map.getString(key + ".type"))) {
                case "item":
                    GUI_ITEM_MAP.put(
                            key.charAt(0),
                            new NormalItem(Objects.requireNonNull(map.getConfigurationSection(key)), key.charAt(0))
                    );
                    break;
                case "slot":
                    GUI_ITEM_MAP.put(
                            key.charAt(0),
                            new SlotItem(Objects.requireNonNull(map.getConfigurationSection(key)))
                    );
                    break;
                case "previous-page":
                    GUI_ITEM_MAP.put(
                            key.charAt(0),
                            new PreviousPageItem(Objects.requireNonNull(map.getConfigurationSection(key)), key.charAt(0))
                    );
                    break;
                case "next-page":
                    GUI_ITEM_MAP.put(
                            key.charAt(0),
                            new NextPageItem(Objects.requireNonNull(map.getConfigurationSection(key)), key.charAt(0))
                    );
                    break;
                case "go-back":
                    GUI_ITEM_MAP.put(
                            key.charAt(0),
                            new GoBackItem(Objects.requireNonNull(map.getConfigurationSection(key)), key.charAt(0))
                    );
                    break;
                default:
                    throw new IllegalArgumentException("The type '" + map.getString(key + ".type") + "' is unknown!");
            }
        }
    }
}