package com.vaadin.addon.audio.client;

import com.vaadin.addon.audio.client.util.Log;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;
import elemental.html.Uint8Array;

/*
 * Wraps a WebAudio data buffer
 */
public class Buffer {

	private AudioBuffer buffer;
	private Uint8Array data;
	
	public Buffer(AudioContext context, byte[] b) {
		Log.message(this, "create");
	}
	
	public AudioBuffer getAudioBuffer() {
		return buffer;
	}
	
}
