package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.vaadin.addon.audio.client.ClientStream.DataCallback;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.util.Log;
import com.vaadin.ui.Notification;

/**
 * Player controls for a stream.
 */
public class AudioStreamPlayer {

	//
	// TODO: improve log messages
	//
	
	private static final int MAX_PLAYERS = 3;	// Maximum number of players
	
	//TODO: sync this with server side chunking
	private static final int TIME_PER_CHUNK = 5000;
	
	private ClientStream stream;
	private BufferPlayer player = null;
	private int currentPlayer = 0;
	BufferPlayer[] players = new BufferPlayer[MAX_PLAYERS];
	
	private List<Effect> effects = new ArrayList<Effect>();
	
	private int duration = 0;
	private int position = 0;
	private int playerStartPosition = 0;
	
	private Timer playNextChunkTimer;
	private Duration durationObj;
	private int currentPlayTime = 0;
	private int elapsedTime = 0;
	
	public AudioStreamPlayer(ClientStream stream) {
		Log.message(this, "create");
		
		// Warm up the stream
		this.stream = stream;
		// create players
		for (int i = 0; i < players.length; i++) {
			players[i] = new BufferPlayer();
		}
		// get first chunk of audio
		player = getCurrentPlayer();
		stream.requestChunkByTimestamp(0, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
			}
		});
		// setup timer for moving to next chunk after current chunk finishes playing
		playNextChunkTimer = new Timer() {
			@Override
			public void run() {
				playNextChunk();
			}
		};
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setPosition(final int millis) {
		Log.message(this, "set position to " + millis);
		stop();
		stream.requestChunkByTimestamp(millis, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				player.setBuffer(stream.getBufferForChunk(chunk));
				position = millis;
			}
		});
	}
	
	public int getPosition() {
		return position;
	}
	
	public void play() {
		Log.message(this, "play");
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		
		// start player current chunk
		player.play(playerStartPosition);
		
		// Duration object starts counting MS when instantiated (used to continue from a pause)
		durationObj = new Duration();

		// start timer to play next chunk of audio
		playNextChunkTimer.schedule(TIME_PER_CHUNK);
		
		// start loading next chunk
		int nextChunkTime = currentPlayTime + TIME_PER_CHUNK;
		stream.requestChunkByTimestamp(nextChunkTime, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				getNextPlayer().setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
			}
		});
	}
	
	private void playNextChunk() {
		moveToNextPlayer();
		currentPlayTime += TIME_PER_CHUNK;
		player = getCurrentPlayer();
		play();
	}
	
	private void moveToNextPlayer() {
		currentPlayer = (currentPlayer + 1) % MAX_PLAYERS;
	}
	
	private void moveToPrevPlayer() {
		currentPlayer = (currentPlayer == 0 ? MAX_PLAYERS - 1 : currentPlayer - 1);
	}
	
	private BufferPlayer getCurrentPlayer() {
		return players[currentPlayer];
	}
	
	private BufferPlayer getNextPlayer() {
		int i = (currentPlayer + 1) % MAX_PLAYERS;
		return players[i];
	}
	
	private BufferPlayer getPrevPlayer() {
		int i = (currentPlayer == 0 ? MAX_PLAYERS - 1 : currentPlayer - 1);
		return players[i];
	}
	
	public void pause() {
		Log.message(this, "pause");	
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.stop();
		elapsedTime += durationObj.elapsedMillis();
		playNextChunkTimer.cancel();
	}
	
	public void resume() {
		Log.message(this, "resume");
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.play(elapsedTime);
		playNextChunkTimer.schedule(TIME_PER_CHUNK - elapsedTime);
		// Duration object starts counting MS when instantiated
		durationObj = new Duration();
	}
	
	public void stop() {
		Log.message(this, "stop");
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		// reset everything
		player.stop();
		playNextChunkTimer.cancel();
		currentPlayTime = 0;
		elapsedTime = 0;
		// load the first chunk (beginning of audio)
		stream.requestChunkByTimestamp(0, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
			}
		});
	}
	
	public void setVolume(double volume) {
		if(player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.setVolume(volume);
	}
	
	public double getVolume() {
		if(player == null) {
			logError("current player is null");
			return 0;
		}
		return player.getVolume();
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		if(player == null) {
			logError("current player is null");
			return;
		}
		player.setPlaybackSpeed(playbackSpeed);
	}
	
	public double getPlaybackSpeed() {
		if(player == null) {
			logError("current player is null");
			return 0;
		}
		return player.getPlaybackSpeed();
	}
	
	public void setBalance(double balance) {
		if(player == null) {
			logError("current player is null");
			return;
		}
		player.setBalance(balance);
	}
	
	public double getBalance() {
		if(player == null) {
			logError("current player is null");
			return 0;
		}
		return player.getBalance();
	}
	
	public void setEffects(List<Effect> effects) {
		Log.message(this, "AudioStreamPlayer adding effects");
		this.effects.clear();
		this.effects.addAll(effects);
		//player.setEffects(effects);
		
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
