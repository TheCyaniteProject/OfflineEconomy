package com.kiee.offlineeconomy.commands;

import com.kiee.offlineeconomy.OfflineEconomy;
import com.kiee.offlineeconomy.blocks.ShopBlockContainer;
import com.kiee.offlineeconomy.handlers.Parser;
import com.kiee.offlineeconomy.handlers.ShopItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        LiteralCommandNode<CommandSource> offlineEconomy = dispatcher.register(
            Commands.literal("offlineEconomy")
                .requires(source -> source.hasPermissionLevel(4))
                .then(Commands.literal("set")
                    .then(Commands.argument("item_id", StringArgumentType.string())
                        .then(Commands.argument("buy_price", FloatArgumentType.floatArg())
                            .then(Commands.argument("sell_price", FloatArgumentType.floatArg())
                                .executes(context -> addItem(context, StringArgumentType.getString(context, "item_id"), FloatArgumentType.getFloat(context, "buy_price"), FloatArgumentType.getFloat(context, "sell_price")))
                            )
                            .executes(context -> addItem(context, StringArgumentType.getString(context, "item_id"), FloatArgumentType.getFloat(context, "buy_price")))
                        )
                    )
                )
                .then(Commands.literal("reload").executes(ModCommands::reloadList))
                .then(Commands.literal("reset").executes(ModCommands::resetShop))
                .then(Commands.literal("remove")
                    .then(Commands.argument("item_id", StringArgumentType.string())
                        .executes(context -> removeItem(context, StringArgumentType.getString(context, "item_id")))
                    )
                )
                .then(Commands.literal("get")
                    .then(Commands.argument("item_id", StringArgumentType.string())
                            .executes(context -> getItem(context, StringArgumentType.getString(context, "item_id")))
                    )
                )
        );
        dispatcher.register(Commands.literal("oemod").redirect(offlineEconomy));

    }

    public static int reloadList(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new StringTextComponent("Reloading Config.."), false);
        OfflineEconomy.parser.init();
        resetShop(context);
        return 0;
    }
    public static int resetShop(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new StringTextComponent("Resetting Shop Items.."), false);
        ShopBlockContainer.hasGenerated = false;
        return 0;
    }

    public static int getItem(CommandContext<CommandSource> context, String itemName) throws CommandSyntaxException {
        Item newItem;
        try {
            newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        } catch (ResourceLocationException e) {
            context.getSource().sendFeedback(new StringTextComponent(itemName + " is not a valid item."), false);
            return 0;
        }
        if (ShopBlockContainer.CheckItem(newItem) != null) {
            ShopItem shopItem = ShopBlockContainer.CheckItem(newItem);
            context.getSource().sendFeedback(new StringTextComponent(itemName +" Cost: "+ shopItem.cost +" Value: "+ shopItem.sellValue ), false);
        } else {
            context.getSource().sendFeedback(new StringTextComponent(itemName +" is not a valid item."), false);
        }
        return 0;
    }

    public static int addItem(CommandContext<CommandSource> context, String itemName, float buyPrice) throws CommandSyntaxException {
        addItem(context, itemName, buyPrice, -1);
        return 0;
    }
    public static int addItem(CommandContext<CommandSource> context, String itemName, float buyPrice, float sellPrice) throws CommandSyntaxException {
        Item newItem;
        try {
            newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        } catch (ResourceLocationException e) {
            context.getSource().sendFeedback(new StringTextComponent(itemName +" is not a valid item."), false);
            return 0;
        }
        context.getSource().sendFeedback(new StringTextComponent(itemName +" Cost: "+ buyPrice +" Value: "+ sellPrice), false);
        Parser.set(itemName, buyPrice, sellPrice);
        return 0;
    }

    public static int removeItem(CommandContext<CommandSource> context, String itemName) throws CommandSyntaxException {
        Parser.remove(itemName);
        context.getSource().sendFeedback(new StringTextComponent(itemName + " was removed."), false);
        return 0;
    }
}