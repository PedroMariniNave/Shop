package com.zpedroo.voltzshop.category;

import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class CategoryItem {

    private BigInteger price;
    private ItemStack display;
    private ItemStack shopItem;

    public CategoryItem(BigInteger price, ItemStack display, ItemStack shopItem) {
        this.price = price;
        this.display = display;
        this.shopItem = shopItem;
    }

    public BigInteger getPrice() {
        return price;
    }

    public ItemStack getDisplay() {
        return display;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }
}