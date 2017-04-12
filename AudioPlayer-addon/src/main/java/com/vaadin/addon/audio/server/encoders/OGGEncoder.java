package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * OGG encoder placeholder. We'll need to add a dependency to an
 * open source OGG encoder implementation and run it in a thread
 * or something. 
 */
public class OGGEncoder extends Encoder {

	public static class OGGFormat {
		// TODO: implement
	}
	
	private OGGFormat outputFormat;
	
	public OGGEncoder(ByteBuffer inputBuffer, PCMFormat inputFormat, OGGFormat outputFormat) {
		super(inputBuffer, inputFormat);
		this.outputFormat = outputFormat;
		
	}

	@Override
	public byte[] encode(int from_offset, int length) {

		return null;
	}

}
