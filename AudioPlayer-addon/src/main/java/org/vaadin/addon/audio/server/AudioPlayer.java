package org.vaadin.addon.audio.server;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addon.audio.server.state.VolumeChangeCallback;
import org.vaadin.addon.audio.server.util.StringFormatter;
import org.vaadin.addon.audio.server.state.PlaybackState;
import org.vaadin.addon.audio.server.state.StateChangeCallback;
import org.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import org.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import org.vaadin.addon.audio.shared.AudioPlayerState;
import org.vaadin.addon.audio.shared.ChunkDescriptor;
import org.vaadin.addon.audio.shared.SharedEffect;
import org.vaadin.addon.audio.shared.util.Log;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

// This is the server-side UI component that provides public API for AudioPlayer
// pako_inflate.min.js is used for compression
// jungle.js is to supply audio node that can pitch shift
@SuppressWarnings("serial")
@JavaScript({ "pako_inflate.min.js", "jungle.js" })
public class AudioPlayer extends AbstractExtension {
	
	private UI ui = null;
	private Stream stream = null;
	private PlaybackState playbackState = PlaybackState.STOPPED;
	private int currentPosition = 0;
	private double volume = 1;
	private double[] channelVolumes = new double[0];

	// TODO: use a proper event system
	private List<StateChangeCallback> stateCallbacks = new ArrayList<>();
	private List<VolumeChangeCallback> volumeCallbacks = new ArrayList<>();

	/**
	 * Create new AudioPlayer 
	 * 
	 * @param stream Stream to use
	 */
    public AudioPlayer(Stream stream) {
		setupAudioPlayer(stream);
    }
    
    /**
     * Create new AudioPlayer
     * 
     * @param stream Stream to use
     * @param reportPositionRepeatTime Define the interval for position reporting, default 500ms
     */
	public AudioPlayer(Stream stream, int reportPositionRepeatTime) {
		getState().reportPositionRepeatTime = reportPositionRepeatTime;
		setupAudioPlayer(stream);
	}
	
    private void setupAudioPlayer(Stream stream) {
    	
    	registerRpc(new AudioPlayerServerRpc() {
			@Override
			public void requestChunk(final int chunkID) {
				Log.message(AudioPlayer.this,"received request for chunk " + chunkID);
				
				final UI ui = UI.getCurrent();
				final AudioPlayer player = AudioPlayer.this;
				
				Stream.Callback onComplete = new Stream.Callback() {
					@Override
					public void onComplete(String encodedData) {
						ui.access(new Runnable() {
							@Override
							public void run() {
								player.getClientRPC().sendData(chunkID, stream.isCompressionEnabled(), encodedData);
								Log.message(AudioPlayer.this, "sent chunk " + chunkID);
							}
						});
					}
				};
				stream.getChunkData(stream.getChunkById(chunkID), onComplete);
			}
			
			@Override
			public void reportPlaybackPosition(int position_millis) {
				// Log.message(AudioPlayer.this,"received position report: " + position_millis);
				if (position_millis != currentPosition) {
					currentPosition = position_millis;
					for (StateChangeCallback cb : stateCallbacks) {
						cb.playbackPositionChanged(position_millis);
					}
				}
			}

			@Override
			public void reportPlaybackStarted() {
				Log.message(AudioPlayer.this,"received playback state change to PLAYING");
				playbackState = PlaybackState.PLAYING;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackStateChanged(playbackState);
				}
			}

			@Override
			public void reportPlaybackPaused() {
				Log.message(AudioPlayer.this,"received playback state change to PAUSED");
				playbackState = PlaybackState.PAUSED;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackStateChanged(playbackState);
				}
			}

			@Override
			public void reportPlaybackStopped() {
				Log.message(AudioPlayer.this,"received playback state change to STOPPED");
				playbackState = PlaybackState.STOPPED;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackStateChanged(playbackState);
				}
			}

