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
	
	public WaveEncoder(ByteBuffer pcmBytes, PCMFormat inputFormat, PCMFormat outputFormat) {
		super(pcmBytes,inputFormat);
		this.outputFormat = outputFormat;
	}
	
	@Override
	public byte[] encode(int from_offset, int length) {
		
		return null;
	}

}
