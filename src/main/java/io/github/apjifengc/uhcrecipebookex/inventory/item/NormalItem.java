package io.github.apjifengc.uhcrecipebookex.inventory.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NormalItem extends InventoryItem {
    private final ItemStack itemStack;

    private final String name;

    private final String lore;

    public NormalItem(ConfigurationSection section, Character character) throws IllegalArgumentException {
        if (section.getString("material") == null) {
            throw new IllegalArgumentException("The '" + character + "' char's item doesn't have material!");
        }
        itemStack = new ItemStack(Material.valueOf(section.getString("material")));
        itemStack.setAmount(section.getInt("amount", 1));
        name = section.getString("name");
        lore = section.getString("lore");
        if (section.getBoolean("glow", false)) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
        }
    }

    public NormalItem(Material material) throws IllegalArgumentException {
        itemStack = new ItemStack(material);
        name = null;
        lore = null;
    }

    @Override
    public ItemStack getItemStack(int page) {
        ItemStack newItemStack = itemStack.clone();
        ItemMeta meta = newItemStack.getItemMeta();
        if (name != null) {
            meta.setDisplayName("\u00A7r" + name.replace("{page_num}", String.valueOf(page + 1))
                    .replace("&", "\u00A7"));
        }
        if (lore != null) {
            meta.setLore(
                    Arrays.stream(lore.replace("{page_num}", String.valueOf(page + 1))
                            .replace("&", "\u00A7")
                            .split("\n"))
                            .map(s -> "\u00A7r" + s)
                            .collect(Collectors.toList())
            );
        }
        newItemStack.setItemMeta(meta);
        return newItemStack;
    }
}
