package com.vaadin.addon.audio.client;

/**
 * Class that contains public JavaScript functions that can be accessed
 * by external JavaScript. 
 * 
 * All public methods are exposed thru the window.vaadin_audioplayer object.
 * 
 * Example of calling getPosition() from external js:
 *  	var position = window.vaadin_audioplayer.getPosition();
 */
public class JavaScriptPublicAPI {
	
	public static final String NAMESPACE = "vaadin_audioplayer";
	
	public static void exposeMethods() {
		createNamespace(NAMESPACE);
		createMethods();
	}
	
	
	/**
	 * Gets position of player. Example usage in JS:<br><br>
	 * <code>var position = window.vaadin_audioplayer.getPosition();</code>
	 * @return position in milliseconds (integer)
	 */
	private static int getPosition() {
		return 0;
	}
	
	/**
	 * Gets total duration of current audio file. Example usage in JS:<br><br>
	 * <code>var duration = window.vaadin_audioplayer.getDuration();</code>
	 * @return duration in milliseconds (integer)
	 */
	private static int getDuration() {
		return 0;
	}
	
	private static native void createNamespace(String namespace) /*-{
		$wnd[namespace] = {};
	}-*/;
	
	private static native void createMethods() /*-{
		$wnd.vaadin_audioplayer.getPosition = @com.vaadin.addon.audio.client.JavaScriptPublicMethods::getPosition();
		$wnd.vaadin_audioplayer.getDuration = @com.vaadin.addon.audio.client.JavaScriptPublicMethods::getDuration();
		
		// example of typed function (not sure if it works)
	    //$wnd.getPosition = function(param) {
	    //     @com.vaadin.addon.audio.client.AudioStreamPlayer::getPosition(I)(param);
	    //};
	}-*/;
}
