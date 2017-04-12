package com.vaadin.addon.audio.client;

import java.util.ArrayList;

import java.util.List;

import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.webaudio.AudioNode;
import com.vaadin.addon.audio.client.webaudio.Buffer;
import com.vaadin.addon.audio.client.webaudio.BufferSourceNode;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.client.webaudio.GainNode;
import com.vaadin.addon.audio.shared.util.Log;

/**
 * Actual player component, plays a single Buffer.
 * 
 * We're using multiple players (two, ideally) to be able to
 * quickly change from one chunk to the next without experiencing
 * possible lag as the data buffers are swapped.
 */
public class BufferPlayer {

	//private List<Effect> effects;
	//private BalanceEffect balanceEffect;
	
	private BufferSourceNode source;
	private GainNode output;
	
    private double speed;
	private double volume;
	
	//private boolean dirty;
	
	public BufferPlayer() {
		this(null);
	}
	
	public BufferPlayer(Buffer buffer) {
		Log.message(this, "create");
		
		Context context = Context.get();
		source = context.createBufferSourceNode();
		if(buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
		output = context.createGainNode();
	}
	
	public AudioNode getOutput() {
//		if(dirty) {
//			Log.message(this, "marked as dirty, reconfigure output");
//			// TODO: build audio processing chain
//			AudioNode current = source;
//			AudioNode prev = null;
//			for(Effect e : effects) {
//				
//				// TODO: uh.. is this the right order?
//				
//				prev = current;
//				current = e.getAudioNode();
//				prev.connect(current, 0, 0);
//			}
//			// connect predefined balance node
//			current.connect(balanceEffect.getAudioNode(), 0, 0);
//			current = balanceEffect.getAudioNode();
//			// connect predefined gain node
//			current.connect(output, 0, 0);
//			// do we need to connect to the context destination here or somewhere else?
//			output.connect(AudioPlayerConnector.getAudioContext().getDestination(), 0, 0);
//			dirty = false;
//		}
		return output;
	}

	public boolean isPlaying() {
		return false;
		//return source.getPlaybackState() == AudioBufferSourceNode.PLAYING_STATE;
	}
	
	public void play(int offset_millis) {
		Log.message(this, "start playback at " + offset_millis);
		
	}
	
	public void stop() {
		Log.message(this, "stop playback");
	}
	
	public void setBuffer(Buffer buffer) {
		Log.message(this, "buffer reassigned");
		stop();
		source.setBuffer(buffer);
	}
	
	public Buffer getBuffer() {
		return source.getBuffer();
	}
	
	public void setVolume(double volume) {
		Log.message(this, "set volume to " + volume);
		this.volume = volume;
	}
	
	public double getVolume() {
		return volume;
	}
	
	public void setPlaybackSpeed(double speed_scale) {
		Log.message(this, "set speed scale " + speed_scale);
		speed = speed_scale;
		
		// TODO: keep normal pitch if speed is changed, normally the pitch also changes
		// when the speed is changed
	}
	
	public double getPlaybackSpeed() {
		return speed;
	}
	
	public void setBalance(double balance) {
		//balanceEffect.setBalance(balance);
	}
	
	public double getBalance() {
		//return balanceEffect.getBalance();
		return 0;
	}
	
	/*
	 // TODO: get the following back into action 
	  * 
	public void setEffects(List<Effect> effects) {
		// TODO: only replace/remove effects based on if there are matching IDs
		this.effects.clear();
		this.effects.addAll(effects);
		for (Effect e : effects) {
			e.setPlayer(this);
			Log.message(this, "BufferPlayer adding effect " + e.getClass().getName());
		}
		dirty = true;
	}
	
	public void addEffect(Effect effect) {
		Log.message(this, "add effect " + effect);
		effect.setPlayer(this);
		effects.add(effect);
		dirty = true;
	}

	public void removeEffect(Effect effect) {
		Log.message(this, "remove effect " + effect);
		effects.remove(effect);
		dirty = true;
	}
	*/
	
	public String toString() {
		return "BufferPlayer";
	}
	
}
