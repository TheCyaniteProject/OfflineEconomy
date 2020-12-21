package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.OfflineEconomy;
import com.kiee.offlineeconomy.handlers.ShopItem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Random;

import static com.kiee.offlineeconomy.blocks.BlockList.SHOPBLOCK_CONTAINER;

public class ShopBlockContainer extends Container {

    public static Item currencyItem = Items.EMERALD;
    public static ArrayList<ShopItem> shopItems = new ArrayList<>();
    private static ArrayList<ShopItem> _shopItems = new ArrayList<>();

    private TileEntity tileEntity;
    private PlayerEntity playerEntity;
    private IItemHandler playerInventory;

    private IItemHandler inputHandler;
    private IItemHandler outputHandler;

    public ShopBlockContainer(int id, World world, BlockPos position, PlayerInventory playerInventory, PlayerEntity player) {
        super(SHOPBLOCK_CONTAINER, id);

        // TODO: regen each day
        generateList();

        tileEntity = world.getTileEntity(position);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(_in -> {

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
            Slot slot = addSlot(new SlotItemHandler(outputHandler, 0, 15, 61 ) );

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
                    _in.insertItem(slot, stack, simulate);
                    return super.insertItem(slot, stack, simulate);
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    _in.extractItem(slot, amount, simulate);
                    return super.extractItem(slot, amount, simulate);
                }
            };
            //inputHandler.insertItem(0, _in.getStackInSlot(0), false);
            addSlot(new SlotItemHandler(inputHandler, 0, 15, 36 )); // Input

            int index = 0;
            for (int y = 0; y < 5; y++) { // for each vertical item slot
                for (int x = 0; x < 6; x++) { // for each horizontal item slot
                    if (_shopItems.size() < index+1) {
                        break;
                    }
                    int itemCount = _shopItems.get(index).count;
                    int itemCost = _shopItems.get(index).cost;
                    Item newItem = _shopItems.get(index).item;

                    IItemHandler shopItemsHandler = new ItemStackHandler(30) {

                        @Nonnull
                        @Override // Prevents player from removing from slot
                        public ItemStack extractItem(int slot, int amount, boolean simulate) {
                            //System.out.println(newItem.getTranslationKey());
                            if (inputHandler.getStackInSlot(0).getCount() >= itemCost && inputHandler.getStackInSlot(0).getItem() == currencyItem) { // if requirements are met
                                inputHandler.extractItem(0, itemCost, simulate);
                                //ItemStack superExtract = super.extractItem(slot, amount, simulate);
                                return new ItemStack(newItem, itemCount);
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
                    addSlot(new SlotItemHandler(shopItemsHandler, index, 50+(18 * x), 13+(18 * y) )); // ShopSlot
                    shopItemsHandler.insertItem(index, new ItemStack(newItem, itemCount), true);
                    //System.out.println("New ShopItem: " + newItem.getName().getString() + " x" + itemCount +" cost: " + itemCost);
                    index++;
                }
            }
        });

        // The position of the top-left corner of the player inventory
        layoutPlayerInventorySlots(8, 129);
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
        _shopItems.clear();
        Random generator = new Random();
        if (shopItems.size() > 30) {
            for (int index = 0; index < 30; index++ ) {
                int randomIndex = generator.nextInt(shopItems.size()); // random item
                if (!_shopItems.contains(shopItems.get(randomIndex))) {
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
