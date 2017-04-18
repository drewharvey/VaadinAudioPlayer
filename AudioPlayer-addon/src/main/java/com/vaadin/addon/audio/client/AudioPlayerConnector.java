package com.vaadin.addon.audio.client;

import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.effects.FilterEffect;
import com.vaadin.addon.audio.client.effects.PitchEffect;
import com.vaadin.addon.audio.client.effects.VolumeEffect;
import com.vaadin.addon.audio.client.webaudio.Buffer;
import com.vaadin.addon.audio.server.AudioPlayer;
import com.vaadin.addon.audio.shared.AudioPlayerClientRpc;
import com.vaadin.addon.audio.shared.AudioPlayerServerRpc;
import com.vaadin.addon.audio.shared.AudioPlayerState;
import com.vaadin.addon.audio.shared.SharedEffect;
import com.vaadin.addon.audio.shared.SharedEffect.EffectName;
import com.vaadin.addon.audio.shared.util.Log;
import com.vaadin.annotations.JavaScript;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

import elemental.html.AudioContext;

//
// TODO: get the JavaScript inflate library to load - this is required
// in order to get the client to receive compressed audio chunks
//

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(AudioPlayer.class)
@SuppressWarnings("serial")
@JavaScript({ "pako_inflate.min.js" }) // TODO: get this to load!!!! (should it be on the server side?)
public class AudioPlayerConnector extends AbstractExtensionConnector {

	// For now, we're going with a simple singleton
	private static AudioContext context;
	
	private static int REPORT_POSITION_REPEAT_TIME = 500;
	
	private AudioStreamPlayer player;
	private ClientStream stream;
	
    public AudioPlayerConnector() {
    }
    
    @OnStateChange("chunks")
    private void updateChunks() {
    	 Log.message(this, "chunk table updated");
    	 
    	 // TODO: do we need to respond to this somehow? probably not
    	 
    }
    
    @OnStateChange("effects")
    private void updateEffects() {
    	Log.message(this, "shared state effects list changed");
    	// TODO: don't rebuild list every time
    	
    	// TODO: for now we return early; this method causes a NullPointerException
    	return;
    	
    	/*
		List<Effect> effects = new ArrayList<Effect>();
    	for (SharedEffect e : getState().effects) {
    		Log.message(this, "adding " + e.getName().name());
    		for (SharedEffectProperty prop : e.getProperties()) {
    			Log.message(this, prop.getProperty().name() + " : " + prop.getValue());
    		}
    		effects.add(getEffectFromSharedEffect(e));
    	}
    	player.setEffects(effects);
    	*/
    }
    
    private Effect getEffectFromSharedEffect(SharedEffect sharedEffect) {
    	// TODO: add properties to each effect
    	if (sharedEffect.getName() == EffectName.BalanceEffect) {
    		BalanceEffect effect = new BalanceEffect();
    		effect.setID(sharedEffect.getID());
    		return effect;
    	} else if (sharedEffect.getName() == EffectName.FilterEffect) {
    		FilterEffect effect = new FilterEffect(sharedEffect.getProperties());
    		effect.setID(sharedEffect.getID());
    		return effect;
    	} else if (sharedEffect.getName() == EffectName.PitchEffect) {
    		PitchEffect effect = new PitchEffect();
    		effect.setID(sharedEffect.getID());
    		return effect;
    	} else if (sharedEffect.getName() == EffectName.VolumeEffect) {
    		VolumeEffect effect = new VolumeEffect();
    		effect.setID(sharedEffect.getID());
    		return effect;
    	}
    	return null;
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
			public void sendData(int chunkId, boolean compressed, String data) {
				Log.message(AudioPlayerConnector.this, "data available for chunk " + chunkId);
				stream.notifyChunkLoaded(chunkId, new Buffer(data, compressed));
			}

			@Override
			public void setPlaybackPosition(int position_millis) {
				Log.message(AudioPlayerConnector.this, "set playback position to " + position_millis);
				position_millis = (position_millis < 0) ? 0 : position_millis;
				player.setPosition(position_millis);
				getServerRPC().reportPlaybackPosition(position_millis);
			}
			
			@Override
			public void skipPosition(int delta_millis) {
				Log.message(AudioPlayerConnector.this, "skip by " + delta_millis);
				setPlaybackPosition(player.getPosition() + delta_millis);
			}

			@Override
			public void startPlayback() {
				Log.message(AudioPlayerConnector.this, "start playback");
				player.play();
				getServerRPC().reportPlaybackStarted();
				getServerRPC().reportPlaybackPosition(player.getPosition());
			}

			@Override
			public void pausePlayback() {
				Log.message(AudioPlayerConnector.this, "pause playback");
				player.pause();
				getServerRPC().reportPlaybackPaused();
			}

			@Override
			public void resumePlayback() {
				Log.message(AudioPlayerConnector.this, "resume playback");
				player.resume();
				getServerRPC().reportPlaybackStarted();
			}

			@Override
			public void stopPlayback() {
				Log.message(AudioPlayerConnector.this, "stop playback");
				player.stop();
				getServerRPC().reportPlaybackStopped();
			}

			@Override
			public void setPlaybackSpeed(double speed_multiplier) {
				Log.message(AudioPlayerConnector.this, "set playback speed to " + speed_multiplier);
				player.setPlaybackSpeed(speed_multiplier);
			}
			
			@Override
			public void setVolume(double volume) {
				Log.message(AudioPlayerConnector.this, "set volume to " + volume);
				player.setVolume(volume);
			}
			
			@Override
			public void setBalance(double balance) {
				Log.message(AudioPlayerConnector.this, "set balance to " + balance);
				player.setBalance(balance);
			}

        });
        
    	stream = new ClientStream(this);
    	player = new AudioStreamPlayer(stream);
    	
    	Timer reportPositionTimer = new Timer() {
			@Override
			public void run() {
				getServerRPC().reportPlaybackPosition(player.getPosition());
			}
    	};
    	reportPositionTimer.scheduleRepeating(REPORT_POSITION_REPEAT_TIME);
	}
	
	public String toString() {
		return "AudioPlayerConnector";
	}

}
