package com.vaadin.addon.audio.client;

import java.util.Stack;

import com.vaadin.addon.audio.client.util.Log;
import com.vaadin.addon.audio.shared.ChunkDescriptor;

public class AudioStreamPlayer {

	private StreamReceiver stream;
	
	private Stack<BufferPlayer> prevPlayers = new Stack<>();
	private Stack<BufferPlayer> nextPlayers = new Stack<>();
	private BufferPlayer currentPlayer = null;
	
	private int duration = 0;
	private int position = 0;
	
	public AudioStreamPlayer(StreamReceiver stream) {
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
		currentPlayer.setVolume(volume);
	}
	
	public double getVolume() {
		return currentPlayer.getVolume();
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		currentPlayer.setPlaybackSpeed(playbackSpeed);
	}
	
	public double getPlaybackSpeed() {
		return currentPlayer.getPlaybackSpeed();
	}
	
	public void setBalance(double balance) {
		currentPlayer.setBalance(balance);
	}
	
	public double getBalance() {
		return currentPlayer.getBalance();
	}
	
}
