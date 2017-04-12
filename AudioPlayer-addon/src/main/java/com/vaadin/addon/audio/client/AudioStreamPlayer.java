package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.addon.audio.shared.util.Log;

/**
 * Player controls for a stream.
 */
public class AudioStreamPlayer {

	//
	// TODO: improve log messages
	//
	
	private static final int MAX_PLAYERS = 3;	// Maximum number of players
	
	private ClientStream stream;
	
	private final Set<BufferPlayer> players = new HashSet<BufferPlayer>();
	private BufferPlayer currentPlayer = null;
	
	private List<Effect> effects = new ArrayList<Effect>();
	
	private int duration = 0;
	private int position = 0;
	
	public AudioStreamPlayer(ClientStream stream) {
		Log.message(this, "create");
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setPosition(int millis) {
		Log.message(this, "set position to " + millis);
	}
	
	public int getPosition() {
		return position;
	}
	
	public void play() {
		Log.message(this, "play");
		if (currentPlayer == null) {
			logError("current player is null");
			return;
		}
	}
	
	public void pause() {
		Log.message(this, "pause");		
	}
	
	public void resume() {
		Log.message(this, "resume");
	}
	
	public void stop() {
		Log.message(this, "stop");
	}
	
	public void setVolume(double volume) {
		if(currentPlayer == null) {
			logError("current player is null");
			return;
		}
		currentPlayer.setVolume(volume);
	}
	
	public double getVolume() {
		if(currentPlayer == null) {
			logError("current player is null");
			return 0;
		}
		return currentPlayer.getVolume();
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		if(currentPlayer == null) {
			logError("current player is null");
			return;
		}
		currentPlayer.setPlaybackSpeed(playbackSpeed);
	}
	
	public double getPlaybackSpeed() {
		if(currentPlayer == null) {
			logError("current player is null");
			return 0;
		}
		return currentPlayer.getPlaybackSpeed();
	}
	
	public void setBalance(double balance) {
		if(currentPlayer == null) {
			logError("current player is null");
			return;
		}
		currentPlayer.setBalance(balance);
	}
	
	public double getBalance() {
		if(currentPlayer == null) {
			logError("current player is null");
			return 0;
		}
		return currentPlayer.getBalance();
	}
	
	public void setEffects(List<Effect> effects) {
		Log.message(this, "AudioStreamPlayer adding effects");
		this.effects.clear();
		this.effects.addAll(effects);
		currentPlayer.setEffects(effects);
		
	}
	
	public List<Effect> getEffects() {
		return effects;
	}

	private static Logger logger = Logger.getLogger("AudioStreamPlayer");
	
	private static void logError(String msg) {
		logger.log(Level.SEVERE, msg);
	}
	
	public String toString() {
		return "AudioStreamPlayer";
	}
}
