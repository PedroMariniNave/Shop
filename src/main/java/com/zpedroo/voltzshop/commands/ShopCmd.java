package com.zpedroo.voltzshop.commands;

import com.zpedroo.voltzshop.category.Category;
import com.zpedroo.voltzshop.managers.ShopManager;
import com.zpedroo.voltzshop.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (player == null && args.length >= 3) {
            switch (args[0].toUpperCase()) {
                case "OPEN":
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) return true;

                    Category category = ShopManager.getInstance().getCategoryDataCache().getCategory(args[2]);
                    if (category == null) return true;

                    Menus.getInstance().openCategoryMenu(target, category);
                    return true;
            }
        }

        if (player == null) return true;
        
        Menus.getInstance().openMainMenu(player);
        return true;
    }
}