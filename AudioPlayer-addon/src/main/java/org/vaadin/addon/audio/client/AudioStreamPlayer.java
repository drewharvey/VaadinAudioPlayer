package org.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;
import org.vaadin.addon.audio.client.ClientStream.DataCallback;
import org.vaadin.addon.audio.client.utils.AudioBufferUtils;
import org.vaadin.addon.audio.shared.util.LogUtils;
import org.vaadin.addon.audio.client.webaudio.AudioNode;
import org.vaadin.addon.audio.client.webaudio.Buffer;
import org.vaadin.addon.audio.client.webaudio.BufferSourceNode;
import org.vaadin.addon.audio.shared.ChunkDescriptor;
import org.vaadin.addon.audio.shared.util.Log;

/**
 * Player controls for a stream.
 */
public class AudioStreamPlayer {

	// TODO: move audio effects and effects chain processing to separate class
	
	private static Logger logger = Logger.getLogger("AudioStreamPlayer");

	private static final int MAX_BUFFER_PLAYERS = 2;

	private int timePerChunk;						// duration of chunk (ms)
	private int chunkOverlapTime;					// duration of crossfade between chunks (ms)
	private int numChunksPreload = 0;				// number of chunks to load ahead of time (set higher for slower connections)

	private int position = 0;						// total position (ms) (in practice, chunk start position) @getPosition
	private int chunkPosition = 0;					// position within the current chunk (ms)
	
	private double volume = 1;
	private HashMap<Integer, Double> channelVolumes = new HashMap<>();
	private double playbackSpeed = 1;
	private double balance = 0;

	private ClientStream stream;					// used to fetch audio chunks from the server or cache
	private BufferPlayerManager playerManager;		// keeps track of rotating thru several BufferPlayers
	private Timer playNextChunkTimer;				// timer used to correctly time the handoff between BufferPlayers
	private Duration chunkPositionClock;			// keeps track of how far into a chunk we have played
	
	private List<Effect> effects = new ArrayList<Effect>();

	public AudioStreamPlayer(ClientStream stream, int timePerChunk) {
		this.stream = stream;
		this.timePerChunk = timePerChunk;
		playerManager = new BufferPlayerManager(MAX_BUFFER_PLAYERS);
		// request first audio chunk
		initFirstAudioChunk();
		// setup timer for moving to next chunk after current chunk finishes playing
		playNextChunkTimer = new Timer() {
			@Override
			public void run() {
				playNextChunk();
			}
		};
	}

	public void play() {
		int currentPosition = getPosition();
		int offset = currentPosition % timePerChunk;
		play(offset,true);
	}
	
	public void play(boolean useCrossFade) {
		play(0, useCrossFade);
	}
	
	private void play(int timeOffset, boolean useCrossFade) {
		logger.info(LogUtils.prefix("PLAY"));
		if (playerManager.getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}

		chunkPosition = timeOffset;

		chunkPositionClock = new Duration();

		if (useCrossFade) {
			// use cross fade to blend prev and current audio together
			int overlapTime = ((int) (chunkOverlapTime / playbackSpeed));
			AudioBufferUtils.crossFadePlayers(playerManager.getCurrentPlayer(), playerManager.getPrevPlayer(),
					chunkPosition, volume, overlapTime);
		} else {
			// simply play the audio
			playerManager.getCurrentPlayer().play(chunkPosition);
		}
		
		// start timer to play next chunk of audio
		scheduleNextChunk();
		
		// start loading next chunk
		int nextChunkTime = position + timePerChunk + chunkOverlapTime;
		nextChunkTime = Math.min(nextChunkTime, this.getDuration());
		fetchChunksForNextPlayer(nextChunkTime, numChunksPreload, timePerChunk, null, null);
	}
	
