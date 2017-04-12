package com.vaadin.addon.audio.client.webaudio;

import com.google.gwt.user.client.Timer;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

public class BufferSourceNode extends AudioScheduledSourceNode {

	// See https://developer.mozilla.org/en-US/docs/Web/API/AudioBufferSourceNode
	
	private Buffer buffer;
	private Timer bufferTimer; 
	
	private static final native elemental.html.AudioNode
	createBufferSource(AudioContext ctx) /*-{
		return ctx.createBufferSource();
	}-*/;
	
	protected BufferSourceNode(AudioContext ctx) {
		super(createBufferSource(ctx));
	}
	
	public void setBuffer(Buffer buffer) {
		if(buffer == this.buffer) {
			return;
		}
		
		this.buffer = buffer;
		if(bufferTimer != null && bufferTimer.isRunning()) {
			bufferTimer.cancel();
			bufferTimer = null;
		}
		bufferTimer = new Timer() {
			@Override
			public void run() {
				Buffer b = BufferSourceNode.this.buffer;
				if(!b.isReady()) {
					bufferTimer.schedule(20);
				} else {
					Log.message(BufferSourceNode.this, "set audio buffer");
					setNativeBuffer(b.getAudioBuffer());
				}
			}
		};
		// Call run instead of schedule to immediately run the timer logic
		bufferTimer.run();
	}
	
	public void setNativeBuffer(AudioBuffer buffer) {
		setBuffer(getNativeNode(), buffer);
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
		node.detune = cents;
	}-*/;
	
	public double getDetune() {
		return getDetune(getNativeNode());
	}
	
	private static final native double getDetune(elemental.html.AudioNode node) /*-{
		return node.detune;
	}-*/;
	
	public void setPlaybackRate(double rate) {
		setPlaybackRate(getNativeNode(), rate);
	}
	
	private static final native void setPlaybackRate(elemental.html.AudioNode node, double rate) /*-{
		node.playbackRate = rate;
	}-*/;
	
	public double getPlaybackRate() {
		return getPlaybackRate(getNativeNode());
	}
	
	private static final native double getPlaybackRate(elemental.html.AudioNode node) /*-{
		return node.playbackRate;
	}-*/;
	
}
