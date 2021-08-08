package com.zpedroo.voltzshop;

import com.zpedroo.voltzshop.commands.ShopCmd;
import com.zpedroo.voltzshop.hooks.VaultHook;
import com.zpedroo.voltzshop.listeners.PlayerChatListener;
import com.zpedroo.voltzshop.managers.InventoryManager;
import com.zpedroo.voltzshop.managers.ShopManager;
import com.zpedroo.voltzshop.utils.FileUtils;
import com.zpedroo.voltzshop.utils.config.Settings;
import com.zpedroo.voltzshop.utils.formatter.NumberFormatter;
import com.zpedroo.voltzshop.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class VoltzShop extends JavaPlugin {

    private static VoltzShop instance;
    public static VoltzShop get() { return instance; }

    public void onEnable() {
        instance = this;

        new FileUtils(this);
        new VaultHook();
        new NumberFormatter(getConfig());
        new InventoryManager();
        new Menus();
        new ShopManager();

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        String command = Settings.COMMAND;
        List<String> aliases = Settings.ALIASES;
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(new ShopCmd());

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
    }
}