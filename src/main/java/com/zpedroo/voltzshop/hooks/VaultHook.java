package com.zpedroo.voltzshop.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigInteger;

public class VaultHook {

    public VaultHook() {
        this.hook();
    }

    private static Economy economy;

    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economy = rsp.getProvider();
    }

    public static BigInteger getMoney(OfflinePlayer player) {
        return new BigInteger(String.format("%.0f", economy.getBalance(player)));
    }

    public static void removeMoney(OfflinePlayer player, BigInteger amount) {
        economy.withdrawPlayer(player.getName(), Bukkit.getWorlds().get(0).getName(), amount.doubleValue());
    }
}