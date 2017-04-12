package com.vaadin.addon.audio.client;

import com.vaadin.addon.audio.shared.util.Log;

import elemental.html.AudioBuffer;
import elemental.html.AudioContext;
import elemental.html.Uint8Array;

/*
 * Wraps a WebAudio data buffer
 */
public class Buffer {

	private AudioBuffer buffer = null;
	private Uint8Array data = null;

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
		Uint8Array decodedBytes = StreamDataDecoder.decode(encodedData);
		if (compressedData) {
			data = StreamDataDecoder.decompress(decodedBytes);
		} else {
			data = decodedBytes;
		}
		Log.message(this, "created");
	}

	/**
	 * Get the WebAudio AudioBuffer object
	 * @return
	 */
	public AudioBuffer getAudioBuffer() {
		if(buffer == null) {
			Log.message(this, "creating AudioBuffer object");
			buffer = AudioPlayerConnector.getAudioContext().createBuffer(data.getBuffer(), false);
			Log.message(this, "AudioBuffer object created");
		}
		return buffer;
	}

	public String toString() {
		return "Buffer";
	}

}
