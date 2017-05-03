package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
	
	Duration execTime = null;
	
	private static final int MAX_PLAYERS = 3;	// Maximum number of players
	//TODO: get values from ChunkDescriptions
	private int timePerChunk = 5000;
	private int chunkOverlapTime = 500; // extra time added to end of each chunk
	
	private int currentPlayer = 0;
	private int position = 0;
	private int chunkPosition = 0;
	
	private double volume = 1;
	private double playbackSpeed = 1;
	private double balance = 0;
	
	private ClientStream stream;
	BufferPlayer[] players = new BufferPlayer[MAX_PLAYERS];
	private Timer playNextChunkTimer;
	private Duration chunkPositionClock;
	
	private List<Effect> effects = new ArrayList<Effect>();

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
				setPersistingPlayerOptions(player, true);
				setCurrentPlayer(player);
				// TODO: get chunk overlap time from chunk descriptor
				//chunkOverlapTime = chunk.getLeadOutDuration();
				// TODO: shouldn't need to add 1 here
				timePerChunk = chunk.getEndTimeOffset() - chunk.getStartSampleOffset() + 1 - chunkOverlapTime;
				logError("timePerChunk: " + timePerChunk + " / chunkLeadTime: " + chunkOverlapTime);
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
	
	public void play() {
		play(true);
	}
	
	public void play(boolean useCrossFade) {
		play(0, useCrossFade);
	}
	
	private void play(int timeOffset, boolean useCrossFade) {
		logError("PLAY");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		
		chunkPosition = timeOffset;
		
		if (useCrossFade) {
			// use cross fade to blend prev and current audio together
			crossFadePlayers(getCurrentPlayer(), getPrevPlayer(), getCurrentPlayer().getVolume());
		} else {
			// simply play the audio
			getCurrentPlayer().play(chunkPosition);
			// track execution time to offset scheduled time for next chunk
			execTime = new Duration();
			// starts counting MS when instantiated, used primarily for pausing
			chunkPositionClock = new Duration();
		}
		
		// start timer to play next chunk of audio
		if (position < timePerChunk) {
			// for some reason the first chunk is fading out 500ms early (or the second chunk is 500ms late)
			// TODO: first chunk should work the same way as others
			logError("Scheduling for " + (timePerChunk - chunkPosition - chunkOverlapTime));
			playNextChunkTimer.schedule(timePerChunk - chunkPosition - chunkOverlapTime - execTime.elapsedMillis());
		} else {
			logError("Scheduling for " + (timePerChunk - chunkPosition));
			playNextChunkTimer.schedule(timePerChunk - chunkPosition - execTime.elapsedMillis());
		}
		
		// start loading next chunk
		int nextChunkTime = position + timePerChunk + chunkOverlapTime;
		logError("nextChunkTime: " + nextChunkTime);
		stream.requestChunkByTimestamp(nextChunkTime, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				BufferPlayer player = new BufferPlayer();
				player.setBuffer(stream.getBufferForChunk(chunk));
				setNextPlayer(player);
			}
		});
	}
	
	/**
	 * Uses a equal power crossfade curve to blend two audio players together.
	 * After the crossfade the currentPlayer will have volume and the prevPlayer
	 * will have a volume of 0.
	 * @param currentPlayer
	 * @param prevPlayer
	 */
	private void crossFadePlayers(final BufferPlayer currentPlayer, final BufferPlayer prevPlayer, final double targetGain) {
		// set volume to 0 and start playing
		if (currentPlayer != null) {
			currentPlayer.setVolume(0);
			currentPlayer.play(chunkPosition);
		}
		// track execution time to offset scheduled time for next chunk
		execTime = new Duration();
		// starts counting MS when instantiated, used primarily for pausing
		chunkPositionClock = new Duration();
		// if we have a prev player then we fade it out and fade our new player in
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				// TODO: use exponentialRampToValueAtTime() when IE supports it:
				// https://developer.mozilla.org/en-US/docs/Web/API/AudioParam/exponentialRampToValueAtTime
				for (double t = 1; t >= -1; t-= 0.001) {
					double[] gains = getCrossFadeValues(t);
					if (currentPlayer != null) {
						currentPlayer.setVolume(gains[0] * targetGain);
					}
					if (prevPlayer != null) {
						prevPlayer.setVolume(gains[1] * targetGain);
					}
				}
				// make sure we are at max/min volumes by the end
				if (currentPlayer != null) {
					currentPlayer.setVolume(targetGain);
				}
				if (prevPlayer != null) {
					prevPlayer.setVolume(0);
				}
			}
		});
	}
	
	/**
	 * Takes a time value (between 1 and -1) and calculates
	 * the volume level following a equal power crossfade curve.
	 * 
	 * @param t 
	 * @param isIncreasing
	 * @return volume (between 0 and 1)
	 */
	private double[] getCrossFadeValues(double t) {
		double[] val = new double[2];
	    val[0] = Math.sqrt(0.5 * (1 - t));
	    val[1] = Math.sqrt(0.5 * (1 + t));
	    return val;
	}
	
	public void pause() {
		logError("PAUSE");	
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		chunkPosition += chunkPositionClock.elapsedMillis();
		chunkPositionClock = null;
		getCurrentPlayer().stop();
		playNextChunkTimer.cancel();
		logError("pause() - elapsedTime:  " + chunkPosition);
	}
	
	public void resume() {
		logError("resume");
		if (getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		logError("resume() - elapsed: " + chunkPosition);
		logError("scheduled for " + (timePerChunk - chunkPosition));
		getCurrentPlayer().play(chunkPosition);
		// track execution time to offset scheduled time for next chunk
		execTime = new Duration();
		// schedule next chunk handoff
		playNextChunkTimer.schedule(timePerChunk - chunkPosition);
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
		if (execTime != null) logError("playNextChunk(): " + execTime.elapsedMillis());
		position += timePerChunk;
		// stop the audio if we've reached the end
		if (getPosition() >= getDuration()) {
			stop();
		} else {
			// make sure the next player has the same options set as current
			setPersistingPlayerOptions(getNextPlayer(), true);
			moveToNextPlayer();
			play(true);
		}
	}
	
	private void setPersistingPlayerOptions(BufferPlayer player, boolean rebuildNodeChain) {
		if (player == null) {
			Log.error(this, "Cannot copy player options to null BufferPlayer");
			return;
		}
		// copy persisting settings to next audio node
		player.setVolume(volume);
		player.setPlaybackSpeed(playbackSpeed);
		player.setBalance(balance);
		player.setEffects(getEffects());
		// TODO: should players auto build chain when changed?
		// force player to rebuild audio node chain
		if (rebuildNodeChain) {
			player.getOutput();
		}
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
				setPersistingPlayerOptions(player, true);
				setCurrentPlayer(player);
				int offset = millis % timePerChunk;
				position = millis - offset;
				play(offset, false);
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
	
	public void setVolume(double volume) {
		this.volume = volume;
		if(getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		if (getCurrentPlayer() != null) {
			getCurrentPlayer().setVolume(volume);
		}
	}
	
	public double getVolume() {
		return volume;
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		this.playbackSpeed = playbackSpeed;
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return;
		}
		if (getCurrentPlayer() != null) {
			getCurrentPlayer().setPlaybackSpeed(playbackSpeed);
		}
	}
	
	public double getPlaybackSpeed() {
		return playbackSpeed;
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
		if(getCurrentPlayer() == null) {
			logError("current player is null");
			return;
		}
		if (getCurrentPlayer() != null) {
			getCurrentPlayer().setBalance(balance);
		}
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void setEffects(List<Effect> effects) {
		logError("AudioStreamPlayer adding effects");
		this.effects.clear();
		if (effects != null) {
			this.effects.addAll(effects);
		}
		if (getCurrentPlayer() != null) {
			getCurrentPlayer().setEffects(effects);
		}
		
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
