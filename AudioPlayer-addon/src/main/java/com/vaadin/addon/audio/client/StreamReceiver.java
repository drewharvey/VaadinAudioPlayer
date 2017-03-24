package com.vaadin.addon.audio.client;

import java.util.List;
import java.util.Map;

import com.vaadin.addon.audio.client.util.Log;
import com.vaadin.addon.audio.shared.ChunkDescriptor;

public class StreamReceiver {

	/**
	 * Callback object. The onDataReceived(chunk) method gets called
	 * when data for a certain chunk has been made available in this stream.
	 */
	public abstract class DataCallback {
		public abstract void onDataReceived(ChunkDescriptor chunk);
	}

	private AudioPlayerConnector connector;
	private List<ChunkDescriptor> chunks;
	private Map<ChunkDescriptor, Buffer> buffers;
	
	public StreamReceiver(AudioPlayerConnector connector, List<ChunkDescriptor> chunks) {
		Log.message(this, "create");
	}

	public Buffer getBufferForChunk(ChunkDescriptor chunk) {
		if(!buffers.containsKey(chunk)) {
			Log.error(this, "Requested data for unloaded chunk " + chunk);
		}
		return buffers.get(chunk);
	}
	
	public void requestChunk(ChunkDescriptor chunk, DataCallback onDataReceived) {
		Log.message(this, "request chunk by descriptor " + chunk);
		
		// TODO: how?
	}

	public void requestChunkById(int chunkId, DataCallback onDataReceived) {
		Log.message(this, "request chunk by ID " + chunkId);
		requestChunk(findChunkById(chunkId), onDataReceived);
	}

	public void requestChunkByTimestamp(int position_millis, DataCallback onDataReceived) {
		Log.message(this, "request chunk by timestimap " + position_millis);
		requestChunk(findChunkForPosition(position_millis), onDataReceived);
	}
	
	/**
	 * Try to find the chunk containing the main position  
	 * 
	 * @param position_millis
	 * @return
	 */
	private ChunkDescriptor findChunkForPosition(int position_millis) {
		return null;
	}
	
	/**
	 * Go through chunks in some smart way, try to find it by ID
	 * @param id
	 * @return
	 */
	private ChunkDescriptor findChunkById(int id) {
		return null;
	}
	
}
