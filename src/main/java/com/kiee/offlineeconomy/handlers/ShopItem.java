package com.kiee.offlineeconomy.handlers;

import net.minecraft.item.Item;

public class ShopItem {
    public String name;
    public Item item;
    public int count;
    public int cost;

    public ShopItem(String name, Item item, int count, int cost) {
        this.name = name;
        this.item = item;
        this.count = count;
        this.cost = cost;
    }
}
