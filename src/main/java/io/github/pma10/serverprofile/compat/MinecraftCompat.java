package io.github.pma10.serverprofile.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftCompat {
    private MinecraftCompat() {
    }

    public static Screen currentScreen(Minecraft client) {
        //? if >=26.2 {
        return client.gui.screen();
        //?} else
        /*return client.screen;*/
    }

    public static void setScreen(Screen screen) {
        Minecraft client = Minecraft.getInstance();

        //? if >=26.2 {
        client.gui.setScreen(screen);
        //?} else
        /*client.setScreen(screen);*/
    }
}
