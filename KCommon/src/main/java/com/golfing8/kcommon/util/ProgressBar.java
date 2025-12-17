package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

/**
 * Utility class for generating progress bar strings
 */
@UtilityClass
public final class ProgressBar {
    private static final String COLOR_GREEN = ChatColor.DARK_GREEN.toString();
    private static final String COLOR_LIME = ChatColor.GREEN.toString();
    private static final String COLOR_GRAY = ChatColor.GRAY.toString();

    /**
     * The Unicode character for boxes
     */
    public static final char BOX_UNICODE = '█';

    /**
     * Gets a progress bar for the given progress out of 100
     *
     * @param progress the progress
     * @return the string
     */
    public static String getProgressBar(double progress) {
        return getProgressBar(progress, 100.0D);
    }

    /**
     * Gets the progress bar for the given progress out of the given max
     *
     * @param progress the progress
     * @param maxProgress the maximum progress
     * @return the string
     */
    public static String getProgressBar(double progress, double maxProgress) {
        return getProgressBar(progress, maxProgress, 10);
    }

    /**
     * Gets the progress bar for the given progress out of the given max with the given length
     *
     * @param progress the progress
     * @param maxProgress the max progress
     * @param length the length of the progress bar
     * @return the string
     */
    public static String getProgressBar(double progress, double maxProgress, int length) {
        return getProgressBar(progress, maxProgress, BOX_UNICODE, length);
    }

    /**
     * Returns a progress bar, colored, of "length" length made up of "barChar" characters.
     *
     * @param progress the progress
     * @param maxProgress the maximum progress
     * @param barChar the box character
     * @param length the length of the bar
     * @return the string
     */
    public static String getProgressBar(double progress, double maxProgress, char barChar, int length) {
        return getProgressBar(progress, maxProgress, barChar, length, COLOR_GREEN, COLOR_LIME, COLOR_GRAY);
    }

    /**
     * Returns a progress bar, colored, of "length" length made up of "barChar" characters.
     *
     * @param progress the progress
     * @param maxProgress the maximum progress
     * @param barChar the box character
     * @param length the length of the bar
     * @param filledColor the filled color of the bar
     * @param semiFilledColor the semi filled color of the bar
     * @param emptyColor the empty color of the bar
     * @return the string
     */
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
