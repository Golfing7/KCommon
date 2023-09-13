package com.golfing8.kcommon.struct.time;

import com.golfing8.kcommon.KCommon;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import lombok.*;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A time stamp used for marking a certain time.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Timestamp {
    /** This value is used when a field isn't being used by this timestamp. */
    public static final int UNUSED = -1;
    /** The year that takes place, or {@link #UNUSED} if not in use. */
    private int year;
    /** The zero-based based month that takes place, or {@link #UNUSED} if not in use. */
    private int month;
    /** The zero-based day of week that takes place, or {@link #UNUSED} if not in use. */
    private int dayOfWeek;
    /** The zero-based day of month that takes place, or {@link #UNUSED} if not in use. */
    private int dayOfMonth;
    /** The zero-based day of year that takes place, or {@link #UNUSED} if not in use. */
    private int dayOfYear;
    /** The hour that takes place, or {@link #UNUSED} if not in use. */
    private int hour;
    /** The minute that takes place, or {@link #UNUSED} if not in use. */
    private int minute;
    /** The second that takes place, or {@link #UNUSED} if not in use. */
    private int second;

    /**
     * Checks if this timestamp is after the other time stamp.
     * <p>
     * In the event that other fields are in use (e.g. This uses year, 'that' doesn't),
     * the 'unused' fields are not compared.
     * </p>
     * <p>
     * Also note the only 'day of' field used in this calculation is {@link #dayOfMonth}.
     * {@link #dayOfYear} and {@link #dayOfWeek} are unused.
     * </p>
     *
     * @param timestamp the other time stamp.
     * @return if this time stamp is after the other.
     */
    public boolean isAfter(Timestamp timestamp) {
        if (this.year != timestamp.year && this.year != UNUSED && timestamp.year != UNUSED)
            return this.year > timestamp.year;

        if (this.month != timestamp.month && this.month != UNUSED && timestamp.month != UNUSED)
            return this.month > timestamp.month;

        if (this.dayOfMonth != timestamp.dayOfMonth && this.dayOfMonth != UNUSED && timestamp.dayOfMonth != UNUSED)
            return this.dayOfMonth > timestamp.dayOfMonth;

        if (this.hour != timestamp.hour && this.hour != UNUSED && timestamp.hour != UNUSED)
            return this.hour > timestamp.hour;

        if (this.minute != timestamp.minute && this.minute != UNUSED && timestamp.minute != UNUSED)
            return this.minute > timestamp.minute;

        return this.second > timestamp.second;
    }

    @Override
    public String toString() {
        return "Timestamp{" +
                "year=" + year +
                ", month=" + month +
                ", dayOfWeek=" + dayOfWeek +
                ", dayOfMonth=" + dayOfMonth +
                ", dayOfYear=" + dayOfYear +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                '}';
    }

    /**
     * Parses the given timestamp from the string.
     * <p>
     * Formatting should be match one of the following: (optional)
     * <ol>
     * <li>hh:mm(:ss)</li>
     * <li>MM-DD-YYYY(-hh:mm(:ss))</li>
     * </ol>
     * </p>
     *
     * @param timestamp the timestamp.
     * @return the parsed timestamp.
     */
    public static Timestamp parse(String timestamp) {
        Preconditions.checkNotNull(timestamp, "Timestamp cannot be null.");

        String toParse = timestamp;
        // Attempt parsing as a date time.
        int months = UNUSED;
        int days = UNUSED;
        int years = UNUSED;
        if (timestamp.contains("-")) {
            String[] dataSplit = toParse.split("-");
            if (dataSplit.length != 4)
                throw new DateTimeException(String.format("Timestamp format does not follow: MM-DD-YYYY(-hh:mm(:ss)). Was %s", timestamp));
            
            months = Integer.parseInt(dataSplit[0]);
            days = Integer.parseInt(dataSplit[1]);
            years = Integer.parseInt(dataSplit[2]);

            // Now let the remaining code parse hours.
            toParse = dataSplit[3];
        }

        String[] timeSplit = toParse.split(":");
        if (timeSplit.length < 2 || timeSplit.length > 3)
            throw new DateTimeException(String.format(String.format("Timestamp does not follow proper formatting. Was %s", timestamp)));

        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);
        int seconds = timeSplit.length > 2 ? Integer.parseInt(timeSplit[2]) : UNUSED;

        return new Timestamp(years, months, UNUSED, days, UNUSED, hours, minutes, seconds);
    }

    /**
     * Creates a timestamp representing the current time of call. Uses {@link KCommon#getTimeZone()} as the time zone.
     *
     * @return the time.
     */
    public static Timestamp now() {
        return now(KCommon.getInstance().getTimeZone());
    }

    /**
     * Creates a timestamp representing the current time of call.
     *
     * @return the time.
     */
    public static Timestamp now(ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return new Timestamp(now.getYear(),
                now.getMonth().getValue() - 1,
                now.getDayOfWeek().getValue() - 1,
                now.getDayOfMonth() - 1,
                now.getDayOfYear() - 1,
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }

    /**
     * Creates a time stamp that represents some time during a day.
     *
     * @param hour the hour.
     * @param minute the minute.
     * @param second the second.
     * @return the generated time stamp.
     */
    public static Timestamp ofIntraDay(int hour, int minute, int second) {
        return new Timestamp(UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, hour, minute, second);
    }
}
