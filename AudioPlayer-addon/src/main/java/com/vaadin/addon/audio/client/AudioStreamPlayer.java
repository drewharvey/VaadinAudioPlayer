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
	//TODO: get values from ChunkDescriptions
	private static final int TIME_PER_CHUNK = 5000;
	private static final int LEAD_IN_TIME = 0;
	
	private ClientStream stream;
	BufferPlayer[] players = new BufferPlayer[MAX_PLAYERS];
	private int currentPlayer = 0;
	
	private List<Effect> effects = new ArrayList<Effect>();
	
	private int playerStartPosition = 0;
	private int position = 0;
	private int chunkPosition = 0;
	
	private Timer playNextChunkTimer;
	private Timer updatePositionTimer;
	private Duration chunkPositionClock;
	
	public AudioStreamPlayer(ClientStream stream) {
		logError("create");
		
		// Warm up the stream
		this.stream = stream;
		
		logError("Duration: " + getDuration());
		
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
		//
		updatePositionTimer = new Timer() {
			@Override
			public void run() {
				 
			}
		};
	}
	
	public int getDuration() {
		return stream.getDuration();
	}
	
	public void setPosition(final int millis) {
		logError("set position to " + millis);
		getCurrentPlayer().stop();
		chunkPosition += chunkPositionClock.elapsedMillis();
		// TODO: impl in a way we don't need the current chunk's playtime offset
		int chunkTime = millis;
		if (chunkTime < 0) {
			chunkTime = 0;
		}
		logError("setPosition() - millis: " + millis + " / elapsedTime:  " + chunkPosition + " / chunkTime: " + chunkTime);
		playNextChunkTimer.cancel();
		stream.requestChunkByTimestamp(chunkTime, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setCurrentPlayer(player);
				int offset = millis % TIME_PER_CHUNK;
				position = millis - offset;
				play(offset);
			}
		});
	}
	
	/**
	 * Gets current playtime position of the entire audio file.
	 * @return playtime position (milliseconds)
	 */
	public int getPosition() {
		return position + getChunkPosition();
	}
	
	public void play() {
		chunkPosition = 0;
		play(chunkPosition);
	}
	
	private void play(int offset) {
		logError("PLAY");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		chunkPosition = offset;
		// start player current chunk
		getCurrentPlayer().play(chunkPosition);
		
		// starts counting MS when instantiated, used primarily for pausing
		chunkPositionClock = new Duration();

		// start timer to play next chunk of audio
		playNextChunkTimer.schedule(TIME_PER_CHUNK - chunkPosition);
		
		// start loading next chunk
		int nextChunkTime = position + TIME_PER_CHUNK;
		stream.requestChunkByTimestamp(nextChunkTime, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk));
				setNextPlayer(player);
			}
		});
	}
	
	/**
	 * Gets playtime position of the current audio chunk.
	 * @return playtime position (milliseconds)
	 */
	private int getChunkPosition() {
		int pos = chunkPosition;
		if (chunkPositionClock != null) {
			pos += chunkPositionClock.elapsedMillis();
		}
		return pos;
	}
	
	private void playNextChunk() {
		logError("PLAY NEXT CHUNK");
		position += TIME_PER_CHUNK;
		// stop the audio if we've reached the end
		if (getPosition() > getDuration()) {
			stop();
		} else {
			moveToNextPlayer();
			play();
		}
	}
	
	public void pause() {
		logError("PAUSE");	
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		getCurrentPlayer().stop();
		chunkPosition += chunkPositionClock.elapsedMillis();
		chunkPositionClock = null;
		logError("pause() - elapsedTime:  " + chunkPosition);
		playNextChunkTimer.cancel();
	}
	
	public void resume() {
		logError("resume");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		logError("resume() - elapsed: " + chunkPosition);
		logError("scheduled for " + (TIME_PER_CHUNK - chunkPosition));
		getCurrentPlayer().play(chunkPosition);
		playNextChunkTimer.schedule(TIME_PER_CHUNK - chunkPosition);
		// Duration object starts counting MS when instantiated
		chunkPositionClock = new Duration();
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
		position = 0;
		chunkPosition = 0;
		chunkPositionClock = null;
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
