package com.vaadin.addon.audio.client;

import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
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
	
	private static Logger logger = Logger.getLogger("BufferPlayer");
	
	private enum State {
		PLAYING,
		STOPPED
	}

	//private List<Effect> effects;
	//private BalanceEffect balanceEffect;
	
	private BufferSourceNode source;
	private GainNode output;
	private State state = State.STOPPED;
	
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
		
		// XXX, TODO: rework this
		source.connect(output);
		output.connect(context.getDestination());
	}
	
	public void resetBufferPlayer(Buffer buffer) {
		Log.message(this, "reset");
		
		Context context = Context.get();
		source = context.createBufferSourceNode();
		if(buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
		output = context.createGainNode();
		
		// XXX, TODO: rework this
		source.connect(output);
		output.connect(context.getDestination());
	}
	
	// TODO: use crossfade curve instead of linear curve to keep volume consistant
	// TODO: handle fade in/out when paused
	public void fadeIn(int offsetMillis, int fadeInDuration) {
		if (fadeInDuration <= 0) {
			play(offsetMillis);
			return;
		}
		final double maxGain = output.getGain();
		final int changeInterval = 100;
		// get number of increases based on doing it every 100 ms
		int numGainChanges = fadeInDuration / changeInterval;
		if (fadeInDuration % 100 != 0) {
			numGainChanges++;
		}
		double gainPerChange = maxGain / numGainChanges;
		
		logger.log(Level.SEVERE, "IN - numGainChanges: " + numGainChanges
				+ " , gainPerChange: " + gainPerChange);
		
		output.setGain(0);
		play(offsetMillis);
		fadeInR(numGainChanges, changeInterval, gainPerChange, maxGain, true);
	}
	
	private void fadeInR(final int numLoops, final int loopInterval, final double gainIncrease, final double maxGain, boolean runImmediately) {
		if (output.getGain() >= maxGain) {
			output.setGain(maxGain);
			return;
		}
		Timer timer = new Timer() {
			@Override
			public void run() {
				logger.log(Level.SEVERE, "Increasing gain " + output.getGain() + " => " + (output.getGain()+gainIncrease));
				output.setGain(output.getGain() + gainIncrease);
				fadeInR(numLoops - 1, loopInterval, gainIncrease, maxGain, false);
			}
		};
		if (runImmediately) {
			timer.run();
		} else {
			timer.schedule(loopInterval);
		}
	}
	
	public void fadeOut(int fadeOutDuration) {
		if (fadeOutDuration <= 0) {
			stop();
			return;
		}
		final double minGain = 0;
		final int changeInterval = 100;
		// get number of increases based on doing it every 100 ms
		int numGainChanges = fadeOutDuration / changeInterval;
		if (fadeOutDuration % changeInterval != 0) {
			numGainChanges++;
		}
		double gainPerChange = (output.getGain() - minGain) / numGainChanges;
		
		logger.log(Level.SEVERE, "OUT - numGainChanges: " + numGainChanges
				+ " , gainPerChange: " + gainPerChange);

		fadeOutR(numGainChanges, changeInterval, gainPerChange, true);
	}
	
	private void fadeOutR(final int numLoops, final int loopInterval, final double gainDecrease, boolean runImmediately) {
		if (output.getGain() <= 0) {
			output.setGain(0);
			return;
		}
		Timer timer = new Timer() {
			@Override
			public void run() {
				logger.log(Level.SEVERE, "Decreasing gain " + output.getGain() + " => " + (output.getGain()-gainDecrease));
				output.setGain(output.getGain() - gainDecrease);
				fadeOutR(numLoops - 1, loopInterval, gainDecrease, false);
			}
		};
		if (runImmediately) {
			timer.run();
		} else {
			timer.schedule(loopInterval);
		}
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
		if (state == State.PLAYING) {
			stop();
		}
		double offsetSeconds = offset_millis / 1000;
		source.start(0, offsetSeconds);
		state = State.PLAYING;
	}
	
	public void stop() {
		Log.message(this, "stop playback");
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
		Context context = Context.get();
		source.disconnect(output);
		source.resetNode();
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
		// XXX, TODO: rework this
		source.connect(output);
	}
	
	public void setBuffer(Buffer buffer) {
		Log.message(this, "buffer reassigned");
		source.setBuffer(buffer);
	}
	
	public Buffer getBuffer() {
		return source.getBuffer();
	}
	
	public void setVolume(double volume) {
		Log.message(this, "set volume to " + volume);
		output.setGain(volume);
	}
	
	public double getVolume() {
		return output.getGain();
	}
	
	public void setPlaybackSpeed(double speed_scale) {
		Log.message(this, "set speed scale " + speed_scale);
		source.setPlaybackRate(speed_scale);
		
		// TODO: keep normal pitch if speed is changed, normally the pitch also changes
		// when the speed is changed
	}
	
	public double getPlaybackSpeed() {
		return source.getPlaybackRate();
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
