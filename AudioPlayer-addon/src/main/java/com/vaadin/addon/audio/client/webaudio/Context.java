package com.vaadin.addon.audio.client.webaudio;

import elemental.html.ArrayBuffer;
import elemental.html.AudioBuffer;
import elemental.html.AudioContext;

/**
 * Custom implementation of WebAudio's AudioContext object 
 */
public class Context {

	public static interface AudioBufferCallback {
		public void onComplete(AudioBuffer buffer);
		
		public void onError();
	}
	
	private static Context instance;
	public static Context get() {
		if(instance == null) {
			instance = new Context();
		}
		return instance;
	}
	
	private AudioContext ctx;
	
	private static final native AudioContext createContext() /*-{
		var AudioContext = window.AudioContext || window.webkitAudioContext;
		var audioCtx = new AudioContext();
		return audioCtx;
	}-*/;
	
	private Context() {
		ctx = createContext();
	}
	
	public AudioBuffer decodeAudioData(ArrayBuffer buf, AudioBufferCallback cb) {
		return decodeAudioData(ctx,buf);
	}
	
	private static final native AudioBuffer decodeAudioData(AudioContext ctx, ArrayBuffer audioData) /*-{
		return ctx.decodeAudioData
	}-*/;
	
	public AudioNode getDestination() {
		return getDestination(ctx);
	}
	
	private static final native AudioNode getDestination(AudioContext ctx) /*-{
		return ctx.destination;
	}-*/;
	
	
	//
	// Node creation
	//
	
	public BufferSourceNode createBufferSourceNode() {
		return new BufferSourceNode(ctx);
	}
	
	public GainNode createGainNode() {
		return new GainNode(ctx);
	}
	
	// TODO: add more node types as you need them
	
}
