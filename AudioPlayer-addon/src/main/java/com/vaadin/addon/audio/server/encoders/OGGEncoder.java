package com.vaadin.addon.audio.server.encoders;

import com.vaadin.addon.audio.server.Encoder;
import com.vaadin.addon.audio.server.encoders.MP3Encoder.MP3Format;

/**
 * OGG encoder placeholder. We'll need to add a dependency to an
 * open source OGG encoder implementation and run it in a thread
 * or something. 
 */
public class OGGEncoder extends Encoder {

	public static class OGGFormat {
		// TODO: implement
	}
	
	private static final OGGFormat DEFAULT_FORMAT;
	
	static {
		DEFAULT_FORMAT = new OGGFormat();
		// TODO: init ogg format
	}

	
	private OGGFormat outputFormat;
	
	public OGGEncoder() {
		this(DEFAULT_FORMAT);
	}
	
	public OGGEncoder(OGGFormat outputFormat) {
		this.outputFormat = outputFormat;
		
	}

	@Override
	public byte[] encode(int from_offset, int length) {

		// TODO: implement OGG encoding
		
		return null;
	}

}
