package com.vaadin.addon.audio.client;

import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.webaudio.AudioNode;
import com.vaadin.addon.audio.client.webaudio.BiquadFilterNode;
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
	
	private static Logger logger = Logger.getLogger("BufferPlayer");
	
	private enum State {
		PLAYING,
		STOPPED
	}

	//private BalanceEffect balanceEffect;
	
	private BufferSourceNode source;
	private GainNode output;
	private State state = State.STOPPED;
	

	public BufferPlayer() {
		this(null);
	}
	
	public BufferPlayer(Buffer buffer) {
		//logger.log(Level.SEVERE, "create");
		Context context = Context.get();
		source = context.createBufferSourceNode();
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
		// create output audio node
		output = context.createGainNode();
		output.connect(context.getDestination());
	}
	
	public AudioNode getSourceNode() {
		return source;
	}
	
	public AudioNode getOutput() {
		return output;
	}
	
	public void play(int offset_millis) {
		logger.log(Level.SEVERE, "start playback at " + offset_millis);
		logger.log(Level.SEVERE, " ==== PLAY CALLED ==== ");
		if (state == State.PLAYING) {
			stop();
		}
		double offsetSeconds = offset_millis / 1000d;
		source.start(0, offsetSeconds);
		
		state = State.PLAYING;
	}
	
	public void stop() {
		//logger.log(Level.SEVERE, "stop playback");
		if (state == State.STOPPED) {
			return;
		}
		Buffer buffer = source.getBuffer();
		source.stop();
		// source nodes can only be played once, so we need to 
		// create a new audio node and prep it to be played
		resetSourceNode(buffer);
		state = State.STOPPED;
	}

	private void resetSourceNode(Buffer buffer) {
		source.disconnect();
		source.resetNode();
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
	}
	
	public void setBuffer(Buffer buffer) {
		setBuffer(buffer, null);
	}
	
	public void setBuffer(Buffer buffer, BufferSourceNode.BufferReadyListener cb) {
		//logger.log(Level.SEVERE, "buffer reassigned");
		source.setBuffer(buffer, cb);
	}
	
	public Buffer getBuffer() {
		return source.getBuffer();
	}
	
	public void setVolume(double volume) {
		// logger.log(Level.SEVERE, "set volume to " + volume);
		output.setGain(volume);
	}
	
	public double getVolume() {
		return output.getGain();
	}
	
	public void setPlaybackSpeed(double speed_scale) {
		logger.log(Level.SEVERE, "set speed scale " + speed_scale);
		source.setPlaybackRate(speed_scale);
		
		// TODO: keep normal pitch if speed is changed, normally the pitch also changes
		// when the speed is changed
	}
	
	public double getPlaybackSpeed() {
		return source.getPlaybackRate();
	}
	
	public void setBalance(double balance) {
//		balanceEffect.setBalance(balance);
	}
	
	public double getBalance() {
		//return balanceEffect.getBalance();
		return 0;
	}
	
	public String toString() {
		return "BufferPlayer";
	}
	
}
