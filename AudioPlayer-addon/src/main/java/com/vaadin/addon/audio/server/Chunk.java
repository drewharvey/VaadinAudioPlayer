package com.vaadin.addon.audio.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.server.StreamResource;

/**
 * 
 */
@SuppressWarnings("serial")
public class Chunk extends StreamResource {

	// TODO: we need to connect these up to the client.
	// Vaadin will take care of the serialization, but receiving these on the client
	// is still not implemented
	
	private static final class ByteStream implements StreamSource {
		private InputStream istream;
		
		public ByteStream(byte[] bytes) {
			istream = new ByteArrayInputStream(bytes);
		}

		@Override
		public InputStream getStream() {
			return istream;
		}
	}

	private ChunkDescriptor descriptor;
	
	public Chunk(AudioPlayer player, int chunkId, byte[] bytes) {
		super(new ByteStream(bytes), "chunk" + chunkId);
		descriptor = player.getChunkDescriptor(chunkId);
	}
	
	public ChunkDescriptor getDescriptor() {
		return descriptor;
	}
	
}
