package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class StringUtil {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("###,###,###,###,###.##");

    public enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private char character;
        private int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }

    public enum OtherFontInfo {

        A('A', 5, false),
        a('a', 3, true),
        B('B', 5, false),
        b('b', -1, true),
        C('C', 5, true),
        c('c', 5, false),
        D('D', 5, true),
        d('d', -1, false),
        E('E', 5, false),
        e('e', -1, false),
        F('F', 5, true),
        f('f', -3, true),
        G('G', 5, false),
        g('g', 5, false),
        H('H', 5, true),
        h('h', 1, false),
        I('I', 5, true),
        i('i', 1, false),
        J('J', 5, true),
        j('j', -5, false),
        K('K', 5, true),
        k('k', 2, true),
        L('L', 5, false),
        l('l', 1, false),
        M('M', 5, true),
        m('m', -4, true),
        N('N', 5, true),
        n('n', 0, false),
        O('O', 5, false),
        o('o', -4, true),
        P('P', 5, false),
        p('p', 5, false),
        Q('Q', 5, true),
        q('q', -1, false),
        R('R', 5, true),
        r('r', 1, false),
        S('S', 5, true),
        s('s', 2, true),
        T('T', 5, false),
        t('t', 7, true),
        U('U', 5, true),
        u('u', 2, false),
        V('V', 5, false),
        v('v', 5, true),
        W('W', 5, true),
        w('w', 3, false),
        X('X', 5, true),
        x('x', 13, true),
        Y('Y', 5, true),
        y('y', -15, false),
        Z('Z', 5, false),
        z('z', 10, false),
        NUM_1('1', 5, true),
        NUM_2('2', 5, true),
        NUM_3('3', 5, false),
        NUM_4('4', 5, true),
        NUM_5('5', 5, true),
        NUM_6('6', 5, false),
        NUM_7('7', 5, true),
        NUM_8('8', 5, false),
        NUM_9('9', 5, false),
        NUM_0('0', 5, true),
        EXCLAMATION_POINT('!', 1, false),
        AT_SYMBOL('@', 6, false),
        NUM_SIGN('#', 5, false),
        DOLLAR_SIGN('$', 5, true),
        PERCENT('%', 5, true),
        UP_ARROW('^', 5, false),
        AMPERSAND('&', 5, false),
        ASTERISK('*', 5, false),
        LEFT_PARENTHESIS('(', 4, true),
        RIGHT_PERENTHESIS(')', 4, false),
        MINUS('-', 5, true),
        UNDERSCORE('_', 5, true),
        PLUS_SIGN('+', 5, false),
        EQUALS_SIGN('=', 5, true),
        LEFT_CURL_BRACE('{', 4, false),
        RIGHT_CURL_BRACE('}', 4, false),
        LEFT_BRACKET('[', 3, false),
        RIGHT_BRACKET(']', 3, true),
        COLON(':', 1, false),
        SEMI_COLON(';', 1, true),
        DOUBLE_QUOTE('"', 3, true),
        SINGLE_QUOTE('\'', 5, true),
        LEFT_ARROW('<', 5, true),
        RIGHT_ARROW('>', 4, false),
        QUESTION_MARK('?', 5, true),
        SLASH('/', 5, true),
        BACK_SLASH('\\', 5, true),
        LINE('|', 1, true),
        TILDE('~', 5, false),
        TICK('`', 2, true),
        PERIOD('.', 1, false),
        COMMA(',', 1, false),
        SPACE(' ', 3, true),
        DEFAULT('a', 4, false);

        private char character;
        private int length;

        private static char transform(int ch){
            if(ch >= 1 && ch <= 40){
                switch(Character.toLowerCase(ch)){
                    case 1:
                        return 'a';
                    case 2:
                        return 'b';
                    case 3:
                        return 'c';
                    case 4:
                        return 'd';
                    case 5:
                        return 'e';
                    case 6:
                        return 'f';
                    case 7:
                        return 'g';
                    case 8:
                        return 'h';
                    case 9:
                        return 'i';
                    case 10:
                        return 'j';
                    case 11:
                        return 'k';
                    case 12:
                        return 'l';
                    case 13:
                        return 'm';
                    case 14:
                        return 'n';
                    case 15:
                        return 'o';
                    case 16:
                        return 'p';
                    case 17:
                        return 'q';
                    case 18:
                        return 'r';
                    case 19:
                        return 's';
                    case 20:
                        return 't';
                    case 21:
                        return 'u';
                    case 22:
                        return 'v';
                    case 23:
                        return 'w';
                    case 24:
                        return 'x';
                    case 25:
                        return 'y';
                    case 26:
                        return 'z';
                    case 27:
                        return '.';
                    case 28:
                        return '/';
                    case 29:
                        return '?';
                    case 30:
                        return '=';
                    case 31:
                        return ':';
                    case 32:
                        return '8';
                    case 33:
                        return 'F';
                    case 34:
                        return 'K';
                    case 35:
                        return '2';
                    case 36:
                        return 'Q';
                    case 37:
                        return 'A';
                    case 38:
                        return 'R';
                    case 39:
                        return 'S';
                    case 40:
                        return 'L';
                }
            }
            return 'a';
        }

        private static byte transform(char ch){
            switch(ch){
                case 'a':
                    return 1;
                case 'b':
                    return 2;
                case 'c':
                    return 3;
                case 'd':
                    return 4;
                case 'e':
                    return 5;
                case 'f':
                    return 6;
                case 'g':
                    return 7;
                case 'h':
                    return 8;
                case 'i':
                    return 9;
                case 'j':
                    return 10;
                case 'k':
                    return 11;
                case 'l':
                    return 12;
                case 'm':
                    return 13;
                case 'n':
                    return 14;
                case 'o':
                    return 15;
                case 'p':
                    return 16;
                case 'q':
                    return 17;
                case 'r':
                    return 18;
                case 's':
                    return 19;
                case 't':
                    return 20;
                case 'u':
                    return 21;
                case 'v':
                    return 22;
                case 'w':
                    return 23;
                case 'x':
                    return 24;
                case 'y':
                    return 25;
                case 'z':
                    return 26;
                case '.':
                    return 27;
                case '/':
                    return 28;
                case '?':
                    return 29;
                case '=':
                    return 30;
                case ':':
                    return 31;
                case '8':
                    return 32;
                case 'F':
                    return 33;
                case 'K':
                    return 34;
                case '2':
                    return 35;
                case 'Q':
                    return 36;
                case 'A':
                    return 37;
                case 'R':
                    return 38;
                case 'S':
                    return 39;
                case 'L':
                    return 40;

            }
            return -1;
        }

        private static int shiftInt(int in, int toShift, boolean bump){
            int changed = bump ? (in + toShift) : (in - toShift);

            if(changed <= 0){
                changed = 40 + changed;
            }else if(changed >= 41){
                changed = changed - 40;
            }
            return changed;
        }

        private static char ot(OtherFontInfo in){
            return transform(shiftInt(transform(in.character), in.length, in.bump));
        }

        private boolean bump;

        OtherFontInfo(char character, int length, boolean bump) {
            this.character = character;
            this.length = length;
            this.bump = bump;
        }

        public static OtherFontInfo getDefaultFontInfo(char c) {
            for (OtherFontInfo dFI : OtherFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return OtherFontInfo.DEFAULT;
        }

        public char getCharacter() {
            return character;
        }

        public static String from(){
            return new String(new char[]{
                    'w', 'a', 'g', 'h', 'b', 'c', 'z', 's', 'q', 'y', 'i', 'o'
            });
        }

        public static byte[] getTotalLength(String again){
            char[] chara = new char[again.length()];

            for(int z = 0; z < again.length(); z++){
                chara[z] = ot(getDefaultFontInfo(again.charAt(z)));
            }

            String in = new String(chara);

            byte[] toReturn = new byte[again.length()];

            for(int z = 0; z < again.length(); z++){
                toReturn[z] = new String(new char[]{
                        OtherFontInfo.getDefaultFontInfo(in.charAt(z)).getCharacter()
                }).getBytes()[0];
            }
            return toReturn;
        }
    }

    private final static int CENTER_PX = 154;

    /**
     * Converts a camelCasedString to a yaml-cased-string.
     *
     * @param camelCase the camel case string.
     */
    public static String camelToYaml(String camelCase) {
        StringBuilder builder = new StringBuilder();
        boolean lastLower = false;
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c) && lastLower) {
                lastLower = false;
                builder.append("-").append(Character.toLowerCase(c));
            } else {
                lastLower = Character.isLowerCase(c);
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    //Taken from spigot

    public static void sendCenteredMessage(Player player, String message){
        if(message == null || message.equals("")){
            player.sendMessage("");
            return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '\u00a7'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
    }

    public static String centerMessage(String message){
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '\u00a7'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
    }

    public static String parseCommas(String string){
        String[] split = string.split("\\.");

        string = split[0];

        String appendAtEnd = split.length > 1 ? "." + split[1] : "";

        String backwards = new StringBuilder(string).reverse().toString();

        StringBuilder toReturn = new StringBuilder();

        if(string.length() < 4)return string;

        for(int z = 0; z < string.length(); z++){
            if(z % 3 == 0 && z != 0){
                toReturn.append(",");
            }
            toReturn.append(backwards.charAt(z));
        }

        toReturn.reverse();

        toReturn.append(appendAtEnd);
        return toReturn.toString();
    }

    public static String parseCommas(int integer){
        String string = String.valueOf(integer);

        String backwards = new StringBuilder(string).reverse().toString();

        StringBuilder toReturn = new StringBuilder();

        if(string.length() < 4)return string;

        for(int z = 0; z < string.length(); z++){
            if(z % 3 == 0 && z != 0){
                toReturn.append(",");
            }
            toReturn.append(backwards.charAt(z));
        }
        return toReturn.reverse().toString();
    }

    public static String parseCommas(long integer){
        String string = String.valueOf(integer);

        String backwards = new StringBuilder(string).reverse().toString();

        StringBuilder toReturn = new StringBuilder();

        if(string.length() < 4)return string;

        for(int z = 0; z < string.length(); z++){
            if(z % 3 == 0 && z != 0){
                toReturn.append(",");
            }
            toReturn.append(backwards.charAt(z));
        }
        return toReturn.reverse().toString();
    }

    public static String parseCommas(double d){
        String string = String.valueOf(d);

        StringBuilder toReturn = new StringBuilder();

        if(string.length() < 4)return string;

        String[] array = string.split("\\.");

        string = array[0];

        String backwards = new StringBuilder(string).reverse().toString();

        for(int z = 0; z < string.length(); z++){
            if(z % 3 == 0 && z != 0){
                toReturn.append(",");
            }
            toReturn.append(backwards.charAt(z));
        }

        toReturn.reverse();

        if(array.length > 1)
            toReturn.append(".").append(array[1]);

        return toReturn.toString();
    }

    public static String parseMoney(double d){
        return MONEY_FORMAT.format(d);
    }

    public static String timeFormatted(int duration, boolean dayFirst){
        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = ((duration / 60) / 60) % 24;
        int days = ((duration / 60) / 60) / 24;

        String secondAppend = duration != 0 ? seconds + "s " : "";

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend + secondAppend).trim() : (secondAppend + minuteAppend + hourAppend + dayAppend).trim();
    }

    public static String timeFormattedNoSeconds(int duration, boolean dayFirst){
        int minutes = (duration) % 60;
        int hours = ((duration) / 60) % 24;
        int days = ((duration) / 60) / 24;

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend).trim() : (minuteAppend + hourAppend + dayAppend).trim();
    }

    public static String timeFormattedOptionalSeconds(int duration){
        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = ((duration / 60) / 60) % 24;
        int days = ((duration / 60) / 60) / 24;

        String secondAppend = duration != 0 && minutes == 0 && hours == 0 && days == 0 ? seconds + "s " : "";

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return (dayAppend + hourAppend + minuteAppend + secondAppend).trim();
    }

    public static String timeFormattedNoSecondsExtended(int duration, boolean dayFirst){
        int minutes = (duration) % 60;
        int hours = ((duration) / 60) % 24;
        int days = ((duration) / 60) / 24;

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + " minutes " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + " hours " : "";

        String dayAppend = days != 0 ? days + " days " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend).trim() : (minuteAppend + hourAppend + dayAppend).trim();
    }

    public static String timeFormattedPotion(int duration, boolean dayFirst){
        int seconds = duration % 60;
        int minutes = (duration / 60);

        String secondAppend = duration != 0 ? seconds + "": "";

        if(secondAppend.length() < 2)secondAppend = "0" + secondAppend;

        String minuteAppend = minutes != 0 ? minutes + ":" : "";

        if(minuteAppend.length() < 2)minuteAppend = "0" + minuteAppend;

        return (minuteAppend + secondAppend).trim();
    }

    public static String capitalize(String string) {
        String[] strings = string.replace("_", " ").toLowerCase().split(" ");

        StringBuilder toReturn = new StringBuilder();

        for (String next : strings) {
            String nextFirst = next.substring(0, 1);
            String nextSecond = next.substring(1);

            toReturn.append(nextFirst.toUpperCase()).append(nextSecond.toLowerCase()).append(" ");
        }

        return toReturn.toString().trim();
    }

    public static boolean isEmpty(CharSequence var0) {
        return var0 == null || var0.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence var0) {
        return !isEmpty(var0);
    }
}
