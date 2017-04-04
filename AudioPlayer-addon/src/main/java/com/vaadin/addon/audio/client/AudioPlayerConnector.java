package com.vaadin.addon.audio.client;

import com.vaadin.addon.audio.client.util.Log;
import com.vaadin.addon.audio.server.AudioPlayer;
import com.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import com.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import com.vaadin.addon.audio.shared.AudioPlayerState;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

import elemental.html.AudioContext;
import elemental.js.JsBrowser;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(AudioPlayer.class)
@SuppressWarnings("serial")
public class AudioPlayerConnector extends AbstractExtensionConnector {

	// For now, we're going with a simple singleton
	private static AudioContext context;
	public static AudioContext getContext() {
		if(context == null) {
			context = JsBrowser.getWindow().newAudioContext();
		}
		return context;
	}
	

	private AudioStreamPlayer player;
	private ClientStream stream;
	
    public AudioPlayerConnector() {
    	// TODO: don't init the stream here! Also the stream needs to be able to update
    	stream = new ClientStream(this, getState().chunks);
    	player = new AudioStreamPlayer(stream);
    }
    
    @OnStateChange("chunks")
    private void updateChunks() {
    	 
    }

    
    //=========================================================================
    //=========================================================================
    //=========================================================================
    
    @Override
    public AudioPlayerState getState() {
        return (AudioPlayerState) super.getState();
    }
    
    private AudioPlayerServerRpc rpc;
    public AudioPlayerServerRpc getServerRPC() {
    	if(rpc == null) {
    		 rpc = RpcProxy.create(AudioPlayerServerRpc.class, this);
    	}
    	return rpc;
    }

	@Override
	protected void extend(ServerConnector target) {
        // To receive RPC events from server, we register ClientRpc implementation 
        registerRpc(AudioPlayerClientRpc.class, new AudioPlayerClientRpc() {

			@Override
			public void dataAvailable(int chunkId) {
				Log.message(this, "data available for chunk " + chunkId);
				stream.notifyChunkLoaded(chunkId);
			}

			@Override
			public void setPlaybackPosition(int position_millis) {
				Log.message(this, "set playback position to " + position_millis);
				player.setPosition(position_millis);
				getServerRPC().reportPlaybackPosition(player.getPosition());
			}
			
			@Override
			public void skipPosition(int delta_millis) {
				Log.message(this, "skip by " + delta_millis);
				// TODO: do something with this
				setPlaybackPosition(player.getPosition() + delta_millis);
			}

			@Override
			public void startPlayback() {
				Log.message(this, "start playback");
				// TODO: do something with this
				getServerRPC().reportPlaybackStarted();
				getServerRPC().reportPlaybackPosition(player.getPosition());
			}

			@Override
			public void pausePlayback() {
				Log.message(this, "pause playback");
				// TODO: do something with this

				getServerRPC().reportPlaybackPaused();
			}

			@Override
			public void resumePlayback() {
				Log.message(this, "resume playback");
				// TODO: do something with this

				getServerRPC().reportPlaybackStarted();
			}

			@Override
			public void stopPlayback() {
				Log.message(this, "stop playback");
				// TODO: do something with this

				getServerRPC().reportPlaybackStopped();
			}

			@Override
			public void setPlaybackSpeed(double speed_multiplier) {
				Log.message(this, "set playback speed to " + speed_multiplier);
				player.setPlaybackSpeed(speed_multiplier);
			}
			
			@Override
			public void setVolume(double volume) {
				Log.message(this, "set volume to " + volume);
				player.setVolume(volume);
			}
			
			@Override
			public void setBalance(double balance) {
				Log.message(this, "set balance to " + balance);
				player.setBalance(balance);
			}

        });		
	}
}
