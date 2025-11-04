package com.kingrunes.somnia.common.util;

/**
 * Represents a range of time in ticks.
 */
public class TimePeriod {

    private final long start;
    private final long end;

    /**
     * Creates a new TimePeriod.
     *
     * @param start the start time (inclusive)
     * @param end   the end time (inclusive)
     */
    public TimePeriod(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Checks whether a given time is within this time period.
     *
     * @param time the time to check
     * @return true if time is within [start, end], false otherwise
     */
    public boolean isTimeWithin(long time) {
        return time >= start && time <= end;
    }

    /**
     * @return the start time of this period
     */
    public long getStart() {
        return start;
    }

    /**
     * @return the end time of this period
     */
    public long getEnd() {
        return end;
    }
}