	public void pause() {
		logger.info(LogUtils.prefix("PAUSE"));
		if (playerManager.getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		chunkPosition += chunkPositionClock.elapsedMillis() * playbackSpeed;
		chunkPositionClock = null;
		playerManager.getCurrentPlayer().stop();
		playNextChunkTimer.cancel();
	}
	
	public void resume() {
		logger.info(LogUtils.prefix("resume"));
		if (playerManager.getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		setPersistingPlayerOptions(playerManager.getCurrentPlayer());
		playerManager.getCurrentPlayer().play(chunkPosition);
		// schedule next chunk handoff
		scheduleNextChunk();
		// Duration object starts counting MS when instantiated
		chunkPositionClock = new Duration();
	}
	
	public void stop() {
		logger.info(LogUtils.prefix("stop"));
		if (playerManager.getCurrentPlayer() == null) {
			Log.error(this, "current player is null");
			return;
		}
		// reset everything
		playerManager.getCurrentPlayer().stop();
		playNextChunkTimer.cancel();
		position = 0;
		chunkPosition = 0;
		chunkPositionClock = null;
		// load the first chunk (beginning of audio)
		initFirstAudioChunk();
	}

	private void initFirstAudioChunk() {
		fetchChunksForNextPlayer(0, numChunksPreload, timePerChunk, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				playerManager.moveToNextPlayer();
				chunkOverlapTime = chunk.getOverlapTime();
			}
		}, null);
	}

	/**
	 * Loads audio chunk at provided timestamp into the next BufferPlayer. You can also pre-load chunks by
	 * giving a value greater than 1 as numChunksToPreload.
	 * @param timestamp				timestap (ms) corresponding to first chunk
	 * @param numChunksToPreload	number of chunks to pre-load including the initial chunk
	 * @param timePerChunk			time (ms) of each chunk
	 * @param chunkDescReadyCb		ran when the ChunkDescriptor is ready, does NOT mean the AudioBuffer is ready
	 * @param bufferReadyCb			ran when the AudioBuffer is fully loaded and ready for use
	 */
	private void fetchChunksForNextPlayer(int timestamp, int numChunksToPreload, int timePerChunk,
										  final DataCallback chunkDescReadyCb,
										  final BufferSourceNode.BufferReadyListener bufferReadyCb) {
		// request first audio chunk
		stream.requestChunkByTimestamp(timestamp, new DataCallback() {
			@Override
			public void onDataReceived(ChunkDescriptor chunk) {
				// create new BufferPlayer
				final BufferPlayer player = new BufferPlayer();
				// assign this to the next BufferPlayer slot
				playerManager.setNextPlayer(player);
				// run the ChunkDescription is ready callback
				if (chunkDescReadyCb != null) {
					chunkDescReadyCb.onDataReceived(chunk);
				}
				// when AudioBuffer is ready, set the BufferPlayer's buffer
				player.setBuffer(AudioStreamPlayer.this.stream.getBufferForChunk(chunk), new BufferSourceNode.BufferReadyListener() {
					@Override
					public void onBufferReady(Buffer b) {
						setPersistingPlayerOptions(player);
						if (bufferReadyCb != null) {
							bufferReadyCb.onBufferReady(b);
						}
					}
				});
			}
		});
		// preload additional chunks if needed
		if (numChunksToPreload > 1) {
			for (int i = 1; i < numChunksToPreload; i++) {
				final int time = i * timePerChunk;
				stream.requestChunkByTimestamp(time, new DataCallback() {
					@Override
					public void onDataReceived(ChunkDescriptor chunk) {
						// these get auto cached in ClientStream
					}
				});
			}
		}
	}
	
	private void scheduleNextChunk() {
		playNextChunkTimer.cancel();
		double chunkDuration = timePerChunk / playbackSpeed;
		double chunkOffset = chunkPosition / playbackSpeed;
		double overlapDuration = chunkOverlapTime / playbackSpeed;
		if (position < chunkDuration) {
			logger.info(LogUtils.prefix("FIRST SCHEDULE"));
			// for some reason the first chunk is fading out 500ms early (or the second chunk is 500ms late)
			// TODO: first chunk should work the same way as others
			// truncating after decimal doesn't matter since we are already in milliseconds
			int time = ((int) (chunkDuration - chunkOffset - overlapDuration));
			if (time < 0) {
				time = 0;
			}
			playNextChunkTimer.schedule(time);
		} else {
			logger.info(LogUtils.prefix("LATER SCHEDULE"));
			int time = ((int) (chunkDuration - chunkOffset));
			if (time < 0) {
				time = 0;
			}
			playNextChunkTimer.schedule(time);
		}
	}
	
	/**
	 * Gets playtime position of the current audio chunk.
	 * @return playtime position (milliseconds)
	 */
	private int getChunkPosition() {
		int pos = chunkPosition;
		if (chunkPositionClock != null) {
			pos += (chunkPositionClock.elapsedMillis() * playbackSpeed);
		}
		return pos;
	}
	
