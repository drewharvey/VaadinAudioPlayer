package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;

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
	
	private static boolean initialized = false;
	private static List<AudioStreamPlayer> players = new ArrayList<AudioStreamPlayer>();
	
	
	public static void exposeMethods() {
		createNamespace(NAMESPACE);
		createMethods();
		initialized = true;
	}
	
	public static void addAudioPlayerInstance(AudioStreamPlayer player) {
		if (!players.contains(player)) {
			players.add(player);
		}
	}
	
	public static void removeAudioPlayerInstance(AudioStreamPlayer player) {
		players.remove(player);
	}
	
	private static AudioStreamPlayer getPlayer(int index) {
		if (index < 0) {
			index = 0;
		}
		AudioStreamPlayer player = null;
		if (players.size() > index) {
			player = players.get(index);
		} 
		return player;
	}
	
	/**<p>Gets position of an audio player. If no player index is provided,
	 * the first audio player will be used.</p>
	 * <p>JS Usage Example:</p>
	 * <p><code>var position = window.vaadin_audioplayer.getPosition();</code></p> 
	 * <p><code>var position = window.vaadin_audioplayer.getPosition(1);</code></p> 
	 * @param playerIndex
	 * 		Index of the audio player to use. Optional.
	 * @return Position in milliseconds. 
	 * 		Returns -1 if no valid audio player is found.
	 */
	private static int getPosition(int playerIndex) {
		Logger.getLogger("JavaScriptPublicAPI").log(Level.SEVERE, "PLAYER INDEX: " + playerIndex);
		AudioStreamPlayer player = getPlayer(playerIndex);
		if (player != null) {
			return player.getPosition();
		}
		return -1;
	}
	
	/**
	 * <p>Gets duration of an audio file. If no player index is provided,
	 * the first audio player will be used.</p>
	 * <p>JS Usage Example:</p>
	 * <p><code>var duration = window.vaadin_audioplayer.getDuration();</code></p> 
	 * <p><code>var duration = window.vaadin_audioplayer.getDuration(1);</code></p> 
	 * @param playerIndex
	 * 		Index of the audio player to use. Optional.
	 * @return Duration in milliseconds. 
	 * 		Returns -1 if no valid audio player is found.
	 */
	private static int getDuration(int playerIndex) {
		AudioStreamPlayer player = getPlayer(playerIndex);
		if (player != null) {
			return player.getDuration();
		}
		return -1;
	}
	
	private static native void createNamespace(String namespace) /*-{
		$wnd[namespace] = {};
	}-*/;
	
	private static native void createMethods() /*-{

		$wnd.vaadin_audioplayer.getPosition = function(index) {
			index = index || 0;
			return @com.vaadin.addon.audio.client.JavaScriptPublicAPI::getPosition(I)(index);
		}  
		
		$wnd.vaadin_audioplayer.getDuration = function(index) {
			index = index || 0;
			return @com.vaadin.addon.audio.client.JavaScriptPublicAPI::getDuration(I)(index);
		}
		
	}-*/;
}
