package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * PCM-to-MSWave encoder.
 */
public class WaveEncoder extends Encoder {

	private PCMFormat outputFormat;
	
	public WaveEncoder() {
		this(null);
	}
	
	public WaveEncoder(PCMFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	@Override
	public byte[] encode(int from_offset, int length) {
		
		PCMFormat infmt = getInputFormat();
		PCMFormat outfmt = outputFormat == null ? infmt : outputFormat;
		
		// TODO: allow different input and output formats - we currently do no conversion
				
		
		return null;
	}

}
