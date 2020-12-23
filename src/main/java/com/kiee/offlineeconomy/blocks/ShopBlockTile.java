package com.kiee.offlineeconomy.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_TILE;

public class ShopBlockTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);

    public ShopBlockTile() {
        super(SHOPBLOCK_TILE);
    }


    private boolean hasTicked = false;

    @Override
    public void tick() {
        /// Reset shop items list each day
        if (Minecraft.getInstance().player == null) return;
        long time = Minecraft.getInstance().player.world.getDayTime();
        if (time > 24000) {
            time = -((long)(Math.floor(time / 24000) * 24000L) - time);
        }
        if (time < 1500 && !hasTicked) {
            hasTicked = true;
            System.out.println("OfflineEconomy: It's a new day, a new set of items in the shop!");
            ShopBlockContainer.hasGenerated = false;
        } else if (time > 1500 && hasTicked) {
            hasTicked = false;
        }
    }

    private IItemHandler createHandler() {
        return new ItemStackHandler(1);
    }


    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ShopBlockContainer(i, world, pos, playerInventory, playerEntity);
    }


}
