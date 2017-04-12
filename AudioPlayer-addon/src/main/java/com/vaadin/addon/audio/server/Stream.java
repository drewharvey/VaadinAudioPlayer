package com.vaadin.addon.audio.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.shared.ChunkDescriptor;

/**
 * Server-side datastream class
 */
public class Stream {

	public static interface Callback {
		public void onComplete(String encodedData);
	}
	
	private List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();
	private ByteBuffer buffer = null;
	private Encoder encoder = null;

	private boolean compression = false;
	private int chunkCount = 0;
	private int duration = 0;
	
	/**
	 * Create an audio stream. Performs on-the-fly audio encoding
	 * on a chunk-by-chunk basis.
	 * 
	 * @param pcmBuffer Buffer containing PCM data
	 * @param encoder 
	 */
	public Stream(ByteBuffer pcmBuffer, Encoder encoder) {
		this.buffer = pcmBuffer;
		this.encoder = encoder;
	}

	/**
	 * Enable or disable datastream compression. If compression is enabled,
	 * data retrieved from this stream is run through a fast zlib compression
	 * routine before being base64 encoded for transport.
	 * 
	 * If you're using the NullEncoder, compression is encouraged.
	 * For any other encoding scheme, use of compression should be decided on
	 * a case-by-case basis.
	 * 
	 * @param enable true to enable, false to disable
	 */
	public void setCompression(boolean enable) {
		compression = enable;
	}
	
	/**
	 * 
	 * 
	 * @return true if compression is being used
	 */
	public boolean isCompressionEnabled() {
		return compression;
	}
	
	/**
	 * Get data for a chunk of audio as an encoded string.
	 * This method is used to facilitate audio transport.
	 * 
	 * The logic runs in its own thread, and calls cb when complete.
	 */
	public void getChunkData(ChunkDescriptor chunk, Callback cb) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int startOffset = chunk.getStartStreamByteOffset();
				int endOffset = chunk.getEndStreamByteOffset();
				int length = endOffset - startOffset; 
				byte[] bytes = new byte[length];
				buffer.get(bytes,startOffset,length);
				if(compression) {
					bytes = DataEncoder.compress(bytes);
				}
				cb.onComplete(DataEncoder.encode(bytes));
			}
		}).run();
	}
	
	/**
	 * Find the chunk descriptor that contains the requested timestamp
	 * 
	 * @param position_millis
	 * @return
	 */
	public ChunkDescriptor findChunk(int position_millis) {
		// Use binary search to locate the chunk responsible for position
		// TODO: maybe remove this function, we don't necessarily need it at all
		return null;
	}

	public ChunkDescriptor getChunkById(int chunkID) {
		return null;
	}
	
	public int getChunkCount() {
		return chunkCount;
	}
	
	public int getDuration() {
		return duration;
	}
	
}
