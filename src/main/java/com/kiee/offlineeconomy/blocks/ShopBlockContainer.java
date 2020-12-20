package com.kiee.offlineeconomy.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_CONTAINER;

public class ShopBlockContainer extends Container {

    private TileEntity tileEntity;
    private PlayerEntity playerEntity;
    private IItemHandler playerInventory;
    public static IItemHandler shopList;

    public ShopBlockContainer(int id, World world, BlockPos position, PlayerInventory playerInventory, PlayerEntity player) {
        super(SHOPBLOCK_CONTAINER, id);
        tileEntity = world.getTileEntity(position);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);


        //addSlotBox(tileEntity.getTileData()., 0, 35, 13, 6, 18, 5, 18);

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {

            Slot input = addSlot(new SlotItemHandler(itemHandler, 0, 8, 49 )); // Input
            Slot output = addSlot(new SlotItemHandler(new ItemStackHandler(1), 0, 152, 49 )); // Output

            // I literally couldn't get anything else to work.. I hope this is good enough.
            // Since I can't save the ItemHandler without causing issues, I'll just generate it every time the UI is opened
            // but fetch it's contents from a list. I need to grab the contents from a config anyways so this should be fine.
            IItemHandler test = new ItemStackHandler(30);
            int index = 0;
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 5; y++) {
                    addSlot(new SlotItemHandler(test, index, 35+(18 * x), 13+(18 * y) )); // ShopSlot
                    index++;
                }
            }
        });

        // The position of the top-left corner of the player inventory
        layoutPlayerInventorySlots(8, 129);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()), playerEntity, BlockList.SHOPBLOCK);
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i=0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y ));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int i=0; i < verAmount; i++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player Inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Player HotBar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

}
