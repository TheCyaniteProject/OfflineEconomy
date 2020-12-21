package com.kiee.offlineeconomy.blocks;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_CONTAINER;

public class ShopBlockContainer extends Container {

    private TileEntity tileEntity;
    private PlayerEntity playerEntity;
    private IItemHandler playerInventory;

    public ShopBlockContainer(int id, World world, BlockPos position, PlayerInventory playerInventory, PlayerEntity player) {
        super(SHOPBLOCK_CONTAINER, id);
        tileEntity = world.getTileEntity(position);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inputHandler -> {

            Slot input = addSlot(new SlotItemHandler(inputHandler, 0, 8, 49 )); // Input
            IItemHandler outputHandler = new ItemStackHandler(1) { // Output
                @Nonnull
                @Override // Prevents player from adding to slot
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    return stack;
                }

            };
            addSlot(new SlotItemHandler(outputHandler, 0, 152, 49 ) );

            int itemCost = 10;
            // .getStackInSlot(0).getItem().getTranslationKey()

            IItemHandler shopItemsHandler = new ItemStackHandler(30) {
                @Nonnull
                @Override // Prevents player from removing from slot
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (inputHandler.getStackInSlot(0).getCount() >= itemCost && inputHandler.getStackInSlot(0).getItem() == Items.EMERALD) { // if requirements are met
                        inputHandler.extractItem(0, itemCost, simulate);
                        outputHandler.insertItem(0, new ItemStack(
                                this.getStackInSlot(0).getItem(),
                                this.getStackInSlot(0).getCount()
                        ), simulate);
                        //System.out.println(this.getStackInSlot(0).getItem().getTranslationKey());
                        return super.extractItem(slot, amount, simulate);
                    }
                    return ItemStack.EMPTY;
                }
                @Nonnull
                @Override // Prevents player from adding to slot
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    if (simulate) {
                        super.insertItem(slot, stack, false);
                    }
                    return stack;
                }

            };
            int index = 0;
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 5; y++) {
                    //new ShopButton(18, 18, 35+(18 * x), 13+(18 * y), "", index, (event) -> {});
                    Slot slot = addSlot(new SlotItemHandler(shopItemsHandler, index, 35+(18 * x), 13+(18 * y) )); // ShopSlot
                    shopItemsHandler.insertItem(index, new ItemStack(Items.DIAMOND, 2), true);
                    index++;
                }
            }
        });

        // The position of the top-left corner of the player inventory
        layoutPlayerInventorySlots(8, 129);
    }

    public class ShopItemHandler extends ItemStackHandler {
        private boolean locked = false;

        public void lock() {
            locked = true;
        }
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
