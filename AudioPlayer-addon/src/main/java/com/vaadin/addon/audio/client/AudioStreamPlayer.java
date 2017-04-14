package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.client.ClientStream.DataCallback;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
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
	private BufferPlayer player = null;
	private int currentPlayer = 0;
	BufferPlayer[] players = new BufferPlayer[2];
	
	private List<Effect> effects = new ArrayList<Effect>();
	
	private int duration = 0;
	private int position = 0;
	private int playerStartPosition = 0;
	
	private Duration durationObj;
	private int elapsedTime = 0;
	
	public AudioStreamPlayer(ClientStream stream) {
		Log.message(this, "create");
		
		// Warm up the stream
		this.stream = stream;
		players[0] = new BufferPlayer();
		players[1] = new BufferPlayer();
		player = players[currentPlayer];
		// get first chunk of audio
		stream.requestChunkByTimestamp(0, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				players[0].setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
			}
		});
		// get second chunk of audio
		stream.requestChunkByTimestamp(5000, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				players[1].setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
			}
		});
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
		player.play(playerStartPosition);
		// Duration object starts counting MS when instantiated
		durationObj = new Duration();
		Timer timer = new Timer() {
			@Override
			public void run() {
				currentPlayer = 1;
				player = players[currentPlayer];
				player.play(0);
			}
		};
		timer.schedule(5000);
	}
	
	public void pause() {
		Log.message(this, "pause");	
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.stop();
		elapsedTime += durationObj.elapsedMillis();
	}
	
	public void resume() {
		Log.message(this, "resume");
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.play(elapsedTime);
		// Duration object starts counting MS when instantiated
		durationObj = new Duration();
	}
	
	public void stop() {
		Log.message(this, "stop");
		if (player == null) {
			Log.error(this, "current player is null");
			return;
		}
		player.stop();
		elapsedTime = 0;
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
