package com.zpedroo.voltzshop.managers;

import com.zpedroo.voltzshop.VoltzShop;
import com.zpedroo.voltzshop.category.Category;
import com.zpedroo.voltzshop.category.CategoryItem;
import com.zpedroo.voltzshop.category.cache.CategoryDataCache;
import com.zpedroo.voltzshop.utils.builder.ItemBuilder;
import com.zpedroo.voltzshop.utils.formatter.NumberFormatter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private static ShopManager instance;
    public static ShopManager getInstance() { return instance; }

    private CategoryDataCache categoryDataCache;

    public ShopManager() {
        instance = this;
        this.categoryDataCache = new CategoryDataCache();
        this.loadCategories();
    }

    private void loadCategories() {
        File folder = new File(VoltzShop.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));

        if (files == null) return;
        for (File fl : files) {
            if (fl == null) continue;

            YamlConfiguration file = YamlConfiguration.loadConfiguration(fl);
            String name = fl.getName().replace(".yml", "");
            String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title", "NULL"));
            Integer size = file.getInt("Inventory.size");
            List<CategoryItem> items = new ArrayList<>(64);

            for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
                if (str == null) continue;

                BigInteger price = new BigInteger(file.getString("Inventory.items." + str + ".price", "0"));
                ItemStack display = ItemBuilder.build(file, "Inventory.items." + str + ".display", new String[]{
                        "{price}"
                }, new String[]{
                        NumberFormatter.getInstance().format(price)
                }).build();
                ItemStack shopItem = ItemBuilder.build(file, "Inventory.items." + str + ".shop-item").build();

                items.add(new CategoryItem(price, display, shopItem));
            }

            Category category = new Category(file, title, size, items);
            cache(name, category);
        }
    }

    private void cache(String name, Category category) {
        getCategoryDataCache().getCategories().put(name, category);
    }

    public CategoryDataCache getCategoryDataCache() {
        return categoryDataCache;
    }
}