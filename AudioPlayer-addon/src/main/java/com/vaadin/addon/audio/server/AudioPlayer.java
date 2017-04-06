package com.vaadin.addon.audio.server;

import com.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import com.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import com.vaadin.addon.audio.shared.AudioPlayerState;
import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

// This is the server-side UI component that provides public API 
// for AudioPlayer
@SuppressWarnings("serial")
public class AudioPlayer extends AbstractExtension {

	private static void trace(String msg) {
		System.err.println("[AudioPlayer] " + msg + " (REMOVEME)");
	}
	
	private enum PlaybackState {
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
			public void requestChunk(int chunkID) {
				
				trace("received request for chunk");
			}
			
			@Override
			public void reportPlaybackPosition(int position_millis) {
				
				trace("received position report: " + position_millis);
			}

			@Override
			public void reportPlaybackStarted() {
				playbackState = PlaybackState.PLAYING;
				trace("received playback state change to PLAYING");
			}

			@Override
			public void reportPlaybackPaused() {
				playbackState = PlaybackState.PAUSED;
				trace("received playback state change to PAUSED");
			}

			@Override
			public void reportPlaybackStopped() {
				playbackState = PlaybackState.STOPPED;
				trace("received playback state change to STOPPED");
			}
		}, AudioPlayerServerRpc.class);

    	// Register stream, set up chunk table in state
    	this.stream = stream;
    	
    	
    	// Extend current UI
    	ui = UI.getCurrent();
    	extend(ui);
    	
    }
    
    public void destroy() {
    	ui.removeExtension(this);
    }

    public Stream getStream() {
    	return stream;
    }
    
    public Stream setStream(Stream stream) {
    	
    	if(this.stream != null) {
    		
    	}
    	
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
