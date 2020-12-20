package com.kiee.offlineeconomy.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {

    @Override
    public void init() {

    }

    @Override
    public World getClientProxy() {
        return Minecraft.getInstance().world;
    }
}
