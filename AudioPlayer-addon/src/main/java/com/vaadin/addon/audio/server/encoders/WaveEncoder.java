package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * PCM-to-MSWave encoder.
 */
public class WaveEncoder extends Encoder {

	// TODO: allow different input and output formats
	
	// 
	//
	//
	
	private PCMFormat outputFormat;
	private PCMFormat inputFormat;
	private ByteBuffer inputBuffer;
	
	public WaveEncoder(ByteBuffer pcmBytes, PCMFormat inputFormat, PCMFormat outputFormat) {
		
	}
	
	// TODO: maybe we should just keep a static buffer of bytes?
	// TODO: run a performance profile on this and see if we're clogging up the heap

	@Override
	public void encode(int from_offset, int length, Callback callback) {

		
		
		byte[] bytes = new byte[length]; 
		getBuffer().get(bytes,from_offset,length);
		callback.onComplete(bytes);
	}

}
