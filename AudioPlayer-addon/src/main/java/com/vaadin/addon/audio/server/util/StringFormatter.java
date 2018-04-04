package com.vaadin.addon.audio.server.util;

/**
 * Created by drewharvey on 6/9/17.
 *
 * Utility methods for formatting strings and objects into strings.
 */
public class StringFormatter {

    /**
     * Converts milliseconds into a String object showing the number of
     * hours, minutes, and seconds.  Hours will only show if there is at
     * least 1 hour.
     *
     * @return String HH:MM:SS (ex: 01:23:20)
     */
    public static String msToPlayerTimeStamp(int ms) {
        long second = (ms / 1000) % 60;
        long minute = (ms / (1000 * 60)) % 60;
        long hour = (ms / (1000 * 60 * 60)) % 24;

        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }
    }
}
