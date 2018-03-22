package org.vaadin.addon.audio.server.encoders;

import org.vaadin.addon.audio.server.Encoder;

/**
 * MP3 encoder placeholder.
 * User will probably provide this and we won't need it,
 * but it will be good to have during testing... 
 */
public class MP3Encoder extends Encoder {

	public static class MP3Format {
		// TODO: implement
	}
	
	private static final MP3Format DEFAULT_FORMAT;
	
	static {
		DEFAULT_FORMAT = new MP3Format();
		// TODO: init mp3 format
	}
	
	private MP3Format outputFormat;
	
	public MP3Encoder() {
		this(DEFAULT_FORMAT);
	}
	
	public MP3Encoder(MP3Format outputFormat) {
		this.outputFormat = outputFormat;
		
		// Actual constructor
	}

	@Override
	public byte[] encode(int from_offset, int length) {

		return null;
	}

	public static boolean isSupported() {
		// TODO: actually report if it's supported or not
		return false;
	}

}
