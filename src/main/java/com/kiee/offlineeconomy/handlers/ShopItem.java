package com.kiee.offlineeconomy.handlers;

import net.minecraft.item.Item;

public class ShopItem {
    public String name;
    public Item item;
    public float cost;
    public float sellValue = -1;


    public ShopItem(String name, Item item, float cost) {
        this.name = name;
        this.item = item;
        this.cost = cost;
    }
    public ShopItem(String name, Item item, float cost, float sellValue) {
        this.name = name;
        this.item = item;
        this.cost = cost;
        this.sellValue = sellValue;
    }
}
