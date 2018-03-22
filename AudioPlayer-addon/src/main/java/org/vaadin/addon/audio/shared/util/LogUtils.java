package org.vaadin.addon.audio.shared.util;

/**
 * Created by drewharvey on 6/20/17.
 */
public class LogUtils {

    /**
     * Prefix used in normal debug message in order to filter out all the other Vaadin logs.
     */
    public static String DEBUG_LOG_PREFIX = "[ap] ";

    public static String prefix(String message) {
        return DEBUG_LOG_PREFIX + message;
    }
}