			@Override
			public void reportVolumeChange(double volume, double[] channelVolumes) {
				Log.message(AudioPlayer.this, "volume change reported from client");
				AudioPlayer.this.volume = volume;
				AudioPlayer.this.channelVolumes = channelVolumes;
				for (VolumeChangeCallback cb : volumeCallbacks) {
					cb.onVolumeChange(volume, channelVolumes);
				}
			}
		}, AudioPlayerServerRpc.class);

    	// Register stream, set up chunk table in state
    	setStream(stream);
    	
    	// Extend current UI
    	ui = UI.getCurrent();
    	extend(ui);
    }
    
    public void destroy() {
    	ui.removeExtension(this);
    }

	/**
	 * Gets Stream object that supplies audio data to this AudioPlayer.
	 * @return Stream
	 */
	public Stream getStream() {
    	return stream;
    }
    
    public Stream setStream(Stream stream) {
    	if(this.stream != null) {
    		getState().chunks.clear();
    	}
    	this.stream = stream;
    	getState().chunks.addAll(stream.getChunks());
    	getState().duration = stream.getDuration();
		getState().chunkTimeMillis = stream.getChunkLength();
    	return stream;
    }

	/**
	 * Gets current audio files total duration in milliseconds.
	 * @return int milliseconds
	 */
	public int getDuration() {
    	return stream.getDuration();
    }
    
    /**
     * Gets current audio players time position.
     * @return int milliseconds
     */
    public int getPosition() {
    	return currentPosition;
    }

    public void setPosition(int millis) {
    	currentPosition = millis;
    	getClientRPC().setPlaybackPosition(millis);
    	Log.message(AudioPlayer.this,"set playback position: " + millis);
    }

	/**
	 * Moves play position by milliseconds.
	 * @param millis number of milliseconds to move
	 */
	public void skip(int millis) {
    	getClientRPC().skipPosition(millis);
    	Log.message(AudioPlayer.this,"skip " + millis + " milliseconds");
    }

	/**
	 * Starts playing audio from the beginning of the audio file.
	 */
	public void play() {
    	getClientRPC().startPlayback();
    	Log.message(AudioPlayer.this,"start or restart playback");
    }

	/**
	 * Starts playing audio from the specified position (milliseconds).
	 * NOT IMPLEMENTED.
	 * @param offset_millis start position in milliseconds
	 */
	public void play(int offset_millis) {
    	getClientRPC().setPlaybackPosition(offset_millis);
    	getClientRPC().startPlayback();
    	Log.message(AudioPlayer.this,"start playback at time offset");
    }

	/**
	 * Pauses the current audio.
	 */
	public void pause() {
    	getClientRPC().pausePlayback();
    	Log.message(AudioPlayer.this,"pause playback");
    }

	/**
	 * Plays audio from last known position (usually used to play while paused).
	 */
	public void resume() {
		getClientRPC().resumePlayback();;
    	Log.message(AudioPlayer.this,"resume playback");
    }

	/**
	 * Stops playing the audio and resets the position to 0 (beginning of audio file).
	 */
	public void stop() {
    	getClientRPC().stopPlayback();
    	Log.message(AudioPlayer.this,"stop playback");
    }

	public boolean isPlaying() {
		return playbackState == PlaybackState.PLAYING;
    }

	public boolean isPaused() {
		return playbackState == PlaybackState.PAUSED;
	}

	public boolean isStopped() {
		return playbackState == PlaybackState.STOPPED;
	}

	/**
	 * Sets the volume of the audio player. 1 is 100% volume (default), 2 is 200% volume, etc.
	 * @param volume volume level
	 */
	public void setVolume(double volume) {
		getClientRPC().setVolume(volume);
		Log.message(AudioPlayer.this,"setting volume to " + volume);
	}

	public void setVolumeOnChannel(double volume, int channel) {
		getClientRPC().setVolumeOnChannel(volume, channel);
		Log.message(AudioPlayer.this, "setting volume to " + volume + " on channel " + channel);

	}

	public double getVolume() {
		return volume;
	}

	public double getVolumeOnChannel(int channel) {
		if (channelVolumes.length > channel) {
			return channelVolumes[channel];
		}
		return -1;
	}

	public double getNumberOfChannels() {
		return channelVolumes.length;
	}

	/**
	 * Sets the speed at which the audio is played.  Changing this will not change
	 * the pitch of the audio.  1 is 100% speed (default), 2 is 200%, etc.
	 * @param playbackSpeed speed ratio
	 */
	public void setPlaybackSpeed(double playbackSpeed) {
		getClientRPC().setPlaybackSpeed(playbackSpeed);
		Log.message(AudioPlayer.this,"setting playback speed to " + playbackSpeed);
	}

	/**
	 * Sets the spread of total gain (volume) between the left and right channels.
	 * -1 is to only play left channel.
	 * 0 is to play equally left and right channels (default).
	 * 1 is to only play right channel.
	 * @param balance
	 */
	public void setBalance(double balance) {
		getClientRPC().setBalance(balance);
	}

	/**
	 * Sets the number of audio chunks that are loaded ahead of the current playing audio chunk.
	 * @param numChunksPreload
	 */
	public void setNumberChunksToPreload(int numChunksPreload) {
		getState().numChunksPreload = numChunksPreload;
	}

	/**
	 * Gets number of chunks to load each time audio chunks are requested.
	 * @return number of chunks
	 */
	public int getNumberChunksToPreload() {
		return getState().numChunksPreload;
	}

	protected ChunkDescriptor getChunkDescriptor(int chunkId) {
		// TODO: return chunk descriptor
		return null;
	}

	/**
	 * Gets String representing current player time position.
	 * @return String
	 */
	public String getPositionString() {
		return StringFormatter.msToPlayerTimeStamp(getPosition());
	}

	/**
	 * Gets String representing current player's total time duration.
	 * @return String
	 */
	public String getDurationString() {
		return StringFormatter.msToPlayerTimeStamp(getDuration());
	}

	//=========================================================================
	//=== Effects =============================================================
	//=========================================================================

	/**
	 * Add effect immediately to the audio player.
	 * @param effect Effect to add
	 */
	public void addEffect(Effect effect) {
		// TODO: update effect if it already exists
		getState().effects.add(effect.getSharedEffectObject());
	}

	/**
	 * Removes effect immediately from audio player.
	 * @param effect Effect to remove
	 */
	public void removeEffect(Effect effect) {
		// TODO: optimize removing effects so we don't have to loop
		for (SharedEffect e : getState().effects) {
			if (effect.getID().equals(e.getID())) {
				Log.message(AudioPlayer.this,"removing effect: " + e.getName().name());
				getState().effects.remove(e);
			}
		}
	}

	/**
	 * Updates properties of the effect and passes the changes to the client side.
	 * @param effect Effect to update
	 */
	public void updateEffect(Effect effect) {
		for (SharedEffect e : getState().effects) {
			if (effect.getID().equals(e.getID())) {
				Log.message(AudioPlayer.this,"updating effect: " + e.getName().name());
				e.setProperties(effect.getSharedEffectObject().getProperties());
			}
		}
	}

	//=========================================================================
	//=== Listeners ===========================================================
	//=========================================================================

	public void addStateChangeListener(StateChangeCallback cb) {
		stateCallbacks.add(cb);
	}

	public void removeStateChangeListener(StateChangeCallback cb) {
		stateCallbacks.remove(cb);
	}

	public void addValueChangeListener(VolumeChangeCallback cb) {
		volumeCallbacks.add(cb);
	}

	public void removeValueChangeListener(VolumeChangeCallback cb) {
		volumeCallbacks.remove(cb);
	}

	//=========================================================================
	//=========================================================================
	//=========================================================================
	
    @Override
    protected AudioPlayerState getState() {
        return (AudioPlayerState) super.getState();
    }
    
    private AudioPlayerClientRpc getClientRPC() {
    	return getRpcProxy(AudioPlayerClientRpc.class);
    }

}
