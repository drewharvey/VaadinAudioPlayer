package com.vaadin.addon.audio.client.webaudio;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

//See https://developer.mozilla.org/en-US/docs/Web/API/AudioBufferSourceNode
public class BufferSourceNode extends AudioScheduledSourceNode {
	
	private static final Logger logger = Logger.getLogger("BufferSourceNode");


	public static interface BufferReadyListener {
		void onBufferReady(Buffer b);
	}
	
	private Buffer buffer;
	private Timer bufferTimer;
	private double playbackRate = 1d;
	
	private static final native elemental.html.AudioNode
	createBufferSource(AudioContext ctx) /*-{
		return ctx.createBufferSource();
	}-*/;
	
	protected BufferSourceNode(AudioContext ctx) {
		super(createBufferSource(ctx));
	}

	public void resetNode() {
		// create new buffer source node and add the current buffer
		setNativeNode(createBufferSource(getNativeContext()));
	}
	
	public void setBuffer(final Buffer buffer, final BufferReadyListener cb) {
		if(buffer == this.buffer) {
			return;
		}
		
		this.buffer = buffer;
		// cancel previous buffer check timer
		if(bufferTimer != null && bufferTimer.isRunning()) {
			bufferTimer.cancel();
			bufferTimer = null;
		}
		// set timer to periodically check if buffer is ready to play
		bufferTimer = new Timer() {
			@Override
			public void run() {
				Buffer b = BufferSourceNode.this.buffer;
				if(!b.isReady()) {
					bufferTimer.schedule(20);
				} else {
					logger.log(Level.SEVERE, " === AUDIO BUFFER IS READY ==== ");
					setNativeBuffer(b.getAudioBuffer());
					if (cb != null) {
						cb.onBufferReady(b);
					}
				}
			}
		};
		// Call run instead of schedule to immediately run the timer logic
		bufferTimer.run();
	}
	
	public void setNativeBuffer(AudioBuffer buffer) {
		setBuffer(getNativeNode(), buffer);
		logger.log(Level.SEVERE, "numberOfChannels: " + buffer.getNumberOfChannels());
		logger.log(Level.SEVERE, "ins/outs: " + this.getNumberOfInputs() + "/" + this.getNumberOfOutputs());
	}
	
	private static final native void setBuffer(elemental.html.AudioNode node, AudioBuffer buffer) /*-{
		node.buffer = buffer;
	}-*/;
	
	public Buffer getBuffer() {
		return buffer;
	}
	
	public AudioBuffer getNativeBuffer() {
		return getBuffer(getNativeNode());
	}
	
	private static final native AudioBuffer getBuffer(elemental.html.AudioNode node) /*-{
		return node.buffer;
	}-*/;

	public void setDetune(double cents) {
		setDetune(getNativeNode(), cents);
	}
	
	private static final native void setDetune(elemental.html.AudioNode node, double cents) /*-{
		node.detune.value = cents;
	}-*/;
	
	public double getDetune() {
		return getDetune(getNativeNode());
	}
	
	private static final native double getDetune(elemental.html.AudioNode node) /*-{
		return node.detune.value;
	}-*/;
	
	public void setPlaybackRate(double rate) {
		// playback rate of 0 doesn't make sense and sets us up for exceptions
		if (rate <= 0) {
			rate = 0.01;
		}
		playbackRate = rate;

		// comment this out if you're stretching the buffer instead
		setPlaybackRate(getNativeNode(), rate);
	}
	
	private static final native void setPlaybackRate(elemental.html.AudioNode node, double rate) /*-{
		// set nodes playback rate
		node.playbackRate.value = rate;
    }-*/;
	
	public double getPlaybackRate() {
		return playbackRate;
		//return getPlaybackRate(getNativeNode());
	}
	
	private static final native double getPlaybackRate(elemental.html.AudioNode node) /*-{
		return node.playbackRate.value;
	}-*/;
	
	@Override
	public String toString() {
		return "BufferSourceNode";
	}
}
