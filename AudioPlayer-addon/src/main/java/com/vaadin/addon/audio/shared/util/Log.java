package com.vaadin.addon.audio.shared.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple mostly devtime helper to get around having to actually
 * declare a logger for every class...
 * 
 * TODO: improve logging further.
 */
public class Log {
	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	
	private static <T> String getInstanceName(T instance) {
		String name = instance.getClass().getSimpleName();
		if(!name.isEmpty()) return name;
		return instance.toString();
	}
	
	private static Logger getLogger(String cls) {
		if(loggers.containsKey(cls)) {
			return loggers.get(cls);
		}
		Logger logger = Logger.getLogger(cls);
		loggers.put(cls, logger);
		return logger;
	}
	
	private static void logMessage(String owner, String message, Level logLevel) {
		Logger l = getLogger(owner);
		l.log(logLevel, "[" + owner + "] " + message);
	}
	
	public static <T> void message(Class<T> cls, String message) {
		logMessage(cls.getSimpleName(), message, Level.INFO);
	}
	
	public static <T> void message(T instance, String message) {
		logMessage(getInstanceName(instance), message, Level.INFO);
	}

	public static <T> void warning(Class<T> cls, String message) {
		logMessage(cls.getSimpleName(), message, Level.WARNING);
	}
	
	public static <T> void warning(T instance, String message) {
		logMessage(getInstanceName(instance), message, Level.WARNING);
	}
	
	public static <T> void error(Class<T> cls, String message) {
		logMessage(cls.getSimpleName(), message, Level.SEVERE);
	}
	
	public static <T> void error(T instance, String message) {
		logMessage(getInstanceName(instance), message, Level.SEVERE);
	}
}