	private void playNextChunk() {
		// stop the audio if we've reached the end
		int oldPosition = position;
		position += timePerChunk;
		if (oldPosition+getChunkPosition() >= getDuration()) {
			stop();
		} else {
			playerManager.moveToNextPlayer();
			play(true);
		}
	}
	
	private void setPersistingPlayerOptions(BufferPlayer player) {
		if (player == null) {
			Log.error(this, "Cannot copy player options to null BufferPlayer");
			return;
		}
		// make sure player has a working audio node chain
		player.configAudioNodeChain();
		// copy persisting settings to next audio node
		player.setVolume(volume);
		player.setPlaybackSpeed(playbackSpeed);
		player.setBalance(balance);
		// set gain per channel
		double gain;
		for (int i = 0; i < player.getNumberOfChannels(); i++) {
			// if we have channels without volume values, assume volume is 1
			if (!channelVolumes.containsKey(i)) {
				channelVolumes.put(i, 1d);
			}
			player.setVolume(channelVolumes.get(i), i);
		}
		// connect to fx chain
		connectBufferPlayerToEffectChain(player, effects);
	}

	public void setNumChunksPreload(int numChunksPreload) {
		logger.info(LogUtils.prefix("numChunksPreload updated"));
		this.numChunksPreload = numChunksPreload;
	}
	
	public int getDuration() {
		return stream.getDuration();
	}
	
	public void setPosition(final int millis) {
		logger.info(LogUtils.prefix("set position to " + millis));
		final boolean isPlaying = playerManager.getCurrentPlayer() != null ? playerManager.getCurrentPlayer().isPlaying() : false;
		if (isPlaying) {
			playerManager.getCurrentPlayer().stop();
		}
		chunkPositionClock = null;
		
		playNextChunkTimer.cancel();
		// calculate the offset time within this audio chunk
		final int offset = millis % timePerChunk;
		// new chunk start position
		int newPosition = millis - offset; 
		chunkPosition = offset;
		if (newPosition == position) {  //within the same chunk, no need to get it again
			if (isPlaying) {
				play(offset, false);
			}
		} else {
			position = newPosition;
			// get audio chunk needed for this time position
			// we need to add the overlap time, so that previous chunk won't pass the search criteria for fetching
			fetchChunksForNextPlayer(position + chunkOverlapTime, numChunksPreload, timePerChunk, null,
					new BufferSourceNode.BufferReadyListener() {
						@Override
						public void onBufferReady(Buffer b) {
							playerManager.moveToNextPlayer();
							if (isPlaying) {
								play(offset, false);
							}
						}
					});
		}
	}
	
	/**
	 * Gets current playtime position of the entire audio file.
	 * @return playtime position (milliseconds)
	 */
	public int getPosition() {
		int currentPosition = position + getChunkPosition();
		return Math.min(currentPosition, this.getDuration());
	}
	
	public void setVolume(double volume) {
		this.volume = volume;
		if(playerManager.getCurrentPlayer() == null) {
			logger.severe("CURRENT PLAYER IS NULL");
			return;
		}
		// TODO: some reason wasn't working when I used getCurrentPlayer().setVolume(volume);
		for (BufferPlayer p : playerManager.getPlayers()) {
			if (p != null) {
				p.setVolume(volume);
			}
		}
	}

	public void setVolume(double volume, int channel) {
		if(playerManager.getCurrentPlayer() == null) {
			logger.severe("CURRENT PLAYER IS NULL");
			return;
		}
		channelVolumes.put(channel, volume);
		// TODO: some reason wasn't working when I used getCurrentPlayer().setVolume(volume);
		for (BufferPlayer p : playerManager.getPlayers()) {
			if (p != null) {
				p.setVolume(volume, channel);
			}
		}
	}
	
	public double getVolume() {
		return volume;
	}

	public HashMap<Integer, Double> getChannelVolumes() {
		return channelVolumes;
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		// stop from any division by 0 errors
		if (playbackSpeed <= 0) {
			logger.severe("playback speed must be greater than 0");
			return;
		}
		boolean isPlaying = playerManager.getCurrentPlayer().isPlaying();
		// calculate the position in the chunk based on elapsed time and current playback speed
		if (isPlaying) {
			chunkPosition += chunkPositionClock.elapsedMillis() * this.playbackSpeed;
			chunkPositionClock = null;
			chunkPositionClock = new Duration();
		}
		// update playback speeds
		this.playbackSpeed = playbackSpeed;
		for (BufferPlayer p : playerManager.getPlayers()) {
			if (p != null) {
				p.setPlaybackSpeed(playbackSpeed);
			}
		}
		if (isPlaying) {
			playNextChunkTimer.cancel();
			scheduleNextChunk();
		}
	}
	
