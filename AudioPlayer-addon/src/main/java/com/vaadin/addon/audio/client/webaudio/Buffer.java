package com.vaadin.addon.audio.client.webaudio;

import com.vaadin.addon.audio.client.StreamDataDecoder;
import com.vaadin.addon.audio.client.webaudio.Context.AudioBufferCallback;
import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.ArrayBuffer;
import elemental.html.AudioBuffer;
import elemental.html.Uint8Array;

/*
 * Wraps a WebAudio data buffer
 */
public class Buffer {

	private AudioBuffer buffer = null;
	private ArrayBuffer data = null;

	/**
	 * Create a new WebAudio buffer using any compatible audio data
	 * 
	 * @param encodedData
	 *            data encoded using the StreamDataEncoder on the server side
	 * 
	 * @param compressedData
	 *            set to true if compression was also used
	 */
	public Buffer(String encodedData, boolean compressedData) {
		Log.message(this, "decoding data");
		ArrayBuffer decodedBytes = StreamDataDecoder.decode(encodedData);
		if (compressedData) {
			data = StreamDataDecoder.decompress(decodedBytes);
		} else {
			data = decodedBytes;
		}
		
		Context.get().decodeAudioData(data, new AudioBufferCallback() {
			@Override
			public void onError() {
				Log.error(Buffer.this, "error decoding audio data buffer");
				buffer = null;
			}
			
			@Override
			public void onComplete(AudioBuffer buffer) {
				Log.message(Buffer.this, "audio data decoded");
				Buffer.this.buffer = buffer;
			}
		});
	}

	public boolean isReady() {
		return buffer != null;
	}
	
	/**
	 * Get the WebAudio AudioBuffer object
	 */
	public AudioBuffer getAudioBuffer() {
		return buffer;
	}

	public void setAudioBuffer(AudioBuffer audioBuffer) {
		buffer = audioBuffer;
	}
	
	public ArrayBuffer getData() {
		return data;
	}

	public String toString() {
		return "Buffer";
	}

}
