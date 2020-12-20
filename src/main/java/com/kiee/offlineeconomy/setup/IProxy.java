package com.kiee.offlineeconomy.setup;

import net.minecraft.world.World;

public interface IProxy {

    void init();

    World getClientProxy();
}