	public double getPlaybackSpeed() {
		return playbackSpeed;
	}

	public void setBalance(double balance) {
		this.balance = balance;
		if(playerManager.getCurrentPlayer() == null) {
			logger.severe("current player is null");
			return;
		}
		playerManager.getCurrentPlayer().setBalance(balance);
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void addEffect(Effect effect) {
		logger.info(LogUtils.prefix("AudioStreamPlayer add effect " + effect.getID()));
		effects.add(effect);
		connectEffectNodes(effects);
		BufferPlayer currentPlayer = playerManager.getCurrentPlayer();
		if (currentPlayer != null) {
			connectBufferPlayerToEffectChain(currentPlayer, effects);
		}
	}
	
	public void removeEffect(Effect effect) {
		logger.info(LogUtils.prefix("AudioStreamPlayer removing effect " + effect.getID()));
		effects.remove(effect);
		connectEffectNodes(effects);
		BufferPlayer currentPlayer = playerManager.getCurrentPlayer();
		if (currentPlayer != null) {
			connectBufferPlayerToEffectChain(currentPlayer, effects);
		}
	}
	
	public void setEffects(List<Effect> effects) {
		logger.info(LogUtils.prefix("AudioStreamPlayer adding effects"));
		this.effects.clear();
		if (effects != null) {
			this.effects.addAll(effects);
		}
		connectEffectNodes(effects);
		BufferPlayer currentPlayer = playerManager.getCurrentPlayer();
		if (currentPlayer != null) {
			connectBufferPlayerToEffectChain(currentPlayer, effects);
		}
		
	}
	
	public List<Effect> getEffects() {
		return effects;
	}

	/**
	 * Connects a BufferPlayer to the current set of effects by connecting the
	 * BufferPlayer's source to the effects chain and then outputs to the BufferPlayer's
	 * output node.
	 *
	 * BufferPlayer.source -> [Effect Chain] -> BufferPlayer.output
	 *
	 * @param player
	 * @param effects
	 */
	private void connectBufferPlayerToEffectChain(BufferPlayer player, List<Effect> effects) {
		// TODO: BufferPlayer already has its own node chain, so we need to somehow inject our effects chain into it
//		AudioNode source = player.getSourceNode();
//		AudioNode output = player.getOutput();
//		source.disconnect();
//		source.connect(output);
//		if (effects.size() > 0) {
//			AudioNode firstEffect = effects.get(0).getAudioNode();
//			AudioNode lastEffect = effects.get(effects.size()-1).getAudioNode();
//			logger.info(LogUtils.prefix("connecting source -> first effect: " + firstEffect.toString()));
//			source.connect(firstEffect);
//			lastEffect.connect(output);
//		} else {
//			logger.info(LogUtils.prefix("connecting source -> output"));
//			source.connect(output);
//		}
	}

	private void disconnectEffectChain(BufferPlayer player, List<Effect> effects) {
		if (effects.size() > 0) {
			AudioNode firstEffect = effects.get(0).getAudioNode();
			AudioNode lastEffect = effects.get(effects.size()-1).getAudioNode();
			lastEffect.disconnect();
			if (player != null) {
				AudioNode source = player.getSourceNode();
				AudioNode output = player.getOutput();
				source.disconnect(firstEffect);
				lastEffect.disconnect(output);
			}
		}
	}
	
	/**
	 * Goes thru list of effects and connects their corresponding AudioNode's 
	 * to enable us to connect the effects chain in between our source and
	 * destination nodes.
	 */
	private void connectEffectNodes(List<Effect> effects) {
		// TODO: optimize by not completely rebuilding unless neccessary
		String msg = "";
		AudioNode prev = null;
		AudioNode current = null;
		for (Effect effect : effects) {
			prev = current;
			current = effect.getAudioNode();
			// disconnect any previous connections
			if (current != null) {
				current.disconnect();
				msg += current.toString() + " -> ";
				// connect two nodes
				if (prev != null) {
					prev.connect(current);
				}
			}
		}
	}
	
	public String toString() {
		return "AudioStreamPlayer";
	}
}
