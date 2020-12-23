package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.handlers.ShopItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_CONTAINER;

public class ShopBlockContainer extends Container {

    public static Item currencyItem = Items.EMERALD;
    public static ArrayList<ShopItem> shopItems = new ArrayList<>();
    private static ArrayList<ShopItem> _shopItems = new ArrayList<>();

    private TileEntity tileEntity;
    public static PlayerEntity playerEntity;
    private IItemHandler playerInventory;

    private IItemHandler inputHandler;
    private IItemHandler outputHandler;
    IItemHandler shopItemsHandler;
    public static boolean hasGenerated = false;
    private ArrayList<ShopItem> shopSlots = new ArrayList<>();
    private ArrayList<Slot> _shopSlots = new ArrayList<>();

    public ShopBlockContainer(int id, World world, BlockPos position, PlayerInventory playerInventory, PlayerEntity player) {
        super(SHOPBLOCK_CONTAINER, id);

        this.tileEntity = world.getTileEntity(position);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);

        if (!hasGenerated) {
            System.out.println("Has Generated.");
            generateList();
        }
        GenerateShopItemSlots();

        // The position of the top-left corner of the player inventory
        layoutPlayerInventorySlots(8, 129);
    }



    public void GenerateShopItemSlots() {
        shopSlots.clear();
        _shopSlots.clear();
        hasGenerated = true;

        this.outputHandler = new ItemStackHandler(1) { // Output

            @Nonnull
            @Override // Prevents player from adding to slot
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (simulate) {
                    this.getStackInSlot(0).setCount(0); // clear slot
                    super.insertItem(slot, stack, false);
                }
                return stack;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!simulate) {
                    inputHandler.getStackInSlot(0).setCount(0);
                }
                return super.extractItem(slot, amount, simulate);
            }
        };
        Slot outputSlot = addSlot(new SlotItemHandler(outputHandler, 0, 15, 61 ) );
        this.inputHandler = new ItemStackHandler(1) { // Output
            @Override
            protected void onContentsChanged(int slot) {
                if (!this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() != ShopBlockContainer.currencyItem) {

                    ShopItem shopItem = CheckItem(this.getStackInSlot(0).getItem());
                    if (shopItem == null) {
                        outputHandler.getStackInSlot(0).setCount(0);
                        return;
                    }
                    int baseValue = shopItem.cost;
                    int value = (int)((((float) baseValue / (float) shopItem.count) * 0.75f) * this.getStackInSlot(0).getCount());
                    if (shopItem.sellValue != -1) {
                        value = (int)(((float) shopItem.sellValue / (float) shopItem.count) * this.getStackInSlot(0).getCount());
                    }

                    if (value >= 1) {
                        outputHandler.insertItem(0, new ItemStack(ShopBlockContainer.currencyItem, value), true);
                    }
                } else {
                    outputHandler.getStackInSlot(0).setCount(0);
                }
                super.onContentsChanged(slot);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, amount, simulate);
            }
        };
        Slot inputSlot = addSlot(new SlotItemHandler(inputHandler, 0, 15, 36 )); // Input

        _shopSlots.add(outputSlot);
        _shopSlots.add(inputSlot);
        shopSlots.add(null);
        shopSlots.add(null);

        shopItemsHandler = new ItemStackHandler(30) {

            @Nonnull
            @Override // Prevents player from removing from slot
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!inputHandler.getStackInSlot(0).isEmpty() && GetShopItemFromSlot(slot+2) != null && inputHandler.getStackInSlot(0).getCount() >= GetShopItemFromSlot(slot+2).cost && inputHandler.getStackInSlot(0).getItem() == currencyItem) { // if requirements are met
                    inputHandler.extractItem(0, GetShopItemFromSlot(slot+2).cost, simulate);
                    //this.insertItem(slot, new ItemStack(GetShopItemFromSlot(slot).item, GetShopItemFromSlot(slot).count), simulate);
                    return ItemHandlerHelper.copyStackWithSize(this.stacks.get(slot), this.stacks.get(slot).getCount() + amount);
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
        for (int y = 0; y < 5; y++) { // for each vertical item slot
            for (int x = 0; x < 6; x++) { // for each horizontal item slot
                if (_shopItems.size()-1 < index) {
                    break;
                }
                ShopItem currentItem = _shopItems.get(index);
                int itemCount = currentItem.count;
                int itemCost = currentItem.cost;
                Item newItem = currentItem.item;

                Slot newSlot = addSlot(new SlotItemHandler(shopItemsHandler, index, 50+(18 * x), 13+(18 * y) )); // ShopSlot
                shopItemsHandler.insertItem(index, new ItemStack(newItem, itemCount), true);
                _shopSlots.add(newSlot);
                shopSlots.add(currentItem);
                index++;
            }
        }
    }

    @Nonnull
    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        if (!inputHandler.getStackInSlot(0).isEmpty()) {
            playerIn.dropItem(inputHandler.getStackInSlot(0).getStack(), false);
        }
        super.onContainerClosed(playerIn);
    }

    //*
    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {

        PlayerInventory playerinventory = player.inventory;
        System.out.println(clickTypeIn);
        if (slotId < 0) return ItemStack.EMPTY;
        if (clickTypeIn == ClickType.PICKUP_ALL) clickTypeIn = ClickType.PICKUP; //  || clickTypeIn == ClickType.QUICK_CRAFT
        if (clickTypeIn == ClickType.PICKUP && !Minecraft.getInstance().player.inventory.getItemStack().isEmpty()
            && !this.inventorySlots.get(slotId).getStack().isEmpty()
            && !Minecraft.getInstance().player.inventory.getItemStack().isEmpty()
            && this.inventorySlots.get(slotId).getStack().getItem() == Minecraft.getInstance().player.inventory.getItemStack().getItem()
            && slotId > 1 && slotId < 32) {
            int dragEvent = getDragEvent(dragType);
            if (dragEvent != 0) {
                this.resetDrag();
                return ItemStack.EMPTY;
            }
            ItemStack playerStack = playerinventory.getItemStack();
            ItemStack itemStack = shopItemsHandler.extractItem(slotId-2, playerStack.getCount(), false);
            if (itemStack != ItemStack.EMPTY) {
                playerinventory.setItemStack(itemStack);
            }
            return ItemStack.EMPTY;
        }
        if (clickTypeIn == ClickType.QUICK_MOVE) { // shift-clicking
            if (slotId < 2) { // Shift clicking out of input or output slot
                for (int slot = 32; slot < this.inventorySlots.size(); slot++) {
                    if (this.inventorySlots.get(slot).getStack().isEmpty() ||
                            (this.inventorySlots.get(slot).getStack().getItem() == this.inventorySlots.get(slotId).getStack().getItem()
                                    && this.inventorySlots.get(slot).getStack().getCount() + this.inventorySlots.get(slotId).getStack().getCount() <= this.inventorySlots.get(slotId).getStack().getMaxStackSize())) {
                        ItemStack freeSlot = this.inventorySlots.get(slot).getStack();
                        if (slotId == 0) { // output slot
                            ItemStack itemStack = outputHandler.extractItem(0, outputHandler.getStackInSlot(0).getCount(), false);
                            this.inventorySlots.get(slot).putStack(new ItemStack(itemStack.getItem(), itemStack.getCount() + freeSlot.getCount()));
                        } else {
                            ItemStack itemStack = inputHandler.extractItem(0, inputHandler.getStackInSlot(0).getCount(), false);
                            this.inventorySlots.get(slot).putStack(new ItemStack(itemStack.getItem(), itemStack.getCount() + freeSlot.getCount()));
                        }
                        this.inventorySlots.get(slotId).putStack(ItemStack.EMPTY);
                    }
                }
            } else if (slotId > 31) { // Shift clicking from player inventory
                if (this.inventorySlots.get(1).getStack().isEmpty() && !this.inventorySlots.get(slotId).getStack().isEmpty() ||
                        (this.inventorySlots.get(1).getStack().getItem() == this.inventorySlots.get(slotId).getStack().getItem()
                                && this.inventorySlots.get(1).getStack().getCount() + this.inventorySlots.get(slotId).getStack().getCount() <= this.inventorySlots.get(slotId).getStack().getMaxStackSize())) { // if input slot is empty, and current slot is not empty
                    ItemStack freeSlot = this.inventorySlots.get(1).getStack();
                    ItemStack itemStack = this.inventorySlots.get(slotId).getStack().copy();
                    this.inventorySlots.get(1).putStack(new ItemStack(itemStack.getItem(), itemStack.getCount() + freeSlot.getCount()));
                    this.inventorySlots.get(slotId).putStack(ItemStack.EMPTY);
                }
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    private ShopItem GetShopItemFromSlot(int index) {
        return shopSlots.get(index);
    }

    private ItemStack GetStackFromShopItem(ShopItem currentItem) {
        return new ItemStack(currentItem.item, currentItem.count);
    }

    public ShopItem CheckItem(Item item) {
        for (ShopItem shopItem : shopItems) {
            if (shopItem.item == item) {
                return shopItem;
            }
        }
        return null;
    }

    private ArrayList<ShopItem> generateList() {
        ArrayList<String> keys = new ArrayList<>();
        _shopItems.clear();
        Random generator = new Random();
        if (shopItems.size() > 30) {
            for (int index = 0; index < 30; index++ ) {
                int randomIndex = -1;
                while ( randomIndex == -1 || keys.contains(shopItems.get(randomIndex).name) || shopItems.get(randomIndex).cost < 1) {
                    randomIndex = generator.nextInt(shopItems.size());
                    if (keys.size() >= shopItems.size()) {
                        break;
                    }
                }
                if (!keys.contains(shopItems.get(randomIndex).name) && !_shopItems.contains(shopItems.get(randomIndex))) {
                    keys.add(shopItems.get(randomIndex).name);
                    _shopItems.add(shopItems.get(randomIndex));
                }
            }
        } else {
            _shopItems.addAll(shopItems);
        }
        return _shopItems;
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
