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
	
	private final AudioContext ctx;
	private AudioDestinationNode destination;
	
	private static final native AudioContext createContext() /*-{
		var audioCtx = new AudioContext();
		console.log("created context: " + audioCtx);
		return audioCtx;
	}-*/;
	
	private Context() {
		ctx = createContext();
	}
	
	public void decodeAudioData(ArrayBuffer buf, AudioBufferCallback cb) {
		decodeAudioData(ctx,buf,cb);
	}
	
	private static final native void decodeAudioData(AudioContext ctx, ArrayBuffer audioData, AudioBufferCallback cb) /*-{
		ctx.decodeAudioData(audioData, function(buffer) {
			@com.vaadin.addon.audio.client.webaudio.Context::audioDataDecodeSuccess(Lelemental/html/AudioBuffer;Lcom/vaadin/addon/audio/client/webaudio/Context$AudioBufferCallback;)(buffer,cb);
		}, function() {
			@com.vaadin.addon.audio.client.webaudio.Context::audioDataDecodeFailed(Lcom/vaadin/addon/audio/client/webaudio/Context$AudioBufferCallback;)(cb);
		});
	}-*/;
	
	protected static void audioDataDecodeSuccess(AudioBuffer buffer, AudioBufferCallback cb) {
		cb.onComplete(buffer);
	}
	
	protected static void audioDataDecodeFailed(AudioBufferCallback cb) {
		cb.onError();
	}
	
	public AudioDestinationNode getDestination() {
		if(destination == null) {
			destination = new AudioDestinationNode(getDestination(ctx));
		}
		return destination;
	}
	
	private static final native elemental.html.AudioNode getDestination(AudioContext ctx) /*-{
		var dest = ctx.destination;
		console.log("context destination: " + dest);
		return dest;
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
