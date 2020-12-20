package com.kiee.offlineeconomy.setup;

import net.minecraft.world.World;

public class ServerProxy implements IProxy {

    @Override
    public void init() {

    }

    @Override
    public World getClientProxy() {
        throw new IllegalStateException("Only run this on the client!");
    }
}
