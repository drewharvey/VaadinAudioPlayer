package com.vaadin.addon.audio.client;

import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioBufferSourceNode;
import elemental.html.AudioContext;
import elemental.html.AudioNode;
import elemental.html.AudioParam;

/**
 * Actual player component, plays a single Buffer.
 * 
 * We're using multiple players (two, ideally) to be able to
 * quickly change from one chunk to the next without experiencing
 * possible lag as the data buffers are swapped.
 */
public class BufferPlayer {

	private Set<Effect> effects;
	private AudioBufferSourceNode source;
	private AudioNode output;
	private Buffer buffer;
	
	private double speed;
	private double volume;
	private boolean dirty;
	
	public BufferPlayer(AudioContext context, Buffer buffer) {
		Log.message(this, "create");
		effects = new LinkedHashSet<Effect>();
		this.buffer = buffer;
		source = context.createBufferSource();
		source.setBuffer(buffer.getAudioBuffer());
		output = context.createGainNode();
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
			for(Effect e : effects) {
				// TODO: Connect from current to next
				
				
				current = e.getAudioNode();
			}
			
			// TODO: Connect to current to output
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
	
	public void setSpeed(double speed_scale) {
		Log.message(this, "set speed scale " + speed_scale);
		speed = speed_scale;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void addEffect(Effect effect) {
		Log.message(this, "add effect " + effect);
		effect.setPlayer(this);
		effects.add(effect);
	}

	public void removeEffect(Effect effect) {
		Log.message(this, "remove effect " + effect);
		effects.remove(effect);
	}
	
}
