package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.OfflineEconomy;
import com.kiee.offlineeconomy.handlers.ShopItem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.SlotItemHandler;

import java.util.*;


public class ShopBlockScreen extends ContainerScreen<ShopBlockContainer> {

    private ResourceLocation GUI = new ResourceLocation(OfflineEconomy.MOD_ID, "textures/gui/shop_screen.png");


    public ShopBlockScreen(ShopBlockContainer screenContainer, PlayerInventory inv, ITextComponent windowId) {
        super(screenContainer, inv, windowId);
        this.ySize = 211;
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (Minecraft.getInstance().player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {

            String lineOne = this.hoveredSlot.getStack().getItem().getName().getString() + " x " + this.hoveredSlot.getStack().getCount();
            String lineTwo = "";
            String lineThree = "";

            ShopItem currentItem = null;
            for (ShopItem shopItem : ShopBlockContainer.shopItems) {
                if (shopItem.item == this.hoveredSlot.getStack().getItem()) {
                    currentItem = shopItem;
                }
            }

            if (this.hoveredSlot.getStack().getItem() == ShopBlockContainer.currencyItem) { // Item is currency
                this.renderTooltip(Arrays.asList(lineOne, "Currency!"), mouseX, mouseY);
                return;
            }

            if (currentItem == null) {
                lineTwo = "Not For Sale";
                this.renderTooltip(Arrays.asList(lineOne, lineTwo, lineThree), mouseX, mouseY);
                return;
            }

            int value = ((int)((((float) currentItem.cost / (float) currentItem.count) * 0.75f) * this.hoveredSlot.getStack().getCount()));
            lineTwo = "Buy: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + (this.hoveredSlot.getStack().getCount() / currentItem.count) * currentItem.cost;

            if (currentItem.cost <1) { // Sell only
                value = (int)(((float) currentItem.sellValue / (float) currentItem.count) * this.hoveredSlot.getStack().getCount());
                lineThree = "Sell: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + value;
                this.renderTooltip(Arrays.asList(lineOne, lineThree), mouseX, mouseY);
                return;
            }
            if (currentItem.sellValue > 0) { // Fixed sell price
                value = (int)(((float) currentItem.sellValue / (float) currentItem.count) * this.hoveredSlot.getStack().getCount());
                lineThree = "Sell: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + value;
            } else if (currentItem.sellValue == 0) { // Fixed sale price of 0
                lineThree = "";
            } else if (value < 1) {
                lineThree = "Sell: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x <1";
            } else { // Standard lineThree
                lineThree = "Sell: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + value;
            }
            if (lineTwo != "" && lineThree != "") {
                this.renderTooltip(Arrays.asList(lineOne, lineTwo, lineThree), mouseX, mouseY);
            } else if (lineTwo != "") {
                this.renderTooltip(Arrays.asList(lineOne, lineTwo), mouseX, mouseY);
            } else if (lineThree != "") {
                this.renderTooltip(Arrays.asList(lineOne, lineThree), mouseX, mouseY);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //this.font.drawString("Marketplace", 8.0f, 5.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, 119.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }
}
