package com.zpedroo.voltzshop.utils.menus;

import com.zpedroo.voltzshop.category.Category;
import com.zpedroo.voltzshop.category.CategoryItem;
import com.zpedroo.voltzshop.listeners.PlayerChatListener;
import com.zpedroo.voltzshop.managers.ShopManager;
import com.zpedroo.voltzshop.utils.FileUtils;
import com.zpedroo.voltzshop.utils.builder.InventoryBuilder;
import com.zpedroo.voltzshop.utils.builder.InventoryUtils;
import com.zpedroo.voltzshop.utils.builder.ItemBuilder;
import com.zpedroo.voltzshop.utils.config.Messages;
import com.zpedroo.voltzshop.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Menus {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private InventoryUtils inventoryUtils;
    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.inventoryUtils = new InventoryUtils();
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            if (items == null) continue;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String actionStr = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            if (!StringUtils.equals(actionStr, "NULL")) {
                if (actionStr.contains("OPEN:")) {
                    String categoryName = actionStr.split(":")[1];
                    Category category = ShopManager.getInstance().getCategoryDataCache().getCategory(categoryName);
                    if (category == null) continue;

                    getInventoryUtils().addAction(inventory, item, () -> {
                        openCategoryMenu(player, category);
                    }, InventoryUtils.ActionClick.ALL);
                }
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openCategoryMenu(Player player, Category category) {
        FileConfiguration file = category.getFile();

        int size = category.getSize();
        String title = category.getTitle();
        Inventory inventory = Bukkit.createInventory(null, size, title);

        int i = -1;
        String[] slots = file.getString("Inventory.item-slots").replace(" ", "").split(",");
        List<ItemBuilder> builders = new ArrayList<>(64);
        for (CategoryItem item : category.getItems()) {
            if (item == null) continue;
            if (++i >= slots.length) i = 0;

            ItemStack display = item.getDisplay().clone();
            int slot = Integer.parseInt(slots[i]);
            List<InventoryUtils.Action> actions = new ArrayList<>();

            actions.add(new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, display, () -> {
                player.closeInventory();
                PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(player, item));
                ItemStack toGive = item.getShopItem().clone();

                for (String msg : Messages.CHOOSE_AMOUNT) {
                    if (msg == null) continue;

                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            toGive.hasItemMeta() ? toGive.getItemMeta().hasDisplayName() ? toGive.getItemMeta().getDisplayName() : toGive.getType().toString() : toGive.getType().toString(),
                            NumberFormatter.getInstance().format(item.getPrice())
                    }));
                }
            }));

            builders.add(ItemBuilder.build(display, slot, actions));
        }

        if (file.contains("Inventory.displays")) {
            for (String display : file.getConfigurationSection("Inventory.displays").getKeys(false)) {
                if (display == null) continue;

                ItemStack item = ItemBuilder.build(file, "Inventory.displays." + display).build();
                int slot = file.getInt("Inventory.displays." + display + ".slot");

                inventory.setItem(slot, item);
            }
        }

        int nextPageSlot = file.getInt("Inventory.next-page-slot");
        int previousPageSlot = file.getInt("Inventory.previous-page-slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    private InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }
}