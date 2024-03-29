package com.zpedroo.voltzshop.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.voltzshop.VoltzShop;
import com.zpedroo.voltzshop.category.CategoryItem;
import com.zpedroo.voltzshop.hooks.VaultHook;
import com.zpedroo.voltzshop.managers.InventoryManager;
import com.zpedroo.voltzshop.utils.config.Messages;
import com.zpedroo.voltzshop.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;

public class PlayerChatListener implements Listener {

    private static HashMap<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(32);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(ChatMessageEvent event) {
        if (!getPlayerChat().containsKey(event.getSender())) return;

        event.setCancelled(true);

        PlayerChat playerChat = getPlayerChat().remove(event.getSender());
        Player player = playerChat.getPlayer();
        Integer amount = null;

        try {
            amount = Integer.parseInt(event.getMessage());
        } catch (Exception ex) {
            // ignore
        }

        if (amount == null || amount <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        CategoryItem item = playerChat.getItem();
        Integer limit = item.getDisplay().getMaxStackSize() == 1 ? 36 : 2304;
        if (amount > limit) amount = limit;

        Integer freeSpace = InventoryManager.getInstance().getFreeSpace(player, item.getDisplay());
        if (freeSpace < amount) {
            player.sendMessage(StringUtils.replaceEach(Messages.NEED_SPACE, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().formatDecimal(freeSpace.doubleValue()),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue())
            }));
            return;
        }

        BigInteger money = VaultHook.getMoney(player);
        BigInteger price = item.getPrice().multiply(BigInteger.valueOf(amount));

        if (money.compareTo(price) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_MONEY, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().format(money),
                    NumberFormatter.getInstance().format(price)
            }));
            return;
        }

        VaultHook.removeMoney(player, price);
        if (item.getShopItem() != null) {
            ItemStack toGive = item.getShopItem().clone();
            if (toGive.getMaxStackSize() == 64) {
                toGive.setAmount(amount);
                player.getInventory().addItem(toGive);
                return;
            }

            for (int i = 0; i < amount; ++i) {
                player.getInventory().addItem(toGive);
            }
        }

        for (String cmd : item.getCommands()) {
            if (cmd == null) continue;

            final Integer finalAmount = amount;
            VoltzShop.get().getServer().getScheduler().runTaskLater(VoltzShop.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                    "{player}",
                    "{amount}"
            }, new String[]{
                    player.getName(),
                    String.valueOf(finalAmount * item.getDefaultAmount())
            })), 0L);
        }

        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
            if (msg == null) continue;

            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                    "{item}",
                    "{amount}",
                    "{price}"
            }, new String[]{
                    item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue()),
                    NumberFormatter.getInstance().format(price)
            }));
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 100f);
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private Player player;
        private CategoryItem item;

        public PlayerChat(Player player, CategoryItem item) {
            this.player = player;
            this.item = item;
        }

        public Player getPlayer() {
            return player;
        }

        public CategoryItem getItem() {
            return item;
        }
    }
}