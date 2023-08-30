package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;

public final class ProgressBar {
    private static final String COLOR_GREEN = ChatColor.DARK_GREEN.toString();
    private static final String COLOR_LIME = ChatColor.GREEN.toString();
    private static final String COLOR_GRAY = ChatColor.GRAY.toString();

    public static final char BOX_UNICODE = '\u2588';

    private ProgressBar() {}

    /**
     * Returns a progress bar, colored, of "length" length made up of "barChar" characters.
     * @param progress
     * @param maxProgress
     * @param barChar
     * @param length
     * @return
     */
    public static String getProgressBar(double progress, double maxProgress, char barChar, int length)
    {
        double percentage = (progress / maxProgress) * 100.0D;

        StringBuilder progressBar = new StringBuilder();

        double percentPerChar = (1D / length) * 100.0D;

        for(int i = 0; i < length; i++)
        {
            double progressPassBar = percentPerChar * (i + 1);

            if(percentage >= progressPassBar)
                progressBar.append(COLOR_GREEN);
            else if(percentage + percentPerChar >= progressPassBar)
                progressBar.append(COLOR_LIME);
            else
                progressBar.append(COLOR_GRAY);

            progressBar.append(barChar);
        }
        return progressBar.toString();
    }
}
