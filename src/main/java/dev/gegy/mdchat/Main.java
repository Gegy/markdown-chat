package dev.gegy.mdchat;// Created 2022-09-06T16:54:32

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;

/**
 * @author KJP12
 * @since ${version}
 **/
public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerMessageDecoratorEvent.EVENT.register(TextStyler.INSTANCE);
    }
}
