package com.golfing8.kcommon.util;

/**
 * Represents the RGB color for text
 */
public class TextColor {
    private String hexCode;
    public int red, green, blue;

    public TextColor(String hexCode) {
        int hex = Integer.parseInt(hexCode, 16);
        red = (hex << 16) & 0xFF;
        green = (hex << 8) & 0xFF;
        blue = hex & 0xFF;
    }
}
