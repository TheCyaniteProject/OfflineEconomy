package com.kiee.offlineeconomy.handlers;

import com.kiee.offlineeconomy.blocks.ShopBlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Parser {

    public static String configFileName = "offlineeconomy.cfg";
    public static Path configDir;
    public static String gameDir = "";

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

    int count = 0;
    int count2 = 0;
    private void Read() {
        count = 0;
        count2 = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(configDir.toFile()));
            String line = reader.readLine();
            while (line != null) {
                Boolean lineCheck = this.parseLine(line);
                if (!lineCheck)
                    count2 ++;
                // read next line
                line = reader.readLine();
            }
            reader.close();
            System.out.println("OfflineEconomy: Finished Registering " + count + " items");
            System.out.println("OfflineEconomy: " + count2 + " errors");
        } catch (IOException e) {
            System.err.println("OfflineEconomy: Failed to Parse Items");
            e.printStackTrace();
        }
    }

    private Boolean parseLine(String line) {

        // item, count, cost, fixed-sale-price (optional)
        // "shop_item = minecraft:diamond, 10, 1"

        line = line.replaceAll("\\s", "");

        if (line.startsWith("sell_value_multiplier=")) {

            line = line.replace("sell_value_multiplier=", "");
            ShopBlockContainer.sell_value_multiplier = Float.parseFloat(line);
            return true;

        } else if (line.startsWith("shop_item=")) {

            line = line.replace("shop_item=", "");
            String[] item = line.split(",");
            if (item.length < 2) {
                System.err.println("OfflineEconomyError: OutOfBoundsError: Improper formatting: \"" + line + "\"");
                return false;
            }
            String itemName = item[0];
            float itemCost = 1;
            float sellValue = -1;
            if (item.length > 2) {
                sellValue = Float.parseFloat(item[2]);
            }
            try {
                itemCost = Float.parseFloat(item[1]);
                if (item.length > 2) {
                    sellValue = Float.parseFloat(item[2]);
                }
            } catch (NumberFormatException e) {
                System.err.println("OfflineEconomyError: NumberFormatException: Improper formatting \"" + line + "\"");
                return false;
            }
            Item newItem;
            try {
                newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            } catch (ResourceLocationException e) {
                System.err.println("OfflineEconomyError: ItemNameError: " + itemName + " is not a valid item name \"" + line + "\"");
                return false;
            }
            ShopItem shopItem = new ShopItem(itemName, newItem, itemCost, sellValue);
            if (newItem != null && !ShopBlockContainer.shopItems.contains(shopItem) && newItem != Items.AIR) {
                ShopBlockContainer.shopItems.add(shopItem);
                System.out.println("OfflineEconomy: Registered item: " + newItem.getItem().getName().getString());
            }
            count ++;
            return true;

        }
        if (!line.startsWith("#") && !line.equals("")) {
            System.err.println("OfflineEconomyError: UnknownError: Unparseable string \"" + line + "\"");
            return false;
        } else {
            return true;
        }
    }

    /*public static void SavePlayerData() {
        Path path = Paths.get(gameDir, "config", "");
        Minecraft.getInstance().world.
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(configDir.toFile()));
            String line = reader.readLine();
            while (line != null) {
                Boolean lineCheck = this.parseLine(line);
                if (!lineCheck) {

                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//*/

    public void add(String item) {

    }

    public void set(String item) {

    }

    public void remove(String item) {

    }

}
