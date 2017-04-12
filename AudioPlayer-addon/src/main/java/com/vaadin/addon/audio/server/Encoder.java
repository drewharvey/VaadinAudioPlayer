package com.vaadin.addon.audio.server;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * Abstract base class for media encoders.
 * Encoders take in PCM data and output another form of encoded audio data.
 * Encoders are used to provide chunks of encoded audio from the server to the client.
 */
public abstract class Encoder {

	private ByteBuffer inputBuffer;
	private PCMFormat inputFormat;
	
	public Encoder(ByteBuffer pcmBytes, PCMFormat inputFormat) {
		this.inputBuffer = pcmBytes;
		this.inputFormat = inputFormat;
	}

	/**
	 * Get access to the raw input data buffer
	 * 
	 * @return a ByteBuffer reference
	 */
	protected ByteBuffer getInputBuffer() {
		return inputBuffer;
	}
	
	/**
	 * Get access to the input format descriptor
	 * 
	 * @return a PCMFormat reference
	 */
	protected PCMFormat getInputFormat() {
		return inputFormat;
	}
	
	/**
	 * Encode a number of bytes from the input buffer. Return a byte array.
	 * 
	 * @param from_offset sample offset to encode from
	 * @param length number of samples to encode
	 * @param callback function to call when encoding is complete 
	 */
	public abstract byte[] encode(int from_offset, int length);
	
}
