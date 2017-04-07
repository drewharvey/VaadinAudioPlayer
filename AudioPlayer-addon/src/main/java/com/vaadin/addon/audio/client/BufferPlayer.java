package com.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioBufferSourceNode;
import elemental.html.AudioContext;
import elemental.html.AudioGainNode;
import elemental.html.AudioNode;

/**
 * Actual player component, plays a single Buffer.
 * 
 * We're using multiple players (two, ideally) to be able to
 * quickly change from one chunk to the next without experiencing
 * possible lag as the data buffers are swapped.
 */
public class BufferPlayer {

	private List<Effect> effects;
	private AudioBufferSourceNode source;
	private AudioNode output;
	private BalanceEffect balanceEffect;
	private Buffer buffer;
	
	private double speed;
	private double volume;
	private boolean dirty;
	
	public BufferPlayer(AudioContext context, Buffer buffer) {
		Log.message(this, "create");
		effects = new ArrayList<Effect>();
		this.buffer = buffer;
		source = context.createBufferSource();
		source.setBuffer(buffer.getAudioBuffer());
		output = context.createGainNode();
		balanceEffect = new BalanceEffect();
		balanceEffect.init(context);
		dirty = true;
	}
	
	public AudioBufferSourceNode getSource() {
		return source;
	}
	
	public AudioNode getOutput() {
		if(dirty) {
			Log.message(this, "marked as dirty, reconfigure output");
			// TODO: build audio processing chain
			AudioNode current = source;
			AudioNode prev = null;
			for(Effect e : effects) {
				prev = current;
				current = e.getAudioNode();
				prev.connect(current, 0, 0);
			}
			// connect predefined balance node
			current.connect(balanceEffect.getAudioNode(), 0, 0);
			current = balanceEffect.getAudioNode();
			// connect predefined gain node
			current.connect(output, 0, 0);
			// do we need to connect to the context destination here or somewhere else?
			output.connect(AudioPlayerConnector.getContext().getDestination(), 0, 0);
			dirty = false;
		}
		return output;
	}
	
	public void setBuffer(Buffer buffer) {
		Log.message(this, "buffer reassigned");
		stop();
		this.buffer = buffer;
		// TODO: change this on the node
	}
	
	public Buffer getBuffer() {
		return buffer;
	}
	
	public void setVolume(double volume) {
		Log.message(this, "set volume to " + volume);
		this.volume = volume;
		((AudioGainNode) output).getGain().setValue((float) volume);
	}
	
	public double getVolume() {
		return volume;
	}
	
	public void play(int offset_millis) {
		Log.message(this, "start playback at " + offset_millis);
		
	}
	
	public void stop() {
		Log.message(this, "stop playback");
	}
	
	public void setPlaybackSpeed(double speed_scale) {
		Log.message(this, "set speed scale " + speed_scale);
		speed = speed_scale;
		source.getPlaybackRate().setValue((float) speed_scale);
		// TODO: keep normal pitch if speed is changed, normally the pitch also changes
		// when the speed is changed
	}
	
	public double getPlaybackSpeed() {
		return speed;
	}
	
	public void setBalance(double balance) {
		balanceEffect.setBalance(balance);
	}
	
	public double getBalance() {
		return balanceEffect.getBalance();
	}
	
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
	
}
