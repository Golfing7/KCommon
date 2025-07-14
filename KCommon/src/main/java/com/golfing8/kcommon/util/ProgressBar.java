package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;

public final class ProgressBar {
    private static final String COLOR_GREEN = ChatColor.DARK_GREEN.toString();
    private static final String COLOR_LIME = ChatColor.GREEN.toString();
    private static final String COLOR_GRAY = ChatColor.GRAY.toString();

    public static final char BOX_UNICODE = '\u2588';

    private ProgressBar() {
    }

    public static String getProgressBar(double progress) {
        return getProgressBar(progress, 100.0D);
    }

    public static String getProgressBar(double progress, double maxProgress) {
        return getProgressBar(progress, maxProgress, 10);
    }

    public static String getProgressBar(double progress, double maxProgress, int length) {
        return getProgressBar(progress, maxProgress, BOX_UNICODE, length);
    }

    /**
     * Returns a progress bar, colored, of "length" length made up of "barChar" characters.
     *
     * @param progress
     * @param maxProgress
     * @param barChar
     * @param length
     * @return
     */
    public static String getProgressBar(double progress, double maxProgress, char barChar, int length) {
        return getProgressBar(progress, maxProgress, barChar, length, COLOR_GREEN, COLOR_LIME, COLOR_GRAY);
    }

    public static String getProgressBar(double progress, double maxProgress, char barChar, int length, String filledColor, String semiFilledColor, String emptyColor) {
        double percentage = (progress / maxProgress) * 100.0D;

        StringBuilder progressBar = new StringBuilder();

        double percentPerChar = (1D / length) * 100.0D;

        for (int i = 0; i < length; i++) {
            double progressPassBar = percentPerChar * (i + 1);

            if (percentage >= progressPassBar)
                progressBar.append(filledColor);
            else if (percentage + percentPerChar >= progressPassBar)
                progressBar.append(semiFilledColor);
            else
                progressBar.append(emptyColor);

            progressBar.append(barChar);
        }
        return progressBar.toString();
    }
}
