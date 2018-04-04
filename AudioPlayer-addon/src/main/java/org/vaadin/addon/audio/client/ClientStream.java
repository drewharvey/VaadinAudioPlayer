package org.vaadin.addon.audio.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.logging.Logger;

import org.vaadin.addon.audio.client.webaudio.Buffer;
import org.vaadin.addon.audio.shared.ChunkDescriptor;
import org.vaadin.addon.audio.shared.util.Log;

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
	public static interface DataCallback {
		public void onDataReceived(ChunkDescriptor chunk);
	}

	private AudioPlayerConnector connector;
	private Map<ChunkDescriptor, Buffer> buffers;
	private Map<ChunkDescriptor, List<DataCallback>> requests;
	
	public ClientStream(AudioPlayerConnector connector) {
		Log.message(this, "create");

		this.connector = connector;
		buffers = new LinkedHashMap<>();
		requests = new HashMap<>();
	}
	
	public int getDuration() {
		return connector.getState().duration;
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

		List<DataCallback> cblist = requests.get(chunk);
		if(cblist != null) {
			cblist.add(onDataReceived);
			return;
		}
		
		cblist = new ArrayList<DataCallback>();
		cblist.add(onDataReceived);
		
		// Otherwise, put in a request for the data
		requests.put(chunk, cblist);
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
		// Read chunks directly from connector
		List<ChunkDescriptor> chunks = connector.getState().chunks;
		
		for(ChunkDescriptor c : chunks) {
			if(c.getStartTimeOffset() <= position_millis 
					&& (c.getEndTimeOffset() - c.getLeadInDuration()) >= position_millis) {
				return c;
			}
		}

		// TODO: fail gracefully
		Logger.getLogger("ClientStream").severe("FAILED TO FIND CHUNK FOR " + position_millis + "ms");
		return chunks.get(0);
	}
	
	/**
	 * Go through chunks in some smart way, try to find it by ID
	 * @param id
	 * @return
	 */
	private ChunkDescriptor findChunkById(int id) {
		// Read chunks directly from connector
		List<ChunkDescriptor> chunks = connector.getState().chunks;
	
		for(ChunkDescriptor c : chunks) {
			if(c.getId() == id) {
				return c;
			}
		}

		// TODO: fail gracefully
		return chunks.get(0);
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
		List<DataCallback> cbs = requests.remove(chunk);
		for(DataCallback cb : cbs) {
			cb.onDataReceived(chunk);
		}
	}
	
	public String toString() {
		return "ClientStream";
	}

}
