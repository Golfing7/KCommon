package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RGBUtils {
    public static final RGBUtils INSTANCE = new RGBUtils();

    private final Pattern hex = Pattern.compile("#[0-9a-fA-F]{6}");
    private final Pattern fix2 = Pattern.compile("\\{#[0-9a-fA-F]{6}\\}");
    private final Pattern fix3 = Pattern.compile("\\&x[\\&0-9a-fA-F]{12}");
    private final Pattern gradient1 = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
    private final Pattern gradient2 = Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}");

    private String toChatColor(String hexCode){
        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        char[] var3 = hexCode.substring(1).toCharArray();

        for (char c : var3) {
            magic.append(ChatColor.COLOR_CHAR).append(c);
        }
        return magic.toString();
    }

    private String toHexString(int red, int green, int blue){
        StringBuilder hexString = new StringBuilder(Integer.toHexString((red << 16) + (green << 8) + blue));
        while(hexString.length() < 6) hexString.insert(0, "0");
        return hexString.toString();
    }

    private String applyFormats(String textInput){
        String text = textInput;
        text = fixFormat1(text);
        text = fixFormat2(text);
        text = fixFormat3(text);
        text = setGradient1(text);
        text = setGradient2(text);
        return text;
    }

    public String hexColor(String in){
        String text = applyFormats(in);
        Matcher m = hex.matcher(text);
        while(m.find()){
            String hexCode = m.group();
            text = text.replace(hexCode, toChatColor(hexCode));
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String fixFormat1(String text){
        return text.replace("&#", "#");
    }

    private String fixFormat2(String input){
        String text = input;
        Matcher m = fix2.matcher(text);
        while(m.find()){
            String hexcode = m.group();
            String fixed = hexcode.substring(2, 8);
            text = text.replace(hexcode, "#" + fixed);
        }
        return text;
    }

    private String fixFormat3(String input){
        String text = input;
        text = text.replace('\u00a7', '&');
        Matcher m = fix3.matcher(text);
        while(m.find()){
            String hexcode = m.group();
            String fixed = new String(new char[] {
                    hexcode.charAt(3),
                    hexcode.charAt(5),
                    hexcode.charAt(7),
                    hexcode.charAt(9),
                    hexcode.charAt(11),
                    hexcode.charAt(13)
            });
            text = text.replace(hexcode, "#" + fixed);
        }
        return text;
    }

    private String setGradient1(String input){
        String text = input;
        Matcher m = gradient1.matcher(text);
        while(m.find()){
            String format = m.group();
            TextColor start = new TextColor(format.substring(2, 8));
            String message = format.substring(9, format.length() - 10);
            TextColor end = new TextColor(format.substring(format.length() - 7, format.length() - 1));
            String applied = asGradient(start, message, end);
            text = text.replace(format, applied);
        }
        return text;
    }

    private String setGradient2(String input){
        String text = input;
        Matcher m = gradient2.matcher(text);
        while(m.find()){
            String format = m.group();
            TextColor start = new TextColor(format.substring(2, 8));
            String message = format.substring(10, format.length() - 10);
            TextColor end = new TextColor(format.substring(format.length() - 8, format.length() - 2));
            String applied = asGradient(start, message, end);
            text = text.replace(format, applied);
        }
        return text;
    }

    private String asGradient(TextColor start, String text, TextColor end){
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        for(int i = 0; i < length; i++){
            int red = (int) (start.red + ((float) (end.red - start.red)) / (length - 1) * i);
            int green = (int) (start.green + ((float) (end.green - start.green)) / (length - 1) * i);
            int blue = (int) (start.blue + ((float) (end.blue - start.blue)) / (length - 1) * i);
            sb.append("#").append(toHexString(red, green, blue)).append(text.charAt(i));
        }
        return sb.toString();
    }
}
