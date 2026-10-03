package io.github.pma10.serverprofile.compat;

import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else
/*import net.minecraft.client.gui.GuiGraphics;*/
import net.minecraft.network.chat.Component;

public final class ScreenGraphics {
    private ScreenGraphics() {
    }

    public static void centered(
        //? if >=26.1 {
        GuiGraphicsExtractor graphics,
        //?} else
        /*GuiGraphics graphics,*/
        Font font,
        Component text,
        int x,
        int y,
        int color
    ) {
        //? if >=26.1 {
        graphics.centeredText(font, text, x, y, color);
        //?} else
        /*graphics.drawCenteredString(font, text, x, y, color);*/
    }

    public static void text(
        //? if >=26.1 {
        GuiGraphicsExtractor graphics,
        //?} else
        /*GuiGraphics graphics,*/
        Font font,
        Component text,
        int x,
        int y,
        int color
    ) {
        //? if >=26.1 {
        graphics.text(font, text, x, y, color);
        //?} else
        /*graphics.drawString(font, text, x, y, color);*/
    }
}
