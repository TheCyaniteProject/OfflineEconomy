package com.kiee.offlineeconomy.blocks;

import com.kiee.offlineeconomy.OfflineEconomy;
import com.kiee.offlineeconomy.handlers.ShopItem;
import com.mojang.blaze3d.platform.GlStateManager;
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
        if (this.minecraft.player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
            ShopItem currentItem = null;
            for (ShopItem shopItem : ShopBlockContainer.shopItems) {
                if (shopItem.item == this.hoveredSlot.getStack().getItem()) {
                    currentItem = shopItem;
                }
            }
            if (currentItem == null) return;
            int baseValue = currentItem.cost;
            int value = (int)((((float) baseValue / (float) currentItem.count) * 0.75f) * this.hoveredSlot.getStack().getCount());
            this.renderTooltip(Arrays.asList(this.hoveredSlot.getStack().getItem().getName().getString() + " x " + this.hoveredSlot.getStack().getCount()
                    , "Buy: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + (this.hoveredSlot.getStack().getCount() / currentItem.count) * currentItem.cost,
                    "Sell: " + ShopBlockContainer.currencyItem.getItem().getName().getString() + " x " + value ), mouseX, mouseY);
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
