package com.kiee.offlineeconomy;

import com.kiee.offlineeconomy.blocks.BlockList;
import com.kiee.offlineeconomy.blocks.ShopBlock;
import com.kiee.offlineeconomy.blocks.ShopBlockContainer;
import com.kiee.offlineeconomy.blocks.ShopBlockTile;
import com.kiee.offlineeconomy.handlers.Parser;
import com.kiee.offlineeconomy.setup.ClientProxy;
import com.kiee.offlineeconomy.setup.IProxy;
import com.kiee.offlineeconomy.setup.ModSetup;
import com.kiee.offlineeconomy.setup.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
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
        new Parser().init();
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new ShopBlock());
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new BlockItem(BlockList.SHOPBLOCK, new Item.Properties().group(ItemGroup.MISC)).setRegistryName("shop_block"));
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(TileEntityType.Builder.create(ShopBlockTile::new, BlockList.SHOPBLOCK).build(null).setRegistryName("shop_block"));
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> {
                BlockPos position = data.readBlockPos();
                return new ShopBlockContainer(windowId, OfflineEconomy.proxy.getClientWorld(), position, inv, OfflineEconomy.proxy.getClientPlayer());
            }).setRegistryName("shop_block"));
        }
    }
}
