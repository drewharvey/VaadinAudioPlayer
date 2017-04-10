package com.vaadin.addon.audio.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.audio.shared.ChunkDescriptor;

public class Stream {
	
	private List<ChunkDescriptor> chunks = new ArrayList<ChunkDescriptor>();
	private ByteBuffer buffer = null;
	private Encoder encoder = null;

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
	 * Read encoded data for a chunk identified by its descriptor into target array
	 * 
	 * @param chunk
	 * @param targetArray
	 */
	public void getChunkData(ChunkDescriptor chunk, byte[] targetArray) {
		
	}
	
	/**
	 * Find the chunk descriptor that contains the requested timestamp
	 * 
	 * @param position_millis
	 * @return
	 */
	public ChunkDescriptor findChunk(int position_millis) {
		// Use binary search to locate the chunk responsible for position
		return null;
	}
	
	public int getChunkCount() {
		return chunkCount;
	}
	
	public int getDuration() {
		return duration;
	}
	
}
