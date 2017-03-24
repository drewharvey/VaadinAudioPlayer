package com.vaadin.addon.audio.server.encoders;

import java.nio.ByteBuffer;

import com.vaadin.addon.audio.server.Encoder;

/**
 * OGG encoder placeholder. We'll need to add a dependency to an
 * open source OGG encoder implementation and run it in a thread
 * or something. 
 */
public class OGGEncoder extends Encoder {

	public OGGEncoder(ByteBuffer pcmData) {
		super(pcmData);
	}

	@Override
	public void encode(int from_offset, int length, Callback callback) {
		// TODO Auto-generated method stub
		
	}

}
