package com.vaadin.addon.audio.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.addon.audio.client.effects.BalanceEffect;
import com.vaadin.addon.audio.client.utils.TimeStretch;
import com.vaadin.addon.audio.client.webaudio.AudioNode;
import com.vaadin.addon.audio.client.webaudio.Buffer;
import com.vaadin.addon.audio.client.webaudio.BufferSourceNode;
import com.vaadin.addon.audio.client.webaudio.Context;
import com.vaadin.addon.audio.client.webaudio.GainNode;
import com.vaadin.addon.audio.shared.util.Log;
import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

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
	private AudioBuffer unmodifiedBuffer = null;
	private double playbackSpeed = 1;
	

	public BufferPlayer() {
		this(null);
	}
	
	public BufferPlayer(Buffer buffer) {
		//logger.log(Level.SEVERE, "create");
		Context context = Context.get();
		source = context.createBufferSourceNode();
		if (buffer != null) {
			unmodifiedBuffer = buffer.getAudioBuffer();
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

	public boolean isPlaying() {
		return state == State.PLAYING;
	}
	
	public void play(int offset_millis) {
		logger.log(Level.SEVERE, "start playback at " + offset_millis);
		if (state == State.PLAYING) {
			stop();
		}
		double offsetSeconds = offset_millis / 1000d;
		source.start(0, offsetSeconds);
		state = State.PLAYING;
	}
	
	public void stop() {
		logger.log(Level.SEVERE, "stop playback");
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
		resetSourceNode(buffer, null);
	}

	private void resetSourceNode(Buffer buffer, final BufferSourceNode.BufferReadyListener cb) {
		source.disconnect();
		source.resetNode();
		if (buffer != null) {
			source.setNativeBuffer(buffer.getAudioBuffer());
		}
	}
	
	public void setBuffer(Buffer buffer) {
		setBuffer(buffer, null);
	}
	
	public void setBuffer(Buffer buffer, final BufferSourceNode.BufferReadyListener cb) {
		source.setBuffer(buffer, new BufferSourceNode.BufferReadyListener() {
			@Override
			public void onBufferReady(Buffer b) {
				// since we may time stretch the buffer, we need to keep
				// a clean copy of the buffer in case setPlaybackSpeed gets called
				setUnmodifiedBuffer(b.getAudioBuffer());
				if (cb != null) {
					cb.onBufferReady(b);
				}
			}
		});
	}
	
	public Buffer getBuffer() {
		return source.getBuffer();
	}

	private void setUnmodifiedBuffer(AudioBuffer buffer) {
		unmodifiedBuffer = buffer;
	}
	
	public void setVolume(double volume) {
		// logger.log(Level.SEVERE, "set volume to " + volume);
		output.setGain(volume);
	}
	
	public double getVolume() {
		return output.getGain();
	}

	/**
	 * Changes the speed at which the BufferPlayer plays the audio without changing
	 * the pitch of the audio. The audio buffer will need to be reprocessed in order to
	 * play at the speed requested.
	 * @param playbackSpeed value greater than 0
	 */
	public void setPlaybackSpeed(double playbackSpeed) {
		logger.log(Level.SEVERE, "set speed scale " + playbackSpeed);
		if (this.playbackSpeed == playbackSpeed) {
			// don't do anything if we aren't changing the scale
			logger.log(Level.SEVERE, "playback speed did not change");
			return;
		}
		// you can only set a BufferSourceNode's buffer once, so lets reset the node and re-set the buffer
		source.disconnect();
		source.resetNode();
		source.setPlaybackRate(playbackSpeed);
		// generate warped buffer if playback speed is other than 1
		if (unmodifiedBuffer != null) {
			AudioBuffer buffer;
			if (playbackSpeed != 1) {
				logger.log(Level.SEVERE, "stretching audio chunk to fit playback speed of " + playbackSpeed);
				double stretchFactor = 1d * playbackSpeed;
				AudioContext context = Context.get().getNativeContext();
				int numChannels = unmodifiedBuffer.getNumberOfChannels();
				buffer = TimeStretch.strechAudioBuffer(stretchFactor, unmodifiedBuffer, context, numChannels, false);
				logger.log(Level.SEVERE, "stretching complete");
			} else {
				buffer = unmodifiedBuffer;
			}
			// apply our buffer ot the source BufferSourceNode
			if (buffer != null) {
				logger.log(Level.SEVERE, "Setting buffer");
				source.setNativeBuffer(buffer);
			}
		} else {
			logger.log(Level.SEVERE, "unmodifiedBuffer is NULL");
		}
	}
	
	public double getPlaybackSpeed() {
		return playbackSpeed;
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
