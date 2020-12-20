package com.kiee.offlineeconomy;

import com.kiee.offlineeconomy.blocks.BlockList;
import com.kiee.offlineeconomy.blocks.ShopBlock;
import com.kiee.offlineeconomy.setup.ClientProxy;
import com.kiee.offlineeconomy.setup.IProxy;
import com.kiee.offlineeconomy.setup.ModSetup;
import com.kiee.offlineeconomy.setup.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("offlineeconomy")
public class OfflineEconomy
{

    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    public static ModSetup modSetup;

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "offlineeconomy";

    public OfflineEconomy() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    // run after registerItems
    private void setup(final FMLCommonSetupEvent event) {
        modSetup = new ModSetup();
        modSetup.init();

        proxy.init();
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new ShopBlock());
            LOGGER.info("Registered Blocks");
        }
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new BlockItem(BlockList.SHOPBLOCK, new Item.Properties()).setRegistryName("shop_block"));
            LOGGER.info("Registered Items");
        }
    }
}
