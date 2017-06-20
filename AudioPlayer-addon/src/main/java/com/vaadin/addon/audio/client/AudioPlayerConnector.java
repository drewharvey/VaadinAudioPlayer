package com.vaadin.addon.audio.client;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.effects.FilterEffect;
import com.vaadin.addon.audio.client.effects.PitchEffect;
import com.vaadin.addon.audio.client.effects.VolumeEffect;
import com.vaadin.addon.audio.shared.util.LogUtils;
import com.vaadin.addon.audio.client.webaudio.Buffer;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.server.AudioPlayer;
import com.vaadin.addon.audio.shared.*;
import com.vaadin.addon.audio.shared.SharedEffect.EffectName;
import com.vaadin.addon.audio.shared.util.Log;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

import elemental.html.AudioContext;

// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(AudioPlayer.class)
@SuppressWarnings("serial")
public class AudioPlayerConnector extends AbstractExtensionConnector {

	// For now, we're going with a simple singleton
	private static AudioContext context;
	
	private final static int REPORT_POSITION_REPEAT_TIME = 500;
	private static int lastPlaybackPosition = 0;
	
	private AudioStreamPlayer player;
	private ClientStream stream;
	private HashMap<String, Effect> effectsMap = new HashMap<String, Effect>();
	
    public AudioPlayerConnector() {
    }
    
    @OnStateChange("chunks")
    private void updateChunks() {
    	 Log.message(this, "chunk table updated");
    	 // TODO: do we need to respond to this somehow? probably not
    }

    @OnStateChange("chunkTimeMillis")
	private void updateChunkTime() {
		Log.message(this, "chunkTimeMillis updated");
	}

	@OnStateChange("duration")
	private void updateDuration() {
		Log.message(this, "duration updated");
	}

    @OnStateChange("numChunksPreload")
	private void updateNumChunksPreload() {
		Log.message(this, "numChunksPreload updated");
		player.setNumChunksPreload(getState().numChunksPreload);
	}
    
    @OnStateChange("effects")
    private void updateEffects() {
    	Log.message(this, "shared state effects list changed");
    	// TODO: don't rebuild list every time
    	List<SharedEffect> effects = getState().effects;
    	for (SharedEffect sharedEffect : effects) {
			Effect existingEffect = effectsMap.get(sharedEffect.getID());
			// check if the effect already exists
			if (existingEffect != null) {
				existingEffect.setProperties(sharedEffect.getProperties());
			} else {
				// if it doesn't exist, create it and try to store it
				existingEffect = getEffectFromSharedEffect(sharedEffect);
				if (existingEffect != null) {
					effectsMap.put(sharedEffect.getID(), existingEffect);
					player.addEffect(existingEffect);
				}
			}
		}
    }
    
    private Effect getEffectFromSharedEffect(SharedEffect sharedEffect) {
    	// TODO: add properties to each effect
    	if (sharedEffect.getName() == EffectName.BalanceEffect) {
    		BalanceEffect effect = new BalanceEffect();
    		effect.setID(sharedEffect.getID());
    		return effect;
    	} else if (sharedEffect.getName() == EffectName.FilterEffect) {
    		FilterEffect effect = new FilterEffect();
    		effect.init(Context.get());
    		effect.setID(sharedEffect.getID());
    		effect.setProperties(sharedEffect.getProperties());
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
			public void requestAndCacheAudioChunks(int startTime, int endTime) {
				Log.message(AudioPlayerConnector.this, "preloading audio chunks from time " + startTime + " - " + endTime);
				// get time per chunk
				int timePerChunk = getState().chunkTimeMillis;
				// loop thru and request whatever chunks needed within time frame
				for (int i = startTime; i < endTime; i += timePerChunk) {
					stream.requestChunkByTimestamp(i, new ClientStream.DataCallback() {
						@Override
						public void onDataReceived(ChunkDescriptor chunk) {
							// caching is handled automatically
						}
					});
				}
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
				getServerRPC().reportVolumeChange(player.getVolume(), player.getChannelVolumes());
			}

			@Override
			public void setVolumeOnChannel(double volume, int channel) {
				Log.message(AudioPlayerConnector.this, "set channel " + channel + " volume to " + volume);
				player.setVolume(volume, channel);
				getServerRPC().reportVolumeChange(player.getVolume(), player.getChannelVolumes());
			}
			
			@Override
			public void setBalance(double balance) {
				Log.message(AudioPlayerConnector.this, "set balance to " + balance);
				player.setBalance(balance);
			}
			
			@Override
			public void updateEffects(List<SharedEffect> effects) {
				for (SharedEffect sharedEffect : effects) {
					Effect existingEffect = effectsMap.get(sharedEffect.getID());
					// check if the effect already exists
					if (existingEffect != null) {
						existingEffect.setProperties(sharedEffect.getProperties());
					} else {
						// if it doesn't exist, create it and try to store it
						existingEffect = getEffectFromSharedEffect(sharedEffect);
						if (existingEffect != null) {
							effectsMap.put(sharedEffect.getID(), existingEffect);
							player.addEffect(existingEffect);
						}
					}
				}
			}

        });
        
        // create stream and the client side audio player
    	stream = new ClientStream(this);
    	player = new AudioStreamPlayer(stream, getState().chunkTimeMillis);
    	
    	// expose this audio player to the client side thru a custom js api
    	JavaScriptPublicAPI.exposeMethods();
    	JavaScriptPublicAPI.addAudioPlayerInstance(player);
    	
    	// create timer that reports playback to server
    	Timer reportPositionTimer = new Timer() {
			@Override
			public void run() {
				if (lastPlaybackPosition != player.getPosition()) {
					lastPlaybackPosition = player.getPosition();
					getServerRPC().reportPlaybackPosition(lastPlaybackPosition);
				}
			}
    	};
    	reportPositionTimer.scheduleRepeating(REPORT_POSITION_REPEAT_TIME);
	}

	@Override
	public void onUnregister() {
		if (player != null) {
			// TODO: test this once delete stream is implemented
			JavaScriptPublicAPI.removeAudioPlayerInstance(player);
		}
	}
	
	public String toString() {
		return "AudioPlayerConnector";
	}

}
