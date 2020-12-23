package com.kiee.offlineeconomy.handlers;

import com.kiee.offlineeconomy.blocks.ShopBlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Parser {

    public String configFileName = "offlineeconomy.cfg";
    public Path configDir;
    public String gameDir = "";

    public void init() {
        try {
            gameDir = new File( Minecraft.getInstance().gameDir.getPath() ).getCanonicalPath();
            configDir = Paths.get(gameDir, "config", configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Write();
        Read();
    }

    private void Write() {
        File configFile = configDir.toFile();
        if (configFile.exists()) return;
        try {
            if (configFile.createNewFile()) {
                //System.out.println("OfflineEconomy: " + Parser.class.getResource("/defaultconfig.cfg"));
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream("defaultconfig.cfg");
                //OutputStream out = new FileOutputStream(configFile);
                FileUtils.copyInputStreamToFile(stream, configFile);
                stream.close();
            }
        } catch (Exception e) {
            System.err.println("OfflineEconomy: Config Init Exception!");
            e.printStackTrace();
        }
        if (configDir.toFile().exists()) {
            System.out.println("OfflineEconomy: Saved Config to: " + configDir);
        } else {
            System.err.println("OfflineEconomy: Config Init Failed");
        }
    }

    private void Read() {
        BufferedReader reader;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(configDir.toFile()));
            String line = reader.readLine();
            while (line != null) {
                Boolean lineCheck = this.parseLine(line);
                if (lineCheck) count ++;
                // read next line
                line = reader.readLine();
            }
            reader.close();
            System.out.println("OfflineEconomy: Finished Registering " + count + " Items");
        } catch (IOException e) {
            System.err.println("OfflineEconomy: Failed to Parse Items");
            e.printStackTrace();
        }
    }

    private Boolean parseLine(String line) {

        // currency_item = minecraft:emerald

        // item, count, cost, fixed-sale-price (optional)
        // "shop_item = minecraft:diamond, 1, 10, 1"

        line = line.replaceAll("\\s", "");

        if (line.startsWith("currency_item=")) {
            Item currency;
            try {
                currency = ForgeRegistries.ITEMS.getValue(new ResourceLocation(line.replace("currency_item=", "")));
            } catch (ResourceLocationException e) {
                return false;
            }
            ShopBlockContainer.currencyItem = currency;
        } else if (line.startsWith("shop_item=")) {
            line = line.replace("shop_item=", "");
            String[] item = line.split(",");
            if (item.length < 3) return false;
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
                return false;
            }
            ShopItem shopItem = new ShopItem(itemName, newItem, itemCount, itemCost, sellValue);
            if (itemCount > 0) {
                ShopBlockContainer.shopItems.add(shopItem);
            }
            return true;
        }
        return false;
    }

    public void add(String item, int count, int cost) {

    }

    public void remove(String item) {

    }

}
