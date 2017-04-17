package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.client.ClientStream.DataCallback;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.util.Log;

/**
 * Player controls for a stream.
 */
public class AudioStreamPlayer {
	
	private static Logger logger = Logger.getLogger("AudioStreamPlayer");

	//
	// TODO: improve log messages
	//
	
	private static final int MAX_PLAYERS = 3;	// Maximum number of players
	
	//TODO: sync this with server side chunking
	private static final int TIME_PER_CHUNK = 5000;
	
	private ClientStream stream;
	BufferPlayer[] players = new BufferPlayer[MAX_PLAYERS];
	private int currentPlayer = 0;
	
	private List<Effect> effects = new ArrayList<Effect>();
	
	private int duration = 0;
	private int position = 0;
	private int playerStartPosition = 0;
	
	private Timer playNextChunkTimer;
	private Duration durationObj;
	private int currentPlayTime = 0;
	private int elapsedTime = 0;
	
	public AudioStreamPlayer(ClientStream stream) {
		logError("create");
		
		// Warm up the stream
		this.stream = stream;
		stream.requestChunkByTimestamp(0, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setCurrentPlayer(player);
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
		logError("set position to " + millis);
		stop();
		stream.requestChunkByTimestamp(millis, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setCurrentPlayer(player);
				position = millis;
			}
		});
	}
	
	public int getPosition() {
		return position;
	}
	
	public void play() {
		logError("PLAY");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		
		// start player current chunk
		getCurrentPlayer().play(playerStartPosition);
		
		// Duration object starts counting MS when instantiated (used to continue from a pause)
		elapsedTime = 0;
		durationObj = new Duration();

		// start timer to play next chunk of audio
		playNextChunkTimer.schedule(TIME_PER_CHUNK);
		
		// start loading next chunk
		int nextChunkTime = currentPlayTime + TIME_PER_CHUNK;
		stream.requestChunkByTimestamp(nextChunkTime, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setNextPlayer(player);
			}
		});
	}
	
	private void playNextChunk() {
		logError("PLAY NEXT CHUNK");
		moveToNextPlayer();
		currentPlayTime += TIME_PER_CHUNK;
		play();
	}
	
	public void pause() {
		logError("PAUSE");	
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		getCurrentPlayer().stop();
		elapsedTime += durationObj.elapsedMillis();
		logError("pause() - elapsedTime:  " + elapsedTime);
		playNextChunkTimer.cancel();
	}
	
	public void resume() {
		logError("resume");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		logError("resume() - elapsed: " + elapsedTime);
		logError("scheduled for " + (TIME_PER_CHUNK - elapsedTime));
		getCurrentPlayer().play(elapsedTime);
		playNextChunkTimer.schedule(TIME_PER_CHUNK - elapsedTime);
		// Duration object starts counting MS when instantiated
		durationObj = new Duration();
	}
	
	public void stop() {
		logError("stop");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		// reset everything
		getCurrentPlayer().stop();
		playNextChunkTimer.cancel();
		currentPlayTime = 0;
		elapsedTime = 0;
		// load the first chunk (beginning of audio)
		stream.requestChunkByTimestamp(0, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setCurrentPlayer(player);
			}
		});
	}
	
	private void setCurrentPlayer(BufferPlayer player) {
		players[currentPlayer] = player;
	}
	
	private void setNextPlayer(BufferPlayer player) {
		int i = (currentPlayer + 1) % MAX_PLAYERS;
		players[i] = player;
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
	
	public void setVolume(double volume) {
		if(getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		getCurrentPlayer().setVolume(volume);
	}
	
	public double getVolume() {
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return 0;
		}
		return getCurrentPlayer().getVolume();
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return;
		}
		getCurrentPlayer().setPlaybackSpeed(playbackSpeed);
	}
	
	public double getPlaybackSpeed() {
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return 0;
		}
		return getCurrentPlayer().getPlaybackSpeed();
	}
	
	public void setBalance(double balance) {
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return;
		}
		getCurrentPlayer().setBalance(balance);
	}
	
	public double getBalance() {
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return 0;
		}
		return getCurrentPlayer().getBalance();
	}
	
	public void setEffects(List<Effect> effects) {
		logError("AudioStreamPlayer adding effects");
		this.effects.clear();
		this.effects.addAll(effects);
		//player.setEffects(effects);
		
	}
	
	public List<Effect> getEffects() {
		return effects;
	}

	private static void logError(String msg) {
		logger.log(Level.SEVERE, msg);
	}
	
	public String toString() {
		return "AudioStreamPlayer";
	}
}
