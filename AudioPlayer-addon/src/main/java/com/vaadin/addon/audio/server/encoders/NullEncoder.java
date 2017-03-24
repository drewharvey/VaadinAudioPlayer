package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;

/**
 * This encoder does nothing, it just passes data right through.
 * It should be the default encoder.
 */
public class NullEncoder extends Encoder {

	public NullEncoder(ByteBuffer pcmData) {
		super(pcmData);
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
