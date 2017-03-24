package com.vaadin.addon.audio.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple mostly devtime helper to get around having to actually
 * declare a logger for every class...
 * 
 *  XXX: of course dynamic name resolution doesn't work in GWT... *sigh*
 *  TODO: figure out some better way to log stuff. Perhaps a direct call to console.log? Always log a warning? What?
 */
public class Log {
	private static final Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
	
	private static Logger getLogger(Class<?> cls) {
		if(loggers.containsKey(cls)) {
			return loggers.get(cls);
		}
		Logger logger = Logger.getLogger(cls.getSimpleName());
		loggers.put(cls, logger);
		return logger;
	}
	
	public static <T> void message(T instance, String message) {
		getLogger(instance.getClass()).log(Level.INFO, message);
	}
	
	public static <T> void warning(T instance, String message) {
		getLogger(instance.getClass()).log(Level.WARNING, message);
	}
	
	public static <T> void error(T instance, String message) {
		getLogger(instance.getClass()).log(Level.SEVERE, message);
	}
}
