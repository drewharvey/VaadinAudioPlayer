package com.vaadin.addon.audio.server;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import com.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import com.vaadin.addon.audio.shared.AudioPlayerState;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.addon.audio.shared.util.Log;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

// This is the server-side UI component that provides public API 
// for AudioPlayer
@SuppressWarnings("serial")
@JavaScript({ "pako_inflate.min.js", "jungle.js" })
public class AudioPlayer extends AbstractExtension {

	// TODO: use an actual event system
	public static interface StateChangeCallback {
		
		void playbackPositionChanged(int new_position_millis);
		
		void playbackStateChanged(PlaybackState new_state);
		
	}

	public static enum PlaybackState {
		PLAYING,
		PAUSED,
		STOPPED
	}
	
	private UI ui = null;
	private Stream stream = null;
	private PlaybackState playbackState = PlaybackState.STOPPED;
	private int currentPosition = 0;
	
    public AudioPlayer(Stream stream) {
    	
    	registerRpc(new AudioPlayerServerRpc() {
			@Override
			public void requestChunk(final int chunkID) {
				// Log.message(AudioPlayer.this,"received request for chunk " + chunkID);
				
				final UI ui = UI.getCurrent();
				final AudioPlayer player = AudioPlayer.this;
				
				Stream.Callback onComplete = new Stream.Callback() {
					@Override
					public void onComplete(String encodedData) {
						ui.access(new Runnable() {
							@Override
							public void run() {
								player.getClientRPC().sendData(chunkID, stream.isCompressionEnabled(), encodedData);
								// Log.message(AudioPlayer.this, "sent chunk " + chunkID);
							}
						});
					}
				};
				stream.getChunkData(stream.getChunkById(chunkID), onComplete);
			}
			
			@Override
			public void reportPlaybackPosition(int position_millis) {
				// Log.message(AudioPlayer.this,"received position report: " + position_millis);
				currentPosition = position_millis;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackPositionChanged(position_millis);
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
		}, AudioPlayerServerRpc.class);

    	// Register stream, set up chunk table in state
    	setStream(stream);
    	
    	// Extend current UI
    	ui = UI.getCurrent();
    	extend(ui);
    }
    
    // TODO: use a proper event system
    private List<StateChangeCallback> stateCallbacks = new
    		ArrayList<>();
    public void addStateChangeListener(StateChangeCallback cb) {
    	stateCallbacks.add(cb);
    }
    
    public void removeStateChangeListener(StateChangeCallback cb) {
    	stateCallbacks.remove(cb);
    }
    
    public void destroy() {
    	ui.removeExtension(this);
    }

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
    
    public void skip(int millis) {
    	getClientRPC().skipPosition(millis);
    	Log.message(AudioPlayer.this,"skip " + millis + " milliseconds");
    }
    
    public void play() {
    	getClientRPC().startPlayback();
    	Log.message(AudioPlayer.this,"start or restart playback");
    }
    
    public void play(int offset_millis) {
    	// TODO: re-enable
    	//getClientRPC().setPlaybackPosition(offset_millis);
    	getClientRPC().startPlayback();
    	Log.message(AudioPlayer.this,"start playback at time offset");
    }
    
    public void pause() {
    	getClientRPC().pausePlayback();
    	Log.message(AudioPlayer.this,"pause playback");
    }
    
    public void resume() {
    	getClientRPC().resumePlayback();
    	Log.message(AudioPlayer.this,"resume playback");
    }
    
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
	
	public void setVolume(double volume) {
		getClientRPC().setVolume(volume);
		Log.message(AudioPlayer.this,"setting volume to " + volume);
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		getClientRPC().setPlaybackSpeed(playbackSpeed);
		Log.message(AudioPlayer.this,"setting playback speed to " + playbackSpeed);
	}
	
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
	
	public void addEffect(Effect effect) {
		// TODO: update effect if it already exists
		getState().effects.add(effect.getSharedEffectObject());
	}
	
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
	 * @param effect
	 */
	public void updateEffect(Effect effect) {
		for (SharedEffect e : getState().effects) {
			if (effect.getID().equals(e.getID())) {
				Log.message(AudioPlayer.this,"updating effect: " + e.getName().name());
				e.setProperties(effect.getSharedEffectObject().getProperties());
			}
		}
	}

	protected ChunkDescriptor getChunkDescriptor(int chunkId) {
		// TODO: return chunk descriptor
		return null;
	}
	
	public String getPositionString() {
		long second = (getPosition() / 1000) % 60;
		long minute = (getPosition() / (1000 * 60)) % 60;
		long hour = (getPosition() / (1000 * 60 * 60)) % 24;
		
		long durationHours = (getDuration() / (1000 * 60 * 60)) % 24;
		
		if (durationHours > 0) {
			return String.format("%02d:%02d:%02d", hour, minute, second);
		} else {
			return String.format("%02d:%02d", minute, second);
		}
	}
	
	public String getDurationString() {
		long second = (getDuration() / 1000) % 60;
		long minute = (getDuration() / (1000 * 60)) % 60;
		long hour = (getDuration() / (1000 * 60 * 60)) % 24;
		
		if (hour > 0) {
			return String.format("%02d:%02d:%02d", hour, minute, second);
		} else {
			return String.format("%02d:%02d", minute, second);
		}
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
