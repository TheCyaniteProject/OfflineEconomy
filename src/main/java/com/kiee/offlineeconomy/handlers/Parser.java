package com.kiee.offlineeconomy.handlers;

import com.kiee.offlineeconomy.blocks.ShopBlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftGame;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Parser {

    public String configFileName = "offlineeconomy.cfg";
    public Path configDir;

    public void init() {
        try {
            String current = new java.io.File( Minecraft.getInstance().gameDir.getPath() ).getCanonicalPath();
            configDir = Paths.get(current, "config", configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(configDir.toFile()));
            String line = reader.readLine();
            while (line != null) {
                this.parseLine(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void parseLine(String line) {

        // currency_item=minecraft:emerald

        // item, count, cost
        // "shop_item = minecraft:diamond, 1, 10"

        line = line.replaceAll("\\s", "");

        if (line.startsWith("currency_item=")) {
            Item currency;
            try {
                currency = ForgeRegistries.ITEMS.getValue(new ResourceLocation(line.replace("currency_item=", "")));
            } catch (ResourceLocationException e) {
                return;
            }
            ShopBlockContainer.currencyItem = currency;
        } else if (line.startsWith("shop_item=")) {
            line = line.replace("shop_item=", "");
            String[] item = line.split(",");

            String itemName = item[0];
            int itemCount = Integer.parseInt(item[1]);
            int itemCost = Integer.parseInt(item[2]);
            int sellValue = -1;
            if (item.length > 3) {
                sellValue = Integer.parseInt(item[3]);
            }
            Item newItem;
            try {
                newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            } catch (ResourceLocationException e) {
                return;
            }
            ShopItem shopItem = new ShopItem(itemName, newItem, itemCount, itemCost, sellValue);
            ShopBlockContainer.shopItems.add(shopItem);
            System.out.println("Finished registerItems");
        }

    }

    public void add(String item, int count, int cost) {

    }

    public void remove(String item) {

    }

}
