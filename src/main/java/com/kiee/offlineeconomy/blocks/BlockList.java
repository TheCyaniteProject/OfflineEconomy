package com.kiee.offlineeconomy.blocks;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class BlockList
{
    @ObjectHolder("offlineeconomy:shop_block")
    public static ShopBlock SHOPBLOCK;

    @ObjectHolder("offlineeconomy:shop_block")
    public static TileEntityType<ShopBlockTile> SHOPBLOCK_TILE;

    @ObjectHolder("offlineeconomy:shop_block")
    public static ContainerType<ShopBlockContainer> SHOPBLOCK_CONTAINER;
}
