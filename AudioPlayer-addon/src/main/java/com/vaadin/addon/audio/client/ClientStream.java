package com.vaadin.addon.audio.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.addon.audio.shared.ChunkDescriptor;
import com.vaadin.addon.audio.shared.util.Log;

/**
 * Handles stream logic on the client.
 */
public class ClientStream {

	// TODO: the following constants should be tweakable
	
	/**
	 * Number of buffers to retain. Player objects should
	 * lock the 
	 */
	private static final int MAX_BUFFER_RETAIN_COUNT = 8;
	
	/**
	 * Callback object. The onDataReceived(chunk) method gets called
	 * when data for a certain chunk has been made available in this stream.
	 */
	public abstract class DataCallback {
		public abstract void onDataReceived(ChunkDescriptor chunk);
	}

	private AudioPlayerConnector connector;
	private Map<ChunkDescriptor, Buffer> buffers;
	private Map<ChunkDescriptor, DataCallback> requests;
	
	public ClientStream(AudioPlayerConnector connector, List<ChunkDescriptor> chunks) {
		Log.message(this, "create");
		
		buffers = new LinkedHashMap<>();
		requests = new HashMap<>();
	}

	public Buffer getBufferForChunk(ChunkDescriptor chunk) {
		if(!buffers.containsKey(chunk)) {
			Log.error(this, "Requested data for unloaded chunk " + chunk);
		}
		return buffers.get(chunk);
	}
	
	public void requestChunk(ChunkDescriptor chunk, DataCallback onDataReceived) {
		Log.message(this, "request chunk by descriptor " + chunk);

		// If the chunk is retained in the buffers, just directly notify of its existence 
		if(buffers.get(chunk) != null) {
			onDataReceived.onDataReceived(chunk);
			return;
		}

		// Otherwise, put in a request for the data
		requests.put(chunk, onDataReceived);
		connector.getServerRPC().requestChunk(chunk.getId());
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
	
	
	/**
	 * Receive notification about a chunk having data made available, i.e.
	 * the server has finished transmitting data for the requested chunk.
	 * 
	 * XXX: called from AudioPlayerConnector - we'd want to wrap this in a prettier fashion 
	 */
	protected void notifyChunkLoaded(int chunkId, Buffer buffer) {

		// If there's more than MAX_BUFFER_RETAIN_COUNT buffers retained, free the first one
		if(buffers.size() > MAX_BUFFER_RETAIN_COUNT) {
			Entry<ChunkDescriptor, Buffer> it = buffers.entrySet().iterator().next();
			
			buffers.remove(it.getKey());
		}
		
		// Once we have data for a chunk, store it in the buffers
		ChunkDescriptor chunk = findChunkById(chunkId);
		buffers.put(chunk, buffer);
	
		// Notify of chunk being received
		DataCallback cb = requests.get(chunk);
		cb.onDataReceived(chunk);
	}
	
	public String toString() {
		return "ClientStream";
	}

}
