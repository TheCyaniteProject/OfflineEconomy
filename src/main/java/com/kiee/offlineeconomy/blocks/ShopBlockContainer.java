package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.handlers.ShopItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Random;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_CONTAINER;

public class ShopBlockContainer extends Container {
    public static float sell_value_multiplier = 0.8f;
    public static ArrayList<ShopItem> shopItems = new ArrayList<>();
    private static ArrayList<ShopItem> _shopItems = new ArrayList<>();

    private final TileEntity tileEntity;
    public static PlayerEntity playerEntity;
    private final IItemHandler playerInventory;
    private static final DataParameter<Float> CREDITS = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.FLOAT);

    public IItemHandler inputHandler;
    private IItemHandler shopItemsHandler;
    public static boolean hasGenerated = false;
    private ArrayList<ShopItem> shopSlots = new ArrayList<>();

    public static float credit = -1; // Amount of stored currency
    String key;

    public ShopBlockContainer(int id, World world, BlockPos position, PlayerInventory playerInventory, PlayerEntity player) {
        super(SHOPBLOCK_CONTAINER, id);

        this.tileEntity = world.getTileEntity(position);
        playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);

        if (!hasGenerated) {
            System.out.println("Has Generated.");
            generateList();
        }

        GenerateShopItemSlots();
        key = "oeBalance_" + playerEntity.getDisplayName().getString();
        if (tileEntity != null && playerEntity.getPersistentData().contains("oeCredits")) {
            tileEntity.getTileData().putFloat(key, playerEntity.getPersistentData().getFloat("oeCredits"));
        }
        if (credit == -1) {
            LoadCredits();
        }

        // The position of the top-left corner of the player inventory
        layoutPlayerInventorySlots(8, 129);
    }



    public void SaveCredits() {
        if (credit != -1) {
            System.out.println("PreSave: " + tileEntity.getTileData().getFloat(key) );
            tileEntity.getTileData().putFloat(key, credit);
            playerEntity.getPersistentData().putFloat("oeCredits", credit);
            System.out.println("Saved: " + credit);
        }
    }

    public void LoadCredits() {
        if (credit == -1) {
            credit = tileEntity.getTileData().getFloat(key);
            System.out.println("Loaded: " + credit);
        }
        //credit = playerEntity.getPersistentData().getFloat("oeCredit");
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity playerIn) {
        SaveCredits();
        super.onContainerClosed(playerIn);
    }



    public void GenerateShopItemSlots() {
        shopSlots.clear();
        hasGenerated = true;

        /* this.outputHandler = new ItemStackHandler(6) {

            @Nonnull
            @Override // Prevents player from adding to slot
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (simulate) { // If this was called from us or not.


                    displayTotal // The amount of currency in the GUI
                    credit // The amount of actual currency
                    potential // the amount of currency which is visual-only
                    excessOutput // the amount of currency that wont fit in the output

                    if (displayTotal != credit + potential) { // Should we change rendered items?
                        for (int index = 0; index < outputHandler.getSlots(); index ++) {
                            this.stacks.get(index).setCount(0);
                        } // Clear all output slots
                        excessOutput = 0;

                        int remainder = credit + potential;
                        for ( int index = 0; remainder != 0; index ++) {
                            if (index > outputHandler.getSlots()-1) {
                                excessOutput = remainder;
                                break;
                            } // Break if out of bounds

                            if (remainder > currencyItem.getMaxStackSize()) { // Set Stack full
                                remainder -= currencyItem.getMaxStackSize();
                                this.stacks.set(index, new ItemStack(currencyItem, currencyItem.getMaxStackSize()));
                            } else {
                                this.stacks.set(index, new ItemStack(currencyItem, remainder));
                            } // Set item stacks
                        }
                    } // If not, we just leave things alone
                    displayTotal = GetTotal(); // GetTotal() should be the same as the starter value for remainder
                }
                return ItemStack.EMPTY; // Output value is excess. Because we are storing the excess, we never need to return a value.
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (simulate) { // if !true; we should go on.
                    ItemStack output = ItemStack.EMPTY; // Set default stack (What should go out to our hand)
                    Minecraft.getInstance().player.inventory.setItemStack(this.stacks.get(slot).copy());
                    credit -= this.stacks.get(slot).getCount();
                    System.out.println(credit);
                    outputHandler.insertItem(0, ItemStack.EMPTY, true); // Force update (After we have new stack for our hand)

                    return ItemStack.EMPTY; // Set hand value
                }
                return ItemStack.EMPTY;
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                tileEntity.getTileData().putInt("oeCredit", credit);
            }
        }

        for (int y = 0; y < 2; y++) { // for each vertical item slot
            for (int x = 0; x < 3; x++) { // for each horizontal item slot
                addSlot( new SlotItemHandler(outputHandler, index, 16+(18 * x), 64+(18 * y) ) );
                index ++;
            }
        };*/

        this.inputHandler = new ItemStackHandler(6) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!simulate) {
                    if (stack.isEmpty()) return ItemStack.EMPTY;

                    validateSlotIndex(slot);

                    ShopItem currentItem = CheckItem(stack.getItem());

                    if (currentItem == null || currentItem.sellValue == 0) return stack;

                    if (currentItem.sellValue > 0) {
                        credit += ((currentItem.sellValue * sell_value_multiplier) * stack.getCount());
                    } else {
                        credit += ((currentItem.cost * sell_value_multiplier) * stack.getCount());
                    }
                    this.stacks.get(0).setCount(0);
                }
                return ItemStack.EMPTY;
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }
        };


        addSlot( new SlotItemHandler(inputHandler, 0, 21, 49 ) );

        shopItemsHandler = new ItemStackHandler(30) {

            @Nonnull
            @Override // Prevents player from removing from slot
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!simulate) {
                    if (credit >= (GetShopItemFromSlot(slot).cost * amount) ) {
                        credit -= (GetShopItemFromSlot(slot).cost * amount);
                        return new ItemStack(this.getStackInSlot(slot).getItem(), amount);
                    }
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
        int xPos = 58;
        int yPos = 13;
        int index = 0;
        for (int y = 0; y < 5; y++) { // for each vertical item slot
            for (int x = 0; x < 6; x++) { // for each horizontal item slot
                if (_shopItems.size()-1 < index) {
                    break;
                }
                ShopItem currentItem = _shopItems.get(index);
                float itemCost = currentItem.cost;
                Item newItem = currentItem.item;

                Slot newSlot = addSlot( new SlotItemHandler(shopItemsHandler, index, xPos+(18 * x), yPos+(18 * y) ) ); // ShopSlot
                shopItemsHandler.insertItem(index, new ItemStack(newItem, 1), true);
                shopSlots.add(currentItem);
                index++;
            }
        }
    }

    //*
    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        int numberOfShopSlots = 30;
        int total = numberOfShopSlots;
        PlayerInventory playerinventory = player.inventory;
        if (clickTypeIn == ClickType.PICKUP_ALL) {
            if (slotId > total+1) {
                return super.slotClick(slotId, dragType, clickTypeIn, player);
            } else {
                clickTypeIn = ClickType.PICKUP;
            }
        }
        if (clickTypeIn == ClickType.QUICK_CRAFT || clickTypeIn == ClickType.THROW) {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
        if (slotId < 0) return ItemStack.EMPTY;
        if (slotId == 0 && clickTypeIn == ClickType.PICKUP && !playerinventory.getItemStack().isEmpty()) {
            ItemStack returned = inputHandler.insertItem(0, playerinventory.getItemStack().copy(), false);
            playerinventory.setItemStack(returned);
            return ItemStack.EMPTY;
        }
        if (slotId > 0 && slotId < total+1 && clickTypeIn == ClickType.PICKUP && playerinventory.getItemStack().isEmpty()) {

            ItemStack itemStack = shopItemsHandler.extractItem(slotId - 1, 1, false);
            if (itemStack != ItemStack.EMPTY) {
                playerinventory.setItemStack(itemStack);
            }
            return ItemStack.EMPTY;
        }
        if (clickTypeIn == ClickType.PICKUP && !playerinventory.getItemStack().isEmpty()
            && !this.inventorySlots.get(slotId).getStack().isEmpty()
            && this.inventorySlots.get(slotId).getStack().getItem() == playerinventory.getItemStack().getItem()
            && slotId > 0 && slotId < total+1) { // If clicking a shop slot with the same item in your hand
            int dragEvent = getDragEvent(dragType);
            if (dragEvent != 0) {
                this.resetDrag();
                return ItemStack.EMPTY;
            }
            ItemStack playerStack = playerinventory.getItemStack();

            ItemStack itemStack = shopItemsHandler.extractItem(slotId-1, playerStack.getCount(), false);
            if (itemStack != ItemStack.EMPTY) {
                playerinventory.setItemStack(new ItemStack(itemStack.getItem(), itemStack.getCount()+playerStack.getCount()));
            }
            return ItemStack.EMPTY;
        }
        if (clickTypeIn == ClickType.QUICK_MOVE) { // shift-clicking
            if (slotId > 0 && slotId < total+1) {
                ItemStack returned = ItemHandlerHelper.insertItemStacked(playerInventory, new ItemStack(shopItemsHandler.getStackInSlot(slotId-1).getItem(), shopItemsHandler.getStackInSlot(slotId-1).getMaxStackSize()), true);
                if (returned.isEmpty()) {
                    ItemStack itemStack = shopItemsHandler.extractItem(slotId - 1, shopItemsHandler.getStackInSlot(slotId-1).getMaxStackSize(), false);
                    ItemHandlerHelper.insertItemStacked(playerInventory, itemStack, false);
                }

                return ItemStack.EMPTY;
            } else if (slotId > total) { // Shift clicking from player inventory
                if (this.inventorySlots.get(0).getStack().isEmpty() && !this.inventorySlots.get(slotId).getStack().isEmpty() ||
                        (this.inventorySlots.get(0).getStack().getItem() == this.inventorySlots.get(slotId).getStack().getItem()
                                && this.inventorySlots.get(0).getStack().getCount() + this.inventorySlots.get(slotId).getStack().getCount() <= this.inventorySlots.get(slotId).getStack().getMaxStackSize())) {
                    ItemStack itemStack = this.inventorySlots.get(slotId).getStack().copy();
                    ItemStack returned = inputHandler.insertItem(0, itemStack, false);
                    this.inventorySlots.get(slotId).putStack(returned);
                }
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    private ShopItem GetShopItemFromSlot(int index) {
        return shopSlots.get(index);
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
