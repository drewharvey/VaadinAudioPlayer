package com.vaadin.addon.audio.client;

import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;
import elemental.html.Uint8Array;

/*
 * Wraps a WebAudio data buffer
 */
public class Buffer {

	private AudioBuffer buffer;
	private Uint8Array data;
	
	/**
	 * Create a new WebAudio buffer using a 
	 * 
	 * @param context
	 * @param encodedData
	 */
	public Buffer(AudioContext context, boolean compressedData, String encodedData) {
		Log.message(this, "create");
		
	}
	
	public AudioBuffer getAudioBuffer() {
		return buffer;
	}
	
	public String toString() {
		return "Buffer";
	}
	
}
