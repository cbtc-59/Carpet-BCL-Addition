package com.bcl.carpet.bcladdition;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;

public class BCLAdditionMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(BCLAdditionExtension.getInstance());
    }
}
