package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.shared.PCMFormat;

/**
 * MP3 encoder placeholder.
 * User will probably provide this and we won't need it,
 * but it will be good to have during testing... 
 */
public class MP3Encoder extends Encoder {

	public static class MP3Format {
		// TODO: implement
	}
	
	private MP3Format outputFormat;
	
	public MP3Encoder(ByteBuffer inputBuffer, PCMFormat inputFormat, MP3Format outputFormat) {
		super(inputBuffer, inputFormat);
		this.outputFormat = outputFormat;
		
	}

	@Override
	public byte[] encode(int from_offset, int length) {

		return null;
	}

	public static boolean isSupported() {
		return false;
	}

}
