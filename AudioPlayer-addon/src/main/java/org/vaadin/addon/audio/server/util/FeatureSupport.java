package org.vaadin.addon.audio.server.util;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

public class FeatureSupport {
	
	public static WebBrowser browser;
	
	// compatibility table found at:
	// https://developer.mozilla.org/en-US/docs/Web/HTML/Supported_media_formats#Browser_compatibility
	
	
	// TODO: check support by actually instantiating on the client instead of "guessing"
	// Example: 
	// Audio audio = Audio.createIfSupported();
	// audio.canPlayType("audio/mpeg");
	//
	// Patrik: How can we get this information to the server before the AudioPlayer object is created?
	
	public static boolean isAudioElementSupported() {
		browser = getBrowser();
		if (browser.isChrome()) {
			// 3.0+
			return browser.getBrowserMajorVersion() >= 3;
		}
		if (browser.isEdge()) {
			return true;
		}
		if (browser.isIE()) {
			// 9.0+
			return browser.getBrowserMajorVersion() >= 9;
		}
		if (browser.isFirefox()) {
			// 3.5+
			return browser.getBrowserMajorVersion() > 3
					|| (browser.getBrowserMajorVersion() == 3 && browser.getBrowserMinorVersion() >= 5);
		}
		if (browser.isSafari()) {
			// 3.1+
			return browser.getBrowserMajorVersion() > 3
					|| (browser.getBrowserMajorVersion() == 3 && browser.getBrowserMinorVersion() >= 1);
		}
		if (browser.isOpera()) {
			// 10.5+
			return browser.getBrowserMajorVersion() > 10
					|| browser.getBrowserMajorVersion() == 10 && browser.getBrowserMinorVersion() >= 5;
		}
		return false;
	}
	
	public static boolean isMp3Supported() {
		// all major browsers implemented mp3 support with initial audio element support
		return isAudioElementSupported();
	}
	
	public static boolean isOggSupported() {
		browser = getBrowser();
		// not supported on IE and Safari
		return isAudioElementSupported() && !browser.isSafari() && !browser.isIE();
	}
	
	private static WebBrowser getBrowser() {
		if (browser == null) {
			browser = Page.getCurrent().getWebBrowser();
		}
		return browser;
	}
	
}
