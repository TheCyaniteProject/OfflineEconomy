package com.kiee.offlineeconomy.blocks;


import com.kiee.offlineeconomy.OfflineEconomy;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ShopBlockScreen extends ContainerScreen<ShopBlockContainer> {

    private ResourceLocation GUI = new ResourceLocation(OfflineEconomy.MOD_ID, "textures/gui/shop_screen.png");

    //private final ShopBlockScreen.TradeButton[] tradeButtons = new ShopBlockScreen.TradeButton[7];
    //private int selectedMerchantRecipe;

    public ShopBlockScreen(ShopBlockContainer screenContainer, PlayerInventory inv, ITextComponent windowId) {
        super(screenContainer, inv, windowId);
        this.ySize = 171;
    }

    /*protected void init() {
        super.init();
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        int k = j + 16 + 2;

        for(int l = 0; l < 7; ++l) {
            this.tradeButtons[l] = this.addButton(new ShopBlockScreen.TradeButton(i + 5, k, l, (p_214132_1_) -> {
                if (p_214132_1_ instanceof ShopBlockScreen.TradeButton) {
                    //this.selectedMerchantRecipe = ((ShopBlockScreen.TradeButton)p_214132_1_).func_212937_a() + this.totalMOffers;
                    //this.func_195391_j();
                }

            }));
            k += 20;
        }

    }*/

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString("Marketplace", 8.0f, 4.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, 74.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }

    /*@OnlyIn(Dist.CLIENT)
    class TradeButton extends Button {
        final int index; // current mOffer

        public TradeButton(int width, int height, int index, Button.IPressable button) {
            super(width, height, 89, 20, "", button);
            this.index = index;
            this.visible = false;
        }
    }*/
}
