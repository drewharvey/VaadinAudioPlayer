package com.vaadin.addon.audio.server;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import com.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import com.vaadin.addon.audio.shared.AudioPlayerState;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

// This is the server-side UI component that provides public API 
// for AudioPlayer
@SuppressWarnings("serial")
public class AudioPlayer extends AbstractExtension {
	
	// TODO: use an actual event system
	public static interface StateChangeCallback {
		
		void playbackPositionChanged(int new_position_millis);
		
		void playbackStateChanged(PlaybackState new_state);
		
	}

	private static void trace(String msg) {
		System.err.println("[AudioPlayer] " + msg + " (REMOVEME)");
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
				trace("received request for chunk " + chunkID);
				
				final UI ui = UI.getCurrent();
				final AudioPlayer player = AudioPlayer.this;
				
				Stream.Callback onComplete = new Stream.Callback() {
					@Override
					public void onComplete(String encodedData) {
						ui.access(new Runnable() {
							@Override
							public void run() {
								player.getClientRPC().sendData(chunkID, stream.isCompressionEnabled(), encodedData);
								trace("sent chunk " + chunkID);
							}
						});
					}
				};
				stream.getChunkData(stream.getChunkById(chunkID), onComplete);
			}
			
			@Override
			public void reportPlaybackPosition(int position_millis) {
				trace("received position report: " + position_millis);
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackPositionChanged(position_millis);
				}
			}

			@Override
			public void reportPlaybackStarted() {
				trace("received playback state change to PLAYING");
				playbackState = PlaybackState.PLAYING;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackStateChanged(playbackState);
				}
			}

			@Override
			public void reportPlaybackPaused() {
				trace("received playback state change to PAUSED");
				playbackState = PlaybackState.PAUSED;
				for(StateChangeCallback cb : stateCallbacks) {
					cb.playbackStateChanged(playbackState);
				}
			}

			@Override
			public void reportPlaybackStopped() {
				trace("received playback state change to STOPPED");
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
    	return stream;
    }
    
    public int getDuration() {
    	return stream.getDuration();
    }
    
    public int getPosition() {
    	return currentPosition;
    }

    public void setPosition(int millis) {
    	getClientRPC().setPlaybackPosition(millis);
    	trace("set playback position: " + millis);
    }
    
    public void skip(int millis) {
    	getClientRPC().skipPosition(millis);
    	trace("skip " + millis + " milliseconds");
    }
    
    public void play() {
    	getClientRPC().stopPlayback();
    	getClientRPC().startPlayback();
    	trace("start or restart playback");
    }
    
    public void play(int offset_millis) {
    	getClientRPC().stopPlayback();
    	getClientRPC().setPlaybackPosition(offset_millis);
    	getClientRPC().startPlayback();
    	trace("start playback at time offset");
    }
    
    public void pause() {
    	getClientRPC().pausePlayback();
    	trace("pause playback");
    }
    
    public void resume() {
    	getClientRPC().resumePlayback();
    	trace("resume playback");
    }
    
    public void stop() {
    	getClientRPC().stopPlayback();
    	trace("stop playback");
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
		trace("setting volume to " + volume);
	}
	
	public void setPlaybackSpeed(double playbackSpeed) {
		getClientRPC().setPlaybackSpeed(playbackSpeed);
		trace("setting playback speed to " + playbackSpeed);
	}
	
	public void setBalance(double balance) {
		getClientRPC().setBalance(balance);
	}
	
	public void addEffect(Effect effect) {
		// TODO: update effect if it already exists
		getState().effects.add(effect.getSharedEffectObject());
	}
	
	public void removeEffect(Effect effect) {
		// TODO: optimize removing effects so we don't have to loop
		getState().effects.remove(effect.getSharedEffectObject());
		for (SharedEffect e : getState().effects) {
			if (effect.getID().equals(e.getID())) {
				getState().effects.remove(e);
				trace("removing effect: " + e.getName().name());
			}
		}
	}

	protected ChunkDescriptor getChunkDescriptor(int chunkId) {
		// TODO: return chunk descriptor
		return null;
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
