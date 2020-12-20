package com.kiee.offlineeconomy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class ShopBlock extends Block {

    public ShopBlock() {
        super(Properties.create(Material.WOOD)
                .sound(SoundType.WOOD)
                .hardnessAndResistance(2.0f)
        );
        setRegistryName("shop_block");
    }
}
