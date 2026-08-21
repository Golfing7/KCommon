package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RGBUtilsTest {

    @Test
    @DisplayName("Translates ampersand legacy color codes")
    void testLegacyColorCodes() {
        String result = RGBUtils.INSTANCE.hexColor("&cRed &aGreen");
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&cRed &aGreen"), result);
    }

    @Test
    @DisplayName("Converts &#RRGGBB hex codes into the bukkit hex color escape sequence")
    void testHexColorCode() {
        String result = RGBUtils.INSTANCE.hexColor("&#FF0000Text");
        StringBuilder expectedPrefix = new StringBuilder().append(ChatColor.COLOR_CHAR).append('x');
        for (char c : "FF0000".toCharArray()) {
            expectedPrefix.append(ChatColor.COLOR_CHAR).append(c);
        }
        assertEquals(expectedPrefix + "Text", result);
    }

    @Test
    @DisplayName("Converts the {&#RRGGBB} bracketed hex format to a color code")
    void testFixFormat2() {
        StringBuilder expectedPrefix = new StringBuilder().append(ChatColor.COLOR_CHAR).append('x');
        for (char c : "00FF00".toCharArray()) {
            expectedPrefix.append(ChatColor.COLOR_CHAR).append(c);
        }
        assertEquals(expectedPrefix + "Text", RGBUtils.INSTANCE.hexColor("{&#00FF00}Text"));
    }

    @Test
    @DisplayName("Converts the &x&R&R&G&G&B&B split hex format to a color code")
    void testFixFormat3() {
        StringBuilder expectedPrefix = new StringBuilder().append(ChatColor.COLOR_CHAR).append('x');
        for (char c : "00FF00".toCharArray()) {
            expectedPrefix.append(ChatColor.COLOR_CHAR).append(c);
        }
        assertEquals(expectedPrefix + "Text", RGBUtils.INSTANCE.hexColor("&x&0&0&F&F&0&0Text"));
    }

    @Test
    @DisplayName("Leaves text with no color codes unchanged")
    void testNoColorCodes() {
        assertEquals("Plain text", RGBUtils.INSTANCE.hexColor("Plain text"));
    }
}
