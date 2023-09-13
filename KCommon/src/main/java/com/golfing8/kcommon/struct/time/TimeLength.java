package com.golfing8.kcommon.struct.time;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Represents some positive duration of time. This was created for the sake of regularizing string to time input.
 */
@NoArgsConstructor
public class TimeLength {
    /**
     * The time length in ticks.
     */
    @Getter
    private long durationTicks;

    /**
     * Constructs an instanceof of time length, guaranteeing a non-negative number.
     *
     * @param durationTicks the non-negative amount of ticks.
     */
    public TimeLength(long durationTicks) {
        Preconditions.checkArgument(durationTicks >= 0, "Duration must be greater or equal to 0!");
        this.durationTicks = durationTicks;
    }

    /**
     * Gets the amount of time in seconds.
     *
     * @return the amount of time in seconds.
     */
    public long toUnit(TimeUnit unit) {
        switch (unit) {
            case DAYS:
                return (durationTicks / 20L) / 86400L;
            case HOURS:
                return (durationTicks / 20L) / 3600;
            case MINUTES:
                return (durationTicks / 20L) / 60L;
            case SECONDS:
                return (durationTicks / 20L);
            case MILLISECONDS:
                return (durationTicks * 50L);
            case MICROSECONDS:
                return (durationTicks * 50L) * 1_000L;
            case NANOSECONDS:
                return (durationTicks * 50L) * 1_000_000L;
            default:
                throw new RuntimeException(String.format("Unrecognized time unit %s!", unit.name()));
        }
    }

    /**
     * Converts this time length to a string.
     *
     * @return the time length in string form.
     */
    public String getAsString(boolean includeTicks) {
        long totalDuration = durationTicks;
        int days = (int) totalDuration / 1728000;
        totalDuration %= 1728000;
        int hours = (int) totalDuration / 72000;
        totalDuration %= 72000;
        int minutes = (int) totalDuration / 1200;
        totalDuration %= 1200;
        int seconds = (int) totalDuration / 20;
        totalDuration %= 20;
        int ticks = (int) totalDuration;

        //Append all of them and return it.
        StringBuilder builder = new StringBuilder();
        if(days > 0)
            builder.append(days).append("d ");
        if(hours > 0)
            builder.append(hours).append("h ");
        if(minutes > 0)
            builder.append(minutes).append("m ");
        if(seconds > 0)
            builder.append(seconds).append("s ");
        if(ticks > 0 && includeTicks)
            builder.append(ticks).append("t");
        return builder.toString().trim();
    }

    @Override
    public String toString() {
        return getAsString(false);
    }

    /**
     * The string of input to parse into a TimeUnit. Returns null if the string wasn't parse-able.
     *
     * @param input the string of input.
     */
    @Nullable
    public static TimeLength parseTime(String input) {
        //The total time that has accumulated.
        long totalTime = 0L;

        //The 'in progress' accumulation.
        int accumulation = 0;
        for (int i = 0; i < input.length(); i++) {
            //Ignore any white space or commas.
            if(Character.isWhitespace(input.charAt(i)) || input.charAt(i) == ',') {
                if(accumulation > 0)
                    return null;
                continue;
            }

            //Interpret as a digit.
            if(Character.isDigit(input.charAt(i))) {
                accumulation *= 10;
                accumulation += input.charAt(i) - '0';
                continue;
            }

            char length = input.charAt(i);
            switch (length) {
                case 'd':
                    totalTime += accumulation * 20L * 86400L;
                    break;
                case 'h':
                    totalTime += accumulation * 20L * 3600L;
                    break;
                case 'm':
                    totalTime += accumulation * 20L * 60L;
                    break;
                case 's':
                    totalTime += accumulation * 20L;
                    break;
                case 't':
                    totalTime += accumulation;
                    break;
                default: //Unrecognized identifier.
                    return null;
            }

            accumulation = 0;
        }
        return new TimeLength(totalTime);
    }
}
