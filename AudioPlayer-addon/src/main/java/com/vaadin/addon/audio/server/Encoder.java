package com.vaadin.addon.audio.server;

import java.nio.ByteBuffer;

public abstract class Encoder {

	/**
	 * Callback that returns the bytes requested by the application
	 */
	public abstract class Callback {
		public abstract void onComplete(byte[] encodedBytes);
	}
	
	private ByteBuffer buffer;
	
	public Encoder() {
		buffer = null;
	}
	
	public void setBuffer(ByteBuffer buf) {
		buffer = buf;
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public abstract void encode(int from_offset, int length, Callback callback);
	
}
