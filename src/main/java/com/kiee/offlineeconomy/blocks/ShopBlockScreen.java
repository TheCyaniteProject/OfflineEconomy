package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.OfflineEconomy;
import com.kiee.offlineeconomy.handlers.ShopItem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.*;


public class ShopBlockScreen extends ContainerScreen<ShopBlockContainer> {

    private ResourceLocation GUI = new ResourceLocation(OfflineEconomy.MOD_ID, "textures/gui/shop_screen.png");
    private ShopBlockContainer screenContainer;

    public ShopBlockScreen(ShopBlockContainer screenContainer, PlayerInventory inv, ITextComponent windowId) {
        super(screenContainer, inv, windowId);
        this.screenContainer = screenContainer;
        this.xSize = 176;
        this.ySize = 211;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString("$"+ screenContainer.credit, 7, 3, 16777215);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 7, 119.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (Minecraft.getInstance().player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {

            String lineOne = this.hoveredSlot.getStack().getItem().getName().getString();
            if (this.hoveredSlot.getStack().getCount() > 1)
                lineOne = lineOne +" x "+ this.hoveredSlot.getStack().getCount();
            String lineTwo = "";

            ShopItem currentItem = null;
            for (ShopItem shopItem : ShopBlockContainer.shopItems) {
                if (shopItem.item == this.hoveredSlot.getStack().getItem()) {
                    currentItem = shopItem;
                }
            }

            if (currentItem != null) {
                if (currentItem.sellValue > 0) {
                    lineTwo = "Buy: "+ currentItem.cost + " | Sell: " + currentItem.sellValue;
                } else if (currentItem.sellValue == 0) {
                    lineTwo = "Buy: "+ currentItem.cost;
                } else {
                    lineTwo = "Buy: "+ currentItem.cost + " | Sell: " + (currentItem.cost * ShopBlockContainer.sell_value_multiplier);
                }
                this.renderTooltip(Arrays.asList(lineOne, lineTwo), mouseX, mouseY);
            }
        }
    }
}
